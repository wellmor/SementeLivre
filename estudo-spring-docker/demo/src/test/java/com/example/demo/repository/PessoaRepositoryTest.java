package com.example.demo.repository;

import com.example.demo.model.Admin;
import com.example.demo.model.Pessoa;
import com.example.demo.model.Proprietario;
import com.example.demo.model.TipoDocumento;
import com.example.demo.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Garante uso do H2 se configurado com este profile
@Transactional
public class PessoaRepositoryTest {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void deveSalvarProprietarioComHerancaTpt() {
        // Arrange
        Proprietario proprietario = new Proprietario();
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setDocumento("12345678901");
        proprietario.setNome("Proprietario Teste");
        proprietario.setEmail("proprietario@teste.com");
        proprietario.setSenhaHash("hash123");
        proprietario.setRg("MG-123456");
        proprietario.setExibirNoSitePublico(true);

        // Act
        Proprietario salvo = pessoaRepository.save(proprietario);
        
        // Força flush pra garantir insert em ambas as tabelas (pessoa_t e proprietario_t)
        pessoaRepository.flush();

        // Assert
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getRg()).isEqualTo("MG-123456");
        
        // Busca do banco
        var doBanco = pessoaRepository.findById(salvo.getId());
        assertThat(doBanco).isPresent();
        assertThat(doBanco.get()).isInstanceOf(Proprietario.class);
        
        Proprietario recuperado = (Proprietario) doBanco.get();
        assertThat(recuperado.getRg()).isEqualTo("MG-123456");
        assertThat(recuperado.getEmail()).isEqualTo("proprietario@teste.com");

        // Asserção nativa no banco de dados para garantir que a herança TPT inseriu
        // o MESMO ID (pessoa_id) nas duas tabelas
        UUID uuidGerado = salvo.getId();
        
        Integer contagemPessoaT = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM pessoa_t WHERE id = ?", Integer.class, uuidGerado);
        assertThat(contagemPessoaT).isEqualTo(1);

        Integer contagemProprietarioT = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM proprietario_t WHERE pessoa_id = ?", Integer.class, uuidGerado);
        assertThat(contagemProprietarioT).isEqualTo(1);
    }

    @Test
    public void deveSalvarUsuarioComHerancaTpt() {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário Teste");
        usuario.setDocumento("11122233344");
        usuario.setTipoDocumento(TipoDocumento.CPF);
        usuario.setEmail("usuario@teste.com");
        usuario.setSenhaHash("hash123");

        Usuario salvo = pessoaRepository.save(usuario);
        pessoaRepository.flush();
        
        UUID uuidGerado = salvo.getId();
        
        Integer contagemPessoaT = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM pessoa_t WHERE id = ?", Integer.class, uuidGerado);
        assertThat(contagemPessoaT).isEqualTo(1);

        Integer contagemUsuarioT = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM usuario_t WHERE pessoa_id = ?", Integer.class, uuidGerado);
        assertThat(contagemUsuarioT).isEqualTo(1);
    }

    @Test
    public void deveSalvarAdminComHerancaTpt() {
        Admin admin = new Admin();
        admin.setNome("Admin Teste");
        admin.setDocumento("99988877766");
        admin.setTipoDocumento(TipoDocumento.CPF);
        admin.setEmail("admin@teste.com");
        admin.setSenhaHash("hash123");
        admin.setNivelAcesso(com.example.demo.model.NivelAcesso.SUPER_ADMIN);

        Admin salvo = pessoaRepository.save(admin);
        pessoaRepository.flush();
        
        UUID uuidGerado = salvo.getId();
        
        Integer contagemPessoaT = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM pessoa_t WHERE id = ?", Integer.class, uuidGerado);
        assertThat(contagemPessoaT).isEqualTo(1);

        Integer contagemAdminT = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM admin_t WHERE pessoa_id = ?", Integer.class, uuidGerado);
        assertThat(contagemAdminT).isEqualTo(1);
        
        String nivelAcesso = jdbcTemplate.queryForObject(
                "SELECT nivel_acesso FROM admin_t WHERE pessoa_id = ?", String.class, uuidGerado);
        assertThat(nivelAcesso).isEqualTo("SUPER_ADMIN");
    }
}
