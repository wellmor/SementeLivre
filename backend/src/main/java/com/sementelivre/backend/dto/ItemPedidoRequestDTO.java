package com.sementelivre.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemPedidoRequestDTO(

        @NotNull(message = "Produto é obrigatório")
        UUID produtoId,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Double quantidade,

        @PositiveOrZero(message = "Preço unitário não pode ser negativo")
        Double precoUnitario

) {
}
