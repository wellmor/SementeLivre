package com.example.demo.service;

import com.example.demo.dto.PessoaCreateDTO;
import com.example.demo.exception.DocumentoJaCadastradoException;
import com.example.demo.exception.EmailJaCadastradoException;
import com.example.demo.model.TipoDocumento;
import com.example.demo.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PessoaService pessoaService;

    @Test
    public void deveLancarExcecaoQuandoEmailDuplicado() {
        PessoaCreateDTO dto = new PessoaCreateDTO();
        dto.setEmail("teste@email.com");

        when(pessoaRepository.existsByEmail("teste@email.com")).thenReturn(true);

        assertThatThrownBy(() -> pessoaService.criar(dto))
                .isInstanceOf(EmailJaCadastradoException.class)
                .hasMessage("Email já cadastrado no sistema.");

        verify(pessoaRepository, never()).save(any());
        verify(pessoaRepository, never()).existsByDocumento(anyString());
    }

    @Test
    public void deveLancarExcecaoQuandoDocumentoDuplicado() {
        PessoaCreateDTO dto = new PessoaCreateDTO();
        dto.setEmail("novo@email.com");
        dto.setDocumento("12345678901");

        when(pessoaRepository.existsByEmail("novo@email.com")).thenReturn(false);
        when(pessoaRepository.existsByDocumento("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> pessoaService.criar(dto))
                .isInstanceOf(DocumentoJaCadastradoException.class)
                .hasMessage("Documento já cadastrado no sistema.");

        verify(pessoaRepository, never()).save(any());
    }
}
