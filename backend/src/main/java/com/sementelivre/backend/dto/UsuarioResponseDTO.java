package com.sementelivre.backend.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class UsuarioResponseDTO {
    private UUID id;
    private String nome;
    private String email;
}
