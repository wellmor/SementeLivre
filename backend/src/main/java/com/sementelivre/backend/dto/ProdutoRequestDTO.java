package com.sementelivre.backend.dto;

import java.util.UUID;

import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.TipoProduto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoRequestDTO(

        @NotBlank(message = "Nome popular é obrigatório")
        @Size(max = 150, message = "Nome popular deve ter no máximo 150 caracteres")
        String nomePopular,

        @Size(max = 150, message = "Nome científico deve ter no máximo 150 caracteres")
        String nomeCientifico,

        String historico,

        @NotBlank(message = "Foto do produto é obrigatória")
        @Size(max = 500, message = "URL da foto deve ter no máximo 500 caracteres")
        String urlFoto,

        @NotNull(message = "Tipo do produto é obrigatório")
        TipoProduto tipo,

        @NotNull(message = "Espécie é obrigatória")
        EspecieGeral especie,

        @NotNull(message = "Formato é obrigatório")
        FormatoProduto formato,

        @Size(max = 150, message = "Família botânica deve ter no máximo 150 caracteres")
        String familiaBotanica,

        UUID comunidadeOrigemId

) {
}