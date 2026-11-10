package com.sementelivre.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.sementelivre.backend.BackendApplication;
import com.sementelivre.backend.dto.LoginRequestDTO;
import com.sementelivre.backend.dto.ProprietarioCreateRequestDTO;
import com.sementelivre.backend.dto.TokenResponseDTO;
import com.sementelivre.backend.dto.UsuarioResponseDTO;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.service.AuthService;
import com.sementelivre.backend.service.PessoaService;

import jakarta.persistence.EntityManager;

/**
 * Usuario como conta de login de Admin/Proprietario (migration V7), contra o
 * schema real do Flyway.
 */
@SpringBootTest(classes = BackendApplication.class)
class ContaUsuarioIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final String EMAIL = "cadastro.proprietario@teste.com";
    private static final String SENHA = "senhaSegura123";

    @Autowired
    private AuthService authService;

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ProprietarioCreateRequestDTO cadastroValido() {
        ProprietarioCreateRequestDTO dto = new ProprietarioCreateRequestDTO();
        dto.setNome("Proprietario Cadastro");
        dto.setTipoDocumento(TipoDocumento.CPF);
        dto.setDocumento("11144477735");
        dto.setEmail(EMAIL);
        dto.setSenha(SENHA);
        dto.setRg("MG-CADASTRO");
        return dto;
    }

    private int contar(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }

    @Test
    void senhaDeveFicarNaContaENaoNaPessoa() {
        int colunaEmPessoa = contar("SELECT count(*) FROM information_schema.columns "
                + "WHERE table_name = 'pessoa_t' AND column_name = 'senha_hash'");
        int colunaEmUsuario = contar("SELECT count(*) FROM information_schema.columns "
                + "WHERE table_name = 'usuario_t' AND column_name = 'senha_hash'");

        assertThat(colunaEmPessoa).isZero();
        assertThat(colunaEmUsuario).isEqualTo(1);
        assertThat(contar("SELECT count(*) FROM role_t WHERE nome = 'ROLE_USUARIO'")).isZero();
    }

    @Test
    void adminDoSeedDeveGanharContaComPerfilAdmin() {
        String sql = """
                SELECT count(*)
                FROM admin_t a
                JOIN pessoa_t p ON p.id = a.pessoa_id
                JOIN usuario_t u ON u.pessoa_id = a.pessoa_id
                JOIN usuario_role_t ur ON ur.usuario_id = u.pessoa_id
                JOIN role_t r ON r.id = ur.role_id
                WHERE p.email = 'admin@sementelivre.com.br'
                  AND r.nome = 'ROLE_ADMIN'
                  AND u.ativo
                  AND u.senha_hash IS NOT NULL
                """;

        assertThat(contar(sql)).isEqualTo(1);
    }

    @Test
    @Transactional
    void cadastroDeveCriarProprietarioComContaQueConsegueLogar() {
        UsuarioResponseDTO cadastrado = authService.cadastrar(cadastroValido());
        entityManager.flush();

        assertThat(cadastrado.getTipoPessoa()).isEqualTo("PROPRIETARIO");
        assertThat(cadastrado.getRoles()).containsExactly("ROLE_PROPRIETARIO");
        assertThat(contar("SELECT count(*) FROM proprietario_t WHERE pessoa_id = ?", cadastrado.getId()))
                .isEqualTo(1);

        TokenResponseDTO tokens = authService.login(new LoginRequestDTO(EMAIL, SENHA));

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
    }

    @Test
    @Transactional
    void excluirPessoaDeveRemoverContaETokens() {
        UUID id = authService.cadastrar(cadastroValido()).getId();
        authService.login(new LoginRequestDTO(EMAIL, SENHA));
        entityManager.flush();
        assertThat(contar("SELECT count(*) FROM refresh_token_t WHERE usuario_id = ?", id)).isEqualTo(1);

        pessoaService.deletar(id);
        entityManager.flush();

        assertThat(contar("SELECT count(*) FROM refresh_token_t WHERE usuario_id = ?", id)).isZero();
        assertThat(contar("SELECT count(*) FROM usuario_t WHERE pessoa_id = ?", id)).isZero();
        assertThat(contar("SELECT count(*) FROM pessoa_t WHERE id = ?", id)).isZero();
    }
}
