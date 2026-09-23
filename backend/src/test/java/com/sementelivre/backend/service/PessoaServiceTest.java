package com.sementelivre.backend.service;

import com.sementelivre.backend.dto.LogradouroDTO;
import com.sementelivre.backend.dto.PessoaUpdateRequestDTO;
import com.sementelivre.backend.exception.EmailJaCadastradoException;
import com.sementelivre.backend.exception.PessoaNaoEncontradaException;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Pessoa;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.repository.LogradouroRepository;
import com.sementelivre.backend.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PessoaServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private LogradouroRepository logradouroRepository;

    @InjectMocks
    private PessoaService pessoaService;

    private Usuario pessoaExistente(UUID id, String email) {
        Usuario pessoa = new Usuario();
        pessoa.setId(id);
        pessoa.setNome("Nome Antigo");
        pessoa.setEmail(email);
        return pessoa;
    }

    @Test
    public void deveLancarExcecaoQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(pessoaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pessoaService.buscarPorId(id))
                .isInstanceOf(PessoaNaoEncontradaException.class)
                .hasMessage("Pessoa não encontrada com o ID: " + id);
    }

    @Test
    public void deveLancarExcecaoAoAtualizarPessoaInexistente() {
        UUID id = UUID.randomUUID();
        when(pessoaRepository.findById(id)).thenReturn(Optional.empty());

        PessoaUpdateRequestDTO dto = new PessoaUpdateRequestDTO();
        dto.setNome("Nome Novo");
        dto.setEmail("novo@teste.com");

        assertThatThrownBy(() -> pessoaService.atualizar(id, dto))
                .isInstanceOf(PessoaNaoEncontradaException.class);
    }

    @Test
    public void deveAtualizarComSucessoQuandoEmailMudaParaUmDisponivel() {
        UUID id = UUID.randomUUID();
        Usuario pessoa = pessoaExistente(id, "antigo@teste.com");
        when(pessoaRepository.findById(id)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.existsByEmail("novo@teste.com")).thenReturn(false);
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LogradouroDTO endereco = new LogradouroDTO();
        endereco.setLogradouro("Rua Nova");
        endereco.setMunicipio("Cidade Nova");
        endereco.setUf("MG");

        PessoaUpdateRequestDTO dto = new PessoaUpdateRequestDTO();
        dto.setNome("Nome Novo");
        dto.setTelefone("32999998888");
        dto.setEmail("novo@teste.com");
        dto.setEndereco(endereco);

        Pessoa atualizado = pessoaService.atualizar(id, dto);

        assertThat(atualizado.getNome()).isEqualTo("Nome Novo");
        assertThat(atualizado.getTelefone()).isEqualTo("32999998888");
        assertThat(atualizado.getEmail()).isEqualTo("novo@teste.com");
        assertThat(atualizado.getLogradouro()).isNotNull();
        assertThat(atualizado.getLogradouro().getLogradouro()).isEqualTo("Rua Nova");
        verify(logradouroRepository).save(any(Logradouro.class));
    }

    @Test
    public void deveLancarConflitoAoAtualizarParaEmailDeOutraPessoa() {
        UUID id = UUID.randomUUID();
        Usuario pessoa = pessoaExistente(id, "antigo@teste.com");
        when(pessoaRepository.findById(id)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.existsByEmail("jaexiste@teste.com")).thenReturn(true);

        PessoaUpdateRequestDTO dto = new PessoaUpdateRequestDTO();
        dto.setNome("Nome Novo");
        dto.setEmail("jaexiste@teste.com");

        assertThatThrownBy(() -> pessoaService.atualizar(id, dto))
                .isInstanceOf(EmailJaCadastradoException.class);
    }

    @Test
    public void deveAtualizarSemConflitoQuandoEmailPermaneceOMesmo() {
        UUID id = UUID.randomUUID();
        Usuario pessoa = pessoaExistente(id, "mesmo@teste.com");
        when(pessoaRepository.findById(id)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PessoaUpdateRequestDTO dto = new PessoaUpdateRequestDTO();
        dto.setNome("Nome Atualizado");
        dto.setEmail("mesmo@teste.com");

        Pessoa atualizado = pessoaService.atualizar(id, dto);

        assertThat(atualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(atualizado.getEmail()).isEqualTo("mesmo@teste.com");
        verify(pessoaRepository, never()).existsByEmail(any());
    }
}
