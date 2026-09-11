package com.estudo.auth.controller;

import com.estudo.auth.dto.CadastroRequest;
import com.estudo.auth.dto.LoginRequest;
import com.estudo.auth.dto.LoginResponse;
import com.estudo.auth.model.Usuario;
import com.estudo.auth.service.JwtService;
import com.estudo.auth.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<Map<String, String>> cadastrar(@RequestBody CadastroRequest request) {
        String role = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().toUpperCase()
                : "PROPRIETARIO";

        if (!role.equals("PROPRIETARIO") && !role.equals("ADMIN")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Role inválida. Use PROPRIETARIO ou ADMIN."));
        }

        usuarioService.cadastrar(request.getEmail(), request.getSenha(), role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", "Usuário cadastrado com sucesso"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Usuario usuario = usuarioService.buscarPorEmail(request.getEmail());

            if (!usuarioService.senhaCorreta(request.getSenha(), usuario.getSenha())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Credenciais inválidas"));
            }

            String token = jwtService.gerarToken(usuario.getEmail(), usuario.getRole());
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Credenciais inválidas"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.buscarPorEmail(email);
        return ResponseEntity.ok(Map.of(
                "email", usuario.getEmail(),
                "role", usuario.getRole()
        ));
    }
}
