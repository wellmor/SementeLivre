package com.sementelivre.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sementelivre.backend.dto.AdminCreateRequestDTO;
import com.sementelivre.backend.dto.AdminResponseDTO;
import com.sementelivre.backend.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Administração", description = "Rotas exclusivas do admin.")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Endpoint de confirmação de acesso administrativo.
     * Requer a autoridade ROLE_ADMIN — verificação dupla:
     * 1) Via @PreAuthorize (Method Security)
     * 2) Via SecurityConfig (.requestMatchers("/admin/**").hasRole("ADMIN"))
     */
    @GetMapping("/confirmacao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> confirmacaoAdmin() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "mensagem", "Acesso concedido: Usuário é Administrador"
        ));
    }

    @GetMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista os administradores (SUPER_ADMIN primeiro)")
    public List<AdminResponseDTO> listarAdministradores() {
        return adminService.listar();
    }

    @PostMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN') and @adminService.ehSuperAdmin(principal.id)")
    @Operation(summary = "Cadastra um administrador (só SUPER_ADMIN), com senha provisória")
    public ResponseEntity<AdminResponseDTO> criarAdministrador(@RequestBody @Valid AdminCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.criar(dto));
    }
}
