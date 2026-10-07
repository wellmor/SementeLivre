package com.sementelivre.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompradorDTO(

        @NotBlank(message = "Nome do comprador é obrigatório")
        @Size(max = 150, message = "Nome do comprador deve ter no máximo 150 caracteres")
        String nome,

        @Size(max = 20, message = "Telefone do comprador deve ter no máximo 20 caracteres")
        String telefone

) {
}
