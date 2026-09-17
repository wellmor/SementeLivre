package com.sementelivre.backend.dto;

import java.util.Map;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.TipoRelatorio;

import jakarta.validation.constraints.NotNull;

public record RelatorioRequestDTO(

        @NotNull(message = "Tipo do relatório é obrigatório")
        TipoRelatorio tipo,

        // opcional: sem filtros significa "todos os dados"
        Map<String, Object> filtrosUtilizados,

        @NotNull(message = "Proprietário é obrigatório")
        UUID proprietarioId

) {
}
