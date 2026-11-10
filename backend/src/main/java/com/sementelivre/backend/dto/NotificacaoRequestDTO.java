package com.sementelivre.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// "lida" e as datas nao entram aqui: sao controladas pelo sistema
public record NotificacaoRequestDTO(

        @NotBlank(message = "Título é obrigatório")
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String titulo,

        @NotBlank(message = "Mensagem é obrigatória")
        String mensagem,

        @NotNull(message = "Proprietário é obrigatório")
        UUID proprietarioId,

        // opcional
        UUID pedidoRelacionadoId

) {
}
