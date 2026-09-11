package com.sementelivre.backend.dto;

import com.sementelivre.backend.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {
    private UUID id;
    private String nome;
    private String email;
    private Set<String> roles;

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        Set<String> rolesStr = usuario.getRoles().stream()
                .map(r -> r.getNome().name())
                .collect(Collectors.toSet());
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .roles(rolesStr)
                .build();
    }
}
