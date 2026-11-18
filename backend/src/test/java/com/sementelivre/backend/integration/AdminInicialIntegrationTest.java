package com.sementelivre.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import com.sementelivre.backend.config.AdminInicial;
import com.sementelivre.backend.repository.PessoaRepository;
import com.sementelivre.backend.repository.UsuarioRepository;

/**
 * Admin principal do seed: nasce sem senha (V14, o hash do seed é público) e
 * recebe a de ADMIN_INICIAL_SENHA uma vez só.
 */
class AdminInicialIntegrationTest extends AbstractAuthIntegrationTest {

    private static final String EMAIL_DO_SEED = "admin@sementelivre.com.br";
    private static final String ADMIN_DO_SEED =
            "(SELECT id FROM pessoa_t WHERE tipo_documento = 'CPF' AND documento = '00000000000')";

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PessoaRepository pessoaRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private JdbcTemplate jdbcTemplate;

    /** O admin do seed volta ao estado da V14 para não vazar para os outros testes. */
    @AfterEach
    void voltaAoEstadoDoSeed() {
        jdbcTemplate.update("UPDATE usuario_t SET senha_hash = ? WHERE pessoa_id = " + ADMIN_DO_SEED,
                AdminInicial.SENHA_NAO_DEFINIDA);
        jdbcTemplate.update("UPDATE pessoa_t SET email = ? WHERE id = " + ADMIN_DO_SEED, EMAIL_DO_SEED);
    }

    private void subirCom(String email, String senha) {
        new AdminInicial(usuarioRepository, pessoaRepository, passwordEncoder, transactionTemplate, email, senha)
                .run(null);
    }

    private String hashDoSeed() {
        return jdbcTemplate.queryForObject(
                "SELECT senha_hash FROM usuario_t WHERE pessoa_id = " + ADMIN_DO_SEED, String.class);
    }

    private void loginRecusado(String email, String senha) throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginJson(email, senha)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void adminDoSeedNasceSemSenhaUtilizavel() throws Exception {
        assertThat(hashDoSeed()).isEqualTo(AdminInicial.SENHA_NAO_DEFINIDA);
        loginRecusado(EMAIL_DO_SEED, "SENHA_NAO_DEFINIDA");
    }

    @Test
    void semAdminInicialSenhaContinuaSemSenha() {
        subirCom("", "");
        assertThat(hashDoSeed()).isEqualTo(AdminInicial.SENHA_NAO_DEFINIDA);
    }

    @Test
    void senhaDoAmbienteValeSoNaPrimeiraVez() throws Exception {
        subirCom("", "RaizForte123");
        login(EMAIL_DO_SEED, "RaizForte123");

        // Próximo deploy com outra senha no .env: a que já existe não é trocada
        subirCom("", "OutraSenha456");
        loginRecusado(EMAIL_DO_SEED, "OutraSenha456");
        login(EMAIL_DO_SEED, "RaizForte123");
    }

    @Test
    void emailDoAmbienteViraOLoginDoAdmin() throws Exception {
        String email = "raiz-" + sufixoUnico() + "@teste.com";
        subirCom(email, "RaizForte123");

        login(email, "RaizForte123");
        loginRecusado(EMAIL_DO_SEED, "RaizForte123");
    }

    @Test
    void configuracaoInvalidaImpedeOBackendDeSubir() throws Exception {
        assertThatThrownBy(() -> subirCom("", "curta"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("8 e 72");

        Conta produtor = cadastrar();
        assertThatThrownBy(() -> subirCom(produtor.email(), "RaizForte123"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("já é usado");

        assertThat(hashDoSeed()).isEqualTo(AdminInicial.SENHA_NAO_DEFINIDA);
    }
}
