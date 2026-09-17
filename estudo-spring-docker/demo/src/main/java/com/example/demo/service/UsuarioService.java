package com.example.demo.service;

import com.example.demo.dto.UsuarioCreateRequestDTO;
import com.example.demo.exception.DocumentoJaCadastradoException;
import com.example.demo.exception.EmailJaCadastradoException;
import com.example.demo.model.Logradouro;
import com.example.demo.model.Usuario;
import com.example.demo.repository.LogradouroRepository;
import com.example.demo.repository.PessoaRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.validation.DocumentoValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final LogradouroRepository logradouroRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PessoaRepository pessoaRepository,
                          LogradouroRepository logradouroRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.pessoaRepository = pessoaRepository;
        this.logradouroRepository = logradouroRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario criar(UsuarioCreateRequestDTO dto) {
        DocumentoValidator.validar(dto.getTipoDocumento(), dto.getDocumento());

        if (pessoaRepository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException("E-mail já cadastrado no sistema.");
        }

        if (pessoaRepository.existsByDocumento(dto.getDocumento())) {
            throw new DocumentoJaCadastradoException("Documento já cadastrado no sistema.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setTipoDocumento(dto.getTipoDocumento());
        usuario.setDocumento(dto.getDocumento());
        usuario.setTelefone(dto.getTelefone());
        usuario.setEmail(dto.getEmail());
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));

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
            usuario.setLogradouro(logradouro);
        }

        return usuarioRepository.save(usuario);
    }
}
