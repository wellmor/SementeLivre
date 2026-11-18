package com.sementelivre.backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sementelivre.backend.dto.AdminCreateRequestDTO;
import com.sementelivre.backend.dto.AdminResponseDTO;
import com.sementelivre.backend.entity.Admin;
import com.sementelivre.backend.entity.Pessoa;
import com.sementelivre.backend.entity.enums.NivelAcesso;
import com.sementelivre.backend.entity.enums.PerfilEnum;
import com.sementelivre.backend.exception.DocumentoJaCadastradoException;
import com.sementelivre.backend.exception.EmailJaCadastradoException;
import com.sementelivre.backend.repository.AdminRepository;
import com.sementelivre.backend.repository.PessoaRepository;
import com.sementelivre.backend.util.DocumentoValidator;

/**
 * Administradores. Qualquer admin vê a lista; só o SUPER_ADMIN cadastra
 * outros (o admin principal do seed é SUPER_ADMIN).
 */
@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final PessoaRepository pessoaRepository;
    private final UsuarioService usuarioService;

    public AdminService(AdminRepository adminRepository, PessoaRepository pessoaRepository,
                        UsuarioService usuarioService) {
        this.adminRepository = adminRepository;
        this.pessoaRepository = pessoaRepository;
        this.usuarioService = usuarioService;
    }

    /** Usado no @PreAuthorize do controller. */
    @Transactional(readOnly = true)
    public boolean ehSuperAdmin(UUID pessoaId) {
        return pessoaId != null && adminRepository.findById(pessoaId)
                .map(a -> a.getNivelAcesso() == NivelAcesso.SUPER_ADMIN)
                .orElse(false);
    }

    /** SUPER_ADMIN primeiro, depois por nome. */
    @Transactional(readOnly = true)
    public List<AdminResponseDTO> listar() {
        return adminRepository.findAll().stream()
                .sorted(Comparator.comparing((Admin a) -> a.getNivelAcesso() != NivelAcesso.SUPER_ADMIN)
                        .thenComparing(a -> a.getNome().toLowerCase()))
                .map(AdminResponseDTO::de)
                .toList();
    }

    @Transactional
    public AdminResponseDTO criar(AdminCreateRequestDTO dto) {
        NivelAcesso nivel = dto.nivelAcesso() == null ? NivelAcesso.ADMIN : dto.nivelAcesso();
        if (nivel == NivelAcesso.MODERADOR) {
            throw new IllegalArgumentException("Nível de acesso inválido: use ADMIN ou SUPER_ADMIN.");
        }

        String documento = dto.documento().replaceAll("\\D", "");
        DocumentoValidator.validar(dto.tipoDocumento(), documento);

        if (pessoaRepository.existsByEmailIgnoreCase(Pessoa.normalizarEmail(dto.email()))) {
            throw new EmailJaCadastradoException("E-mail já cadastrado no sistema.");
        }
        if (pessoaRepository.existsByDocumento(documento)) {
            throw new DocumentoJaCadastradoException("Documento já cadastrado no sistema.");
        }

        Admin admin = new Admin();
        admin.setNome(dto.nome().trim());
        admin.setEmail(dto.email());
        admin.setTipoDocumento(dto.tipoDocumento());
        admin.setDocumento(documento);
        admin.setNivelAcesso(nivel);

        Admin salvo = adminRepository.save(admin);
        usuarioService.criarConta(salvo, dto.senha(), PerfilEnum.ROLE_ADMIN);
        return AdminResponseDTO.de(salvo);
    }
}
