package com.sementelivre.backend.controller;

import com.sementelivre.backend.dto.*;
import com.sementelivre.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody @Valid CadastroRequestDTO dto) {
        UsuarioResponseDTO usuarioCriado = authService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCriado);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        TokenResponseDTO tokenResponse = authService.login(dto);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO dto) {
        TokenResponseDTO tokenResponse = authService.refreshToken(dto);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Map<String, String>> solicitarRecuperacaoSenha(@RequestBody @Valid SolicitarRecuperacaoDTO dto) {
        authService.solicitarRecuperacaoSenha(dto);
        return ResponseEntity.ok(Map.of("mensagem", "Se o e-mail estiver cadastrado, as instruções foram enviadas."));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody @Valid RedefinirSenhaDTO dto) {
        authService.redefinirSenha(dto);
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso."));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obterPerfilAtual(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.sementelivre.backend.entity.Usuario usuarioAutenticado) {
        if (usuarioAutenticado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuarioAutenticado));
    }
}
