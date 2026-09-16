package com.example.demo.service;

import com.example.demo.dto.ProprietarioCreateRequestDTO;
import com.example.demo.exception.DocumentoJaCadastradoException;
import com.example.demo.exception.EmailJaCadastradoException;
import com.example.demo.exception.RgJaCadastradoException;
import com.example.demo.model.Logradouro;
import com.example.demo.model.Proprietario;
import com.example.demo.repository.LogradouroRepository;
import com.example.demo.repository.PessoaRepository;
import com.example.demo.repository.ProprietarioRepository;
import com.example.demo.validation.DocumentoValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProprietarioService {

    private final ProprietarioRepository proprietarioRepository;
    private final PessoaRepository pessoaRepository;
    private final LogradouroRepository logradouroRepository;
    private final PasswordEncoder passwordEncoder;

    public ProprietarioService(ProprietarioRepository proprietarioRepository, PessoaRepository pessoaRepository,
                               LogradouroRepository logradouroRepository, PasswordEncoder passwordEncoder) {
        this.proprietarioRepository = proprietarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.logradouroRepository = logradouroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Proprietario criar(ProprietarioCreateRequestDTO dto) {
        DocumentoValidator.validar(dto.getTipoDocumento(), dto.getDocumento());

        if (pessoaRepository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException("E-mail já cadastrado no sistema.");
        }

        if (pessoaRepository.existsByDocumento(dto.getDocumento())) {
            throw new DocumentoJaCadastradoException("Documento já cadastrado no sistema.");
        }

        if (proprietarioRepository.existsByRg(dto.getRg())) {
            throw new RgJaCadastradoException("RG já cadastrado no sistema.");
        }

        Proprietario proprietario = new Proprietario();
        proprietario.setNome(dto.getNome());
        proprietario.setTipoDocumento(dto.getTipoDocumento());
        proprietario.setDocumento(dto.getDocumento());
        proprietario.setTelefone(dto.getTelefone());
        proprietario.setEmail(dto.getEmail());
        proprietario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
        
        proprietario.setRg(dto.getRg());
        if (dto.getExibirNoSitePublico() != null) {
            proprietario.setExibirNoSitePublico(dto.getExibirNoSitePublico());
        }

        if (dto.getEndereco() != null) {
            Logradouro logradouro = new Logradouro();
            logradouro.setLogradouro(dto.getEndereco().getLogradouro());
            logradouro.setNumero(dto.getEndereco().getNumero());
            logradouro.setComplemento(dto.getEndereco().getComplemento());
            logradouro.setBairro(dto.getEndereco().getBairro());
            logradouro.setMunicipio(dto.getEndereco().getMunicipio());
            logradouro.setUf(dto.getEndereco().getUf());
            logradouro.setCep(dto.getEndereco().getCep());

            logradouroRepository.save(logradouro);
            proprietario.setLogradouro(logradouro);
        }

        return proprietarioRepository.save(proprietario);
    }
}
