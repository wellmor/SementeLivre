package com.sementelivre.backend.service;

import com.sementelivre.backend.dto.UsuarioCreateRequestDTO;
import com.sementelivre.backend.exception.DocumentoJaCadastradoException;
import com.sementelivre.backend.exception.EmailJaCadastradoException;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.repository.LogradouroRepository;
import com.sementelivre.backend.repository.PessoaRepository;
import com.sementelivre.backend.repository.UsuarioRepository;
import com.sementelivre.backend.validation.DocumentoValidator;
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
