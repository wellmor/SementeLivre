package com.sementelivre.backend.service;

import com.sementelivre.backend.dto.LogradouroDTO;
import com.sementelivre.backend.dto.UsuarioCreateRequestDTO;
import com.sementelivre.backend.exception.DocumentoJaCadastradoException;
import com.sementelivre.backend.exception.EmailJaCadastradoException;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.repository.LogradouroRepository;
import com.sementelivre.backend.repository.PessoaRepository;
import com.sementelivre.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private LogradouroRepository logradouroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioCreateRequestDTO dtoValido() {
        UsuarioCreateRequestDTO dto = new UsuarioCreateRequestDTO();
        dto.setNome("Teste Usuario");
        dto.setTipoDocumento(TipoDocumento.CPF);
        dto.setDocumento("52998224725");
        dto.setEmail("teste@email.com");
        dto.setSenha("senhaSegura123");
        return dto;
    }

    @Test
    public void deveLancarConflitoQuandoEmailDuplicado() {
        UsuarioCreateRequestDTO dto = dtoValido();
        when(pessoaRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(dto))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    public void deveLancarConflitoQuandoDocumentoDuplicado() {
        UsuarioCreateRequestDTO dto = dtoValido();
        when(pessoaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(pessoaRepository.existsByDocumento(dto.getDocumento())).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(dto))
                .isInstanceOf(DocumentoJaCadastradoException.class);
    }

    @Test
    public void deveRejeitarCpfComDigitoVerificadorInvalido() {
        UsuarioCreateRequestDTO dto = dtoValido();
        dto.setDocumento("52998224726");

        assertThatThrownBy(() -> usuarioService.criar(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void deveSalvarSenhaComHashBcryptAoCriar() {
        UsuarioCreateRequestDTO dto = dtoValido();
        when(pessoaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(pessoaRepository.existsByDocumento(dto.getDocumento())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("hash-bcrypt-simulado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = usuarioService.criar(dto);

        verify(passwordEncoder).encode(dto.getSenha());
        assertThat(usuario.getSenhaHash()).isEqualTo("hash-bcrypt-simulado");
        // Reforço de legibilidade: deixa explícito que a senha em texto puro nunca é persistida.
        assertThat(usuario.getSenhaHash()).isNotEqualTo(dto.getSenha());
    }

    @Test
    public void deveCriarUsuarioComSucessoComTodosOsCampos() {
        UsuarioCreateRequestDTO dto = dtoValido();
        dto.setTelefone("(32) 99999-0000");
        LogradouroDTO endereco = new LogradouroDTO();
        endereco.setLogradouro("Rua A");
        endereco.setMunicipio("Cidade");
        endereco.setUf("MG");
        dto.setEndereco(endereco);

        when(pessoaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(pessoaRepository.existsByDocumento(dto.getDocumento())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("hash-bcrypt-simulado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = usuarioService.criar(dto);

        assertThat(usuario.getNome()).isEqualTo(dto.getNome());
        assertThat(usuario.getTipoDocumento()).isEqualTo(TipoDocumento.CPF);
        assertThat(usuario.getDocumento()).isEqualTo(dto.getDocumento());
        assertThat(usuario.getTelefone()).isEqualTo(dto.getTelefone());
        assertThat(usuario.getEmail()).isEqualTo(dto.getEmail());
        assertThat(usuario.getLogradouro()).isNotNull();
        assertThat(usuario.getLogradouro().getLogradouro()).isEqualTo("Rua A");
        verify(logradouroRepository).save(any(Logradouro.class));
    }
}
