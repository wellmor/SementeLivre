package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void deveSalvarEBuscarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário Rep Test");
        usuario.setDocumento("11122233344");
        usuario.setTipoDocumento(TipoDocumento.CPF);
        usuario.setEmail("usurep@teste.com");
        usuario.setSenhaHash("hash123");

        Usuario salvo = usuarioRepository.save(usuario);

        assertThat(salvo.getId()).isNotNull();

        var doBanco = usuarioRepository.findById(salvo.getId());
        assertThat(doBanco).isPresent();
        assertThat(doBanco.get().getNome()).isEqualTo("Usuário Rep Test");
        assertThat(doBanco.get().getEmail()).isEqualTo("usurep@teste.com");
    }
}
