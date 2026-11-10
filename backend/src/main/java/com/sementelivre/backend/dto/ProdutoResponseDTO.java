package com.sementelivre.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.TipoProduto;

public record ProdutoResponseDTO(

        UUID id,
        String nomePopular,
        String nomeCientifico,
        String historico,
        String urlFoto,
        TipoProduto tipo,
        EspecieGeral especie,
        FormatoProduto formato,
        String familiaBotanica,
        UUID comunidadeOrigemId,
        LocalDateTime dataInclusao,
        LocalDateTime dataUltimaAlteracao

) {
}