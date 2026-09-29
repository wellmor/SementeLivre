package com.sementelivre.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaDTO(
    @NotBlank(message = "A senha atual é obrigatória")
    String senhaAtual,

    // O BCrypt considera no maximo 72 bytes da senha.
    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")
    String novaSenha
) {}
