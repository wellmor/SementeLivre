package com.sementelivre.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacaoResponseDTO(

        UUID id,
        String titulo,
        String mensagem,
        boolean lida,
        LocalDateTime dataGeracao,
        LocalDateTime dataLeitura,
        UUID proprietarioId,
        UUID pedidoRelacionadoId

) {
}
