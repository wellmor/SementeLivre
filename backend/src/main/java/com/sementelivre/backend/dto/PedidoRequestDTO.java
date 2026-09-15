package com.sementelivre.backend.dto;

import java.util.List;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.TipoPedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PedidoRequestDTO(

        @NotNull(message = "Tipo do pedido é obrigatório")
        TipoPedido tipoPedido,

        String mensagemOpcional,

        @NotNull(message = "Usuário solicitante é obrigatório")
        UUID usuarioSolicitanteId,

        @NotNull(message = "Proprietário recebedor é obrigatório")
        UUID proprietarioRecebedorId,

        @Valid
        @NotEmpty(message = "O pedido deve ter ao menos um item")
        List<ItemPedidoRequestDTO> itens

) {
}
