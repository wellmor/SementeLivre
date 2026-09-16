package com.sementelivre.backend.dto;

import java.util.UUID;

public record ItemPedidoResponseDTO(

        UUID id,
        UUID produtoId,
        Double quantidade,
        Double precoUnitario

) {
}
