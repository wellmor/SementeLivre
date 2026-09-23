package com.sementelivre.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.TipoRelatorio;

public record RelatorioResponseDTO(

        UUID id,
        TipoRelatorio tipo,
        Map<String, Object> filtrosUtilizados,
        LocalDateTime dataGeracao,
        UUID proprietarioId

) {
}
