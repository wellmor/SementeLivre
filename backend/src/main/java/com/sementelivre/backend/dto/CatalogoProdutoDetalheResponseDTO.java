package com.sementelivre.backend.dto;

import java.util.List;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.TipoProduto;

public record CatalogoProdutoDetalheResponseDTO(

        UUID id,
        String nomePopular,
        String nomeCientifico,
        String historico,
        String urlFoto,
        TipoProduto tipo,
        EspecieGeral especie,
        FormatoProduto formato,
        String familiaBotanica,

        UUID comunidadeId,
        String comunidade,
        String municipio,
        String uf,

        List<CatalogoOfertaResponseDTO> ofertas

) {
}