package com.example.demo.service;

import com.example.demo.dto.PessoaCreateDTO;
import com.example.demo.exception.DocumentoJaCadastradoException;
import com.example.demo.exception.EmailJaCadastradoException;
import com.example.demo.exception.PessoaNaoEncontradaException;
import com.example.demo.model.Admin;
import com.example.demo.model.NivelAcesso;
import com.example.demo.model.Pessoa;
import com.example.demo.model.Proprietario;
import com.example.demo.model.Usuario;
import com.example.demo.repository.PessoaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public PessoaService(PessoaRepository pessoaRepository, PasswordEncoder passwordEncoder) {
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Pessoa criar(PessoaCreateDTO dto) {
        if (pessoaRepository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException("Email já cadastrado no sistema.");
        }

        if (pessoaRepository.existsByDocumento(dto.getDocumento())) {
            throw new DocumentoJaCadastradoException("Documento já cadastrado no sistema.");
        }

        Pessoa novaPessoa;

        if ("PROPRIETARIO".equalsIgnoreCase(dto.getTipoPessoa())) {
            Proprietario p = new Proprietario();
            p.setRg(dto.getRg());
            novaPessoa = p;
        } else if ("ADMIN".equalsIgnoreCase(dto.getTipoPessoa())) {
            Admin a = new Admin();
            if (dto.getNivelAcesso() != null) {
                a.setNivelAcesso(NivelAcesso.valueOf(dto.getNivelAcesso()));
            }
            novaPessoa = a;
        } else {
            novaPessoa = new Usuario();
        }

        novaPessoa.setTipoDocumento(dto.getTipoDocumento());
        novaPessoa.setDocumento(dto.getDocumento());
        novaPessoa.setNome(dto.getNome());
        novaPessoa.setTelefone(dto.getTelefone());
        novaPessoa.setEmail(dto.getEmail());
        novaPessoa.setSenhaHash(passwordEncoder.encode(dto.getSenha()));

        return pessoaRepository.save(novaPessoa);
    }

    @Transactional(readOnly = true)
    public Pessoa buscarPorId(UUID id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new PessoaNaoEncontradaException("Pessoa não encontrada com o ID: " + id));
    }
}
