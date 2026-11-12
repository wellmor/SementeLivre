package com.sementelivre.backend.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

/** Admin cadastra outros admins: qualquer admin vê a lista, só o SUPER_ADMIN cadastra. */
class AdministradoresIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String URL = "/admin/administradores";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String loginSuperAdmin() throws Exception {
        String email = criarAdmin();
        jdbcTemplate.update("""
                UPDATE admin_t SET nivel_acesso = 'SUPER_ADMIN'
                WHERE pessoa_id = (SELECT id FROM pessoa_t WHERE email = ?)""", email);
        return login(email, SENHA);
    }

    private static String novoAdminJson(String email, String documento, String senha, String nivel) {
        String campoNivel = nivel == null ? "" : ", \"nivelAcesso\": \"%s\"".formatted(nivel);
        return """
                {"nome": "Nova Admin", "email": "%s", "tipoDocumento": "CPF", "documento": "%s", "senha": "%s"%s}"""
                .formatted(email, documento, senha, campoNivel);
    }

    private ResultActions cadastrar(String token, String corpo) throws Exception {
        return mockMvc.perform(post(URL)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private static String emailNovo() {
        return "nova-admin-" + sufixoUnico() + "@teste.com";
    }

    @Test
    void superAdminCadastraAdminQueJaConsegueEntrar() throws Exception {
        String superAdmin = loginSuperAdmin();
        String email = emailNovo();

        cadastrar(superAdmin, novoAdminJson(email, cpfValido(), "Provisoria123", null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nivelAcesso").value("ADMIN"));

        String novo = login(email, "Provisoria123");
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, novo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].email", hasItem(email)))
                .andExpect(jsonPath("$[*].email", hasItem("admin@sementelivre.com.br")))
                .andExpect(jsonPath("$[0].nivelAcesso").value("SUPER_ADMIN"));
        // E entra na área de admin de verdade
        mockMvc.perform(get("/admin/confirmacao").header(HttpHeaders.AUTHORIZATION, novo))
                .andExpect(status().isOk());

        // O admin comum não cadastra outros
        cadastrar(novo, novoAdminJson(emailNovo(), cpfValido(), "Provisoria123", null))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminPodeCadastrarOutroSuperAdmin() throws Exception {
        String superAdmin = loginSuperAdmin();
        String email = emailNovo();

        cadastrar(superAdmin, novoAdminJson(email, cpfValido(), "Provisoria123", "SUPER_ADMIN"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelAcesso").value("SUPER_ADMIN"));

        cadastrar(login(email, "Provisoria123"), novoAdminJson(emailNovo(), cpfValido(), "Provisoria123", null))
                .andExpect(status().isCreated());
    }

    @Test
    void adminComumEProprietarioNaoCadastram() throws Exception {
        cadastrar(loginAdmin(), novoAdminJson(emailNovo(), cpfValido(), "Provisoria123", null))
                .andExpect(status().isForbidden());

        Conta produtor = cadastrar();
        String tokenProdutor = login(produtor.email(), SENHA);
        cadastrar(tokenProdutor, novoAdminJson(emailNovo(), cpfValido(), "Provisoria123", null))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(URL).header(HttpHeaders.AUTHORIZATION, tokenProdutor))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadastroInvalidoEhRecusado() throws Exception {
        String superAdmin = loginSuperAdmin();
        Conta produtor = cadastrar();

        // E-mail ou documento de outra conta
        cadastrar(superAdmin, novoAdminJson(produtor.email(), cpfValido(), "Provisoria123", null))
                .andExpect(status().isConflict());
        cadastrar(superAdmin, novoAdminJson(emailNovo(), produtor.documento(), "Provisoria123", null))
                .andExpect(status().isConflict());
        // CPF inválido, senha curta, nível que não existe na tela
        cadastrar(superAdmin, novoAdminJson(emailNovo(), "12345678900", "Provisoria123", null))
                .andExpect(status().isBadRequest());
        cadastrar(superAdmin, novoAdminJson(emailNovo(), cpfValido(), "curta", null))
                .andExpect(status().isBadRequest());
        cadastrar(superAdmin, novoAdminJson(emailNovo(), cpfValido(), "Provisoria123", "MODERADOR"))
                .andExpect(status().isBadRequest());
    }
}
