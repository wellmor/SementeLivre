package com.sementelivre.backend.service;

import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Role;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.enums.PerfilEnum;
import com.sementelivre.backend.exception.ResourceNotFoundException;
import com.sementelivre.backend.repository.RefreshTokenRepository;
import com.sementelivre.backend.repository.RoleRepository;
import com.sementelivre.backend.repository.TokenRecuperacaoSenhaRepository;
import com.sementelivre.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Proprietario proprietario() {
        Proprietario proprietario = new Proprietario();
        proprietario.setId(UUID.randomUUID());
        proprietario.setNome("Teste Proprietario");
        proprietario.setEmail("prop@email.com");
        return proprietario;
    }

    @Test
    public void deveCriarContaComSenhaEmHashEPerfilDaPessoa() {
        Proprietario proprietario = proprietario();
        Role roleProprietario = Role.builder().nome(PerfilEnum.ROLE_PROPRIETARIO).build();
        when(roleRepository.findByNome(PerfilEnum.ROLE_PROPRIETARIO)).thenReturn(Optional.of(roleProprietario));
        when(passwordEncoder.encode("senhaSegura123")).thenReturn("hash-bcrypt-simulado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = usuarioService.criarConta(proprietario, "senhaSegura123", PerfilEnum.ROLE_PROPRIETARIO);

        assertThat(usuario.getPessoa()).isSameAs(proprietario);
        assertThat(usuario.getEmail()).isEqualTo("prop@email.com");
        assertThat(usuario.isAtivo()).isTrue();
        assertThat(usuario.getRoles()).containsExactly(roleProprietario);
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-bcrypt-simulado");
        // Reforço de legibilidade: deixa explícito que a senha em texto puro nunca é persistida.
        assertThat(usuario.getSenhaHash()).isNotEqualTo("senhaSegura123");
    }

    @Test
    public void deveFalharAoCriarContaQuandoPerfilNaoExiste() {
        when(roleRepository.findByNome(PerfilEnum.ROLE_ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.criarConta(proprietario(), "senha", PerfilEnum.ROLE_ADMIN))
                .isInstanceOf(IllegalStateException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    public void deveLancarNaoEncontradoQuandoContaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void deveRemoverTokensAntesDeExcluirConta() {
        Usuario usuario = new Usuario();
        usuario.setPessoa(proprietario());
        UUID id = usuario.getPessoa().getId();
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        usuarioService.excluir(id);

        InOrder ordem = inOrder(refreshTokenRepository, tokenRecuperacaoSenhaRepository, usuarioRepository);
        ordem.verify(refreshTokenRepository).deleteByUsuario(usuario);
        ordem.verify(tokenRecuperacaoSenhaRepository).deleteByUsuario(usuario);
        ordem.verify(usuarioRepository).delete(usuario);
    }

    @Test
    public void naoDeveFazerNadaAoExcluirContaDePessoaSemConta() {
        UUID pessoaId = UUID.randomUUID();
        when(usuarioRepository.findById(pessoaId)).thenReturn(Optional.empty());

        usuarioService.excluirContaDaPessoa(pessoaId);

        verify(usuarioRepository, never()).delete(any());
    }
}
