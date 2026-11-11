package com.sementelivre.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaDTO(
    @NotBlank(message = "O token é obrigatório")
    String token,

    // Mesma regra do cadastro e do alterar-senha: a redefinição não pode aceitar
    // uma senha que esses dois recusariam. Acima de 72 bytes o BCrypt não aceita.
    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")
    String novaSenha
) {}
