package com.sementelivre.backend.integration;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import com.jayway.jsonpath.JsonPath;
import com.sementelivre.backend.BackendApplication;
import com.sementelivre.backend.entity.Admin;
import com.sementelivre.backend.entity.enums.PerfilEnum;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.repository.PessoaRepository;
import com.sementelivre.backend.service.UsuarioService;

/**
 * Fluxo do proprietario usado pelo front-app (issue #99), passando pela cadeia de
 * seguranca de verdade: cadastro, login, /auth/me, edicao do proprio perfil e troca
 * de senha. Cada teste cria seus proprios proprietarios (e-mail/CPF/RG unicos), porque
 * as requisicoes do MockMvc commitam no Postgres compartilhado.
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
class PerfilProprietarioIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String SENHA = "Senha1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private record Conta(String id, String email, String documento, String rg) {}

    /** Gera um CPF com digitos verificadores validos. */
    private static String cpfValido() {
        int[] d = new int[11];
        for (int i = 0; i < 9; i++) {
            d[i] = ThreadLocalRandom.current().nextInt(10);
        }
        d[0] = 1 + ThreadLocalRandom.current().nextInt(9); // evita todos os digitos iguais
        for (int pos = 9; pos < 11; pos++) {
            int soma = 0;
            for (int i = 0; i < pos; i++) {
                soma += d[i] * (pos + 1 - i);
            }
            int resto = soma % 11;
            d[pos] = resto < 2 ? 0 : 11 - resto;
        }
        StringBuilder cpf = new StringBuilder();
        for (int digito : d) {
            cpf.append(digito);
        }
        return cpf.toString();
    }

    private static String cadastroJson(String email, String documento, String rg) {
        return """
                {
                  "nome": "Proprietario Perfil",
                  "tipoDocumento": "CPF",
                  "documento": "%s",
                  "rg": "%s",
                  "telefone": "32999998888",
                  "email": "%s",
                  "senha": "%s",
                  "endereco": {
                    "logradouro": "Rua das Sementes",
                    "numero": "10",
                    "bairro": "Centro",
                    "municipio": "Rio Pomba",
                    "uf": "MG",
                    "cep": "36180000"
                  }
                }
                """.formatted(documento, rg, email, SENHA);
    }

    private Conta cadastrar() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        String email = "perfil-" + sufixo + "@teste.com";
        String documento = cpfValido();
        String rg = "MG-" + sufixo;

        String resposta = mockMvc.perform(post("/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(email, documento, rg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return new Conta(JsonPath.read(resposta, "$.id"), email, documento, rg);
    }

    private String login(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.accessToken");
    }

    /** Cria um admin com conta de login (o admin do seed nao tem senha conhecida) e devolve o token. */
    private String loginAdmin() throws Exception {
        String email = "admin-" + UUID.randomUUID().toString().substring(0, 8) + "@teste.com";
        transactionTemplate.executeWithoutResult(status -> {
            Admin admin = new Admin();
            admin.setTipoDocumento(TipoDocumento.CPF);
            admin.setDocumento(cpfValido());
            admin.setNome("Admin Teste");
            admin.setEmail(email);
            usuarioService.criarConta(pessoaRepository.save(admin), SENHA, PerfilEnum.ROLE_ADMIN);
        });
        return login(email, SENHA);
    }

    private static String atualizacaoJson(String nome, String email) {
        return """
                {
                  "nome": "%s",
                  "telefone": "32988887777",
                  "email": "%s",
                  "endereco": {
                    "logradouro": "Rua Nova",
                    "numero": "20",
                    "municipio": "Mercês",
                    "uf": "MG",
                    "cep": "36190000"
                  }
                }
                """.formatted(nome, email);
    }

    @Test
    void meDeveRetornarPerfilCompletoDoProprietario() throws Exception {
        Conta conta = cadastrar();
        String token = login(conta.email(), SENHA);

        mockMvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(conta.id()))
                .andExpect(jsonPath("$.tipoPessoa").value("PROPRIETARIO"))
                .andExpect(jsonPath("$.documento").value(conta.documento()))
                .andExpect(jsonPath("$.rg").value(conta.rg()))
                .andExpect(jsonPath("$.exibirNoSitePublico").value(false))
                .andExpect(jsonPath("$.endereco.municipio").value("Rio Pomba"))
                .andExpect(jsonPath("$.endereco.cep").value("36180000"))
                .andExpect(jsonPath("$.roles", contains("ROLE_PROPRIETARIO")))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void semTokenOuComCredencialErradaDeveRetornar401() throws Exception {
        Conta conta = cadastrar();

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"errada\"}".formatted(conta.email())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastroDuplicadoDeveIndicarOCampoQueColidiu() throws Exception {
        Conta existente = cadastrar();

        mockMvc.perform(post("/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson("outro-" + existente.email(), existente.documento(), "RG-" + UUID.randomUUID().toString().substring(0, 8))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors", contains("documento: Documento já cadastrado no sistema.")));

        mockMvc.perform(post("/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cadastroJson(existente.email(), cpfValido(), "RG-" + UUID.randomUUID().toString().substring(0, 8))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors", contains("email: E-mail já cadastrado no sistema.")));
    }

    @Test
    void proprietarioDeveEditarOsPropriosDadosMasNaoOsDeOutro() throws Exception {
        Conta maria = cadastrar();
        Conta joao = cadastrar();
        String tokenMaria = login(maria.email(), SENHA);

        mockMvc.perform(put("/api/proprietarios/" + maria.id())
                        .header(HttpHeaders.AUTHORIZATION, tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacaoJson("Maria Editada", maria.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Editada"))
                .andExpect(jsonPath("$.endereco.municipio").value("Mercês"));

        mockMvc.perform(get("/auth/me").header(HttpHeaders.AUTHORIZATION, tokenMaria))
                .andExpect(jsonPath("$.nome").value("Maria Editada"))
                .andExpect(jsonPath("$.telefone").value("32988887777"))
                .andExpect(jsonPath("$.endereco.logradouro").value("Rua Nova"));

        mockMvc.perform(put("/api/proprietarios/" + joao.id())
                        .header(HttpHeaders.AUTHORIZATION, tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacaoJson("Joao Alterado", joao.email())))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/pessoas/" + joao.id())
                        .header(HttpHeaders.AUTHORIZATION, tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacaoJson("Joao Alterado", joao.email())))
                .andExpect(status().isForbidden());
    }

    @Test
    void edicaoComEmailDeOutroProprietarioDeveIndicarCampoEmail() throws Exception {
        Conta maria = cadastrar();
        Conta joao = cadastrar();
        String tokenMaria = login(maria.email(), SENHA);

        mockMvc.perform(put("/api/proprietarios/" + maria.id())
                        .header(HttpHeaders.AUTHORIZATION, tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(atualizacaoJson("Maria", joao.email())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors", contains("email: Email já cadastrado no sistema.")));
    }

    @Test
    void alterarSenhaDeveConferirASenhaAtual() throws Exception {
        Conta conta = cadastrar();
        String token = login(conta.email(), SENHA);

        mockMvc.perform(post("/auth/alterar-senha")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"errada\",\"novaSenha\":\"NovaSenha123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", contains("senhaAtual: Senha atual incorreta.")));

        // senha antiga continua valendo depois da tentativa com a senha atual errada
        login(conta.email(), SENHA);

        mockMvc.perform(post("/auth/alterar-senha")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"%s\",\"novaSenha\":\"NovaSenha123\"}".formatted(SENHA)))
                .andExpect(status().isOk());

        login(conta.email(), "NovaSenha123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(conta.email(), SENHA)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void alterarSenhaDeveExigirAutenticacaoENovaSenhaValida() throws Exception {
        mockMvc.perform(post("/auth/alterar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"%s\",\"novaSenha\":\"NovaSenha123\"}".formatted(SENHA)))
                .andExpect(status().isUnauthorized());

        Conta conta = cadastrar();
        String token = login(conta.email(), SENHA);

        mockMvc.perform(post("/auth/alterar-senha")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"%s\",\"novaSenha\":\"curta\"}".formatted(SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors", contains("novaSenha: A nova senha deve ter entre 8 e 72 caracteres")));
    }

    @Test
    void soAdminDeveExcluirPessoasEContas() throws Exception {
        Conta maria = cadastrar();
        Conta joao = cadastrar();
        Conta ana = cadastrar();
        String tokenMaria = login(maria.email(), SENHA);

        for (String rota : List.of("/api/proprietarios/", "/api/pessoas/", "/api/usuarios/")) {
            mockMvc.perform(delete(rota + joao.id()).header(HttpHeaders.AUTHORIZATION, tokenMaria))
                    .andExpect(status().isForbidden());
        }
        // nem a propria conta: exclusao e so pelo admin
        mockMvc.perform(delete("/api/proprietarios/" + maria.id()).header(HttpHeaders.AUTHORIZATION, tokenMaria))
                .andExpect(status().isForbidden());
        login(joao.email(), SENHA); // continua existindo e logando

        String tokenAdmin = loginAdmin();

        // /api/usuarios remove so a conta de login: a pessoa continua, mas nao loga mais
        mockMvc.perform(delete("/api/usuarios/" + joao.id()).header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(joao.email(), SENHA)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/proprietarios/" + joao.id()).header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/pessoas/" + ana.id()).header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/pessoas/" + ana.id()).header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/proprietarios/" + maria.id()).header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/proprietarios/" + maria.id()).header(HttpHeaders.AUTHORIZATION, tokenAdmin))
                .andExpect(status().isNotFound());
    }
}
