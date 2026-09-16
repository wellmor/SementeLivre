package com.example.demo.service;

import com.example.demo.dto.LogradouroDTO;
import com.example.demo.dto.ProprietarioCreateRequestDTO;
import com.example.demo.exception.DocumentoJaCadastradoException;
import com.example.demo.exception.EmailJaCadastradoException;
import com.example.demo.exception.RgJaCadastradoException;
import com.example.demo.model.Logradouro;
import com.example.demo.model.Proprietario;
import com.example.demo.model.TipoDocumento;
import com.example.demo.repository.LogradouroRepository;
import com.example.demo.repository.PessoaRepository;
import com.example.demo.repository.ProprietarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProprietarioServiceTest {

    @Mock
    private ProprietarioRepository proprietarioRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private LogradouroRepository logradouroRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProprietarioService proprietarioService;

    private ProprietarioCreateRequestDTO dtoValido() {
        ProprietarioCreateRequestDTO dto = new ProprietarioCreateRequestDTO();
        dto.setNome("Teste Proprietario");
        dto.setTipoDocumento(TipoDocumento.CPF);
        dto.setDocumento("52998224725");
        dto.setEmail("prop@email.com");
        dto.setSenha("senhaSegura123");
        dto.setRg("MG-123456");
        return dto;
    }

    @Test
    public void deveLancarConflitoQuandoEmailDuplicado() {
        ProprietarioCreateRequestDTO dto = dtoValido();
        when(pessoaRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> proprietarioService.criar(dto))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    public void deveLancarConflitoQuandoDocumentoDuplicado() {
        ProprietarioCreateRequestDTO dto = dtoValido();
        when(pessoaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(pessoaRepository.existsByDocumento(dto.getDocumento())).thenReturn(true);

        assertThatThrownBy(() -> proprietarioService.criar(dto))
                .isInstanceOf(DocumentoJaCadastradoException.class);
    }

    @Test
    public void deveLancarConflitoQuandoRgDuplicado() {
        ProprietarioCreateRequestDTO dto = dtoValido();
        lenient().when(pessoaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        lenient().when(pessoaRepository.existsByDocumento(dto.getDocumento())).thenReturn(false);
        when(proprietarioRepository.existsByRg(dto.getRg())).thenReturn(true);

        assertThatThrownBy(() -> proprietarioService.criar(dto))
                .isInstanceOf(RgJaCadastradoException.class)
                .hasMessageContaining("RG");
    }

    @Test
    public void deveRejeitarDocumentoComDigitoVerificadorInvalido() {
        ProprietarioCreateRequestDTO dto = dtoValido();
        dto.setDocumento("52998224726");

        assertThatThrownBy(() -> proprietarioService.criar(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void deveCriarProprietarioComSucessoComTodosOsCampos() {
        ProprietarioCreateRequestDTO dto = dtoValido();
        dto.setTelefone("(32) 99999-0000");
        dto.setExibirNoSitePublico(true);
        LogradouroDTO endereco = new LogradouroDTO();
        endereco.setLogradouro("Rua A");
        endereco.setMunicipio("Cidade");
        endereco.setUf("MG");
        dto.setEndereco(endereco);

        when(pessoaRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(pessoaRepository.existsByDocumento(dto.getDocumento())).thenReturn(false);
        when(proprietarioRepository.existsByRg(dto.getRg())).thenReturn(false);
        when(passwordEncoder.encode(dto.getSenha())).thenReturn("hash-bcrypt-simulado");
        when(proprietarioRepository.save(any(Proprietario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Proprietario proprietario = proprietarioService.criar(dto);

        assertThat(proprietario.getNome()).isEqualTo(dto.getNome());
        assertThat(proprietario.getTipoDocumento()).isEqualTo(TipoDocumento.CPF);
        assertThat(proprietario.getDocumento()).isEqualTo(dto.getDocumento());
        assertThat(proprietario.getTelefone()).isEqualTo(dto.getTelefone());
        assertThat(proprietario.getEmail()).isEqualTo(dto.getEmail());
        assertThat(proprietario.getRg()).isEqualTo(dto.getRg());
        assertThat(proprietario.isExibirNoSitePublico()).isTrue();
        assertThat(proprietario.getSenhaHash()).isEqualTo("hash-bcrypt-simulado");
        // Reforço de legibilidade: deixa explícito que a senha em texto puro nunca é persistida.
        assertThat(proprietario.getSenhaHash()).isNotEqualTo(dto.getSenha());
        assertThat(proprietario.getLogradouro()).isNotNull();
        assertThat(proprietario.getLogradouro().getLogradouro()).isEqualTo("Rua A");
        verify(logradouroRepository).save(any(Logradouro.class));
    }
}
