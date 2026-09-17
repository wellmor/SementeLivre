package com.sementelivre.backend.service;

import com.sementelivre.backend.dto.LogradouroDTO;
import com.sementelivre.backend.dto.PessoaUpdateRequestDTO;
import com.sementelivre.backend.exception.EmailJaCadastradoException;
import com.sementelivre.backend.exception.PessoaNaoEncontradaException;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Pessoa;
import com.sementelivre.backend.repository.LogradouroRepository;
import com.sementelivre.backend.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final LogradouroRepository logradouroRepository;

    public PessoaService(PessoaRepository pessoaRepository, LogradouroRepository logradouroRepository) {
        this.pessoaRepository = pessoaRepository;
        this.logradouroRepository = logradouroRepository;
    }

    @Transactional(readOnly = true)
    public List<Pessoa> listarTodas() {
        return pessoaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pessoa buscarPorId(UUID id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new PessoaNaoEncontradaException("Pessoa não encontrada com o ID: " + id));
    }

    @Transactional
    public Pessoa atualizar(UUID id, PessoaUpdateRequestDTO dto) {
        Pessoa pessoa = buscarPorId(id);

        if (!pessoa.getEmail().equalsIgnoreCase(dto.getEmail()) && pessoaRepository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException("Email já cadastrado no sistema.");
        }

        pessoa.setNome(dto.getNome());
        pessoa.setTelefone(dto.getTelefone());
        pessoa.setEmail(dto.getEmail());

        if (dto.getEndereco() != null) {
            Logradouro logradouro = pessoa.getLogradouro();
            if (logradouro == null) {
                logradouro = new Logradouro();
            }
            mapLogradouro(dto.getEndereco(), logradouro);
            logradouroRepository.save(logradouro);
            pessoa.setLogradouro(logradouro);
        }

        return pessoaRepository.save(pessoa);
    }

    @Transactional
    public void deletar(UUID id) {
        Pessoa pessoa = buscarPorId(id);
        pessoaRepository.delete(pessoa);
    }

    private void mapLogradouro(LogradouroDTO dto, Logradouro logradouro) {
        logradouro.setLogradouro(dto.getLogradouro());
        logradouro.setNumero(dto.getNumero());
        logradouro.setComplemento(dto.getComplemento());
        logradouro.setBairro(dto.getBairro());
        logradouro.setMunicipio(dto.getMunicipio());
        logradouro.setUf(dto.getUf());
        logradouro.setCep(dto.getCep());
    }
}
