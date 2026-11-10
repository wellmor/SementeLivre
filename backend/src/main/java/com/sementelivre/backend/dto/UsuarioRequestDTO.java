package com.sementelivre.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "UsuarioRequestDTO", description = "Dados para cadastro ou atualização de usuário")
public class UsuarioRequestDTO {
    @NotBlank(message = "O nome é obrigatório")
    @Size(min = 3, max = 50, message = "O nome deve ter entre 3 a 50 caracteres")
    @Schema(description = "Nome do usuário", example = "Maria da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Email do usuário", example = "maria@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
