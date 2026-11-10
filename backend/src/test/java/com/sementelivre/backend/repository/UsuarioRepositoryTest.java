package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import com.sementelivre.backend.integration.AbstractPostgresIntegrationTest;

@SpringBootTest
@Transactional
public class UsuarioRepositoryTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProprietarioRepository proprietarioRepository;

    @Test
    public void deveSalvarEBuscarContaPeloEmailDaPessoa() {
        Proprietario proprietario = new Proprietario();
        proprietario.setNome("Usuário Rep Test");
        proprietario.setDocumento("11122233344");
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setEmail("usurep@teste.com");
        proprietario.setRg("MG-USUREP");
        proprietarioRepository.save(proprietario);

        Usuario usuario = new Usuario();
        usuario.setPessoa(proprietario);
        usuario.setSenhaHash("hash123");

        Usuario salvo = usuarioRepository.save(usuario);

        // A conta usa o mesmo id da pessoa (pessoa_id e PK e FK)
        assertThat(salvo.getId()).isEqualTo(proprietario.getId());

        var doBanco = usuarioRepository.findByPessoaEmail("usurep@teste.com");
        assertThat(doBanco).isPresent();
        assertThat(doBanco.get().getNome()).isEqualTo("Usuário Rep Test");
        assertThat(doBanco.get().getPassword()).isEqualTo("hash123");
        assertThat(doBanco.get().getPessoa()).isInstanceOf(Proprietario.class);
    }
}
