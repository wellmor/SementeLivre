package com.sementelivre.backend.controller;

import com.sementelivre.backend.dto.UsuarioResponseDTO;
import com.sementelivre.backend.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contas de login. A conta nasce junto com o proprietario (POST /auth/cadastrar
 * ou POST /api/proprietarios); os dados pessoais sao editados pelos endpoints
 * de pessoa/proprietario.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos().stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuarioService.buscarPorId(id)));
    }

    /** Remove so a conta de login; a pessoa continua cadastrada. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
