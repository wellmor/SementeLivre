package com.sementelivre.backend.dto;

import java.util.List;

import com.sementelivre.backend.entity.enums.TipoPedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Nao expoe o status: a mudanca de status passa pelos endpoints de confirmar e
 * cancelar, que sao os unicos pontos que sabem mexer no estoque. Deixar o
 * status aqui permitiria pular a baixa/restauracao com um PUT.
 *
 * O comprador (nome e contato de quem recebe, RF-05) e opcional: ausente, o
 * pedido mantem o atual; presente, substitui nome, telefone e e-mail.
 */
public record PedidoUpdateDTO(

        @NotNull(message = "Tipo do pedido é obrigatório")
        TipoPedido tipoPedido,

        String mensagemOpcional,

        @Valid
        @NotEmpty(message = "O pedido deve ter ao menos um item")
        List<ItemPedidoRequestDTO> itens,

        @Valid
        CompradorDTO comprador

) {
    public PedidoUpdateDTO(TipoPedido tipoPedido, String mensagemOpcional, List<ItemPedidoRequestDTO> itens) {
        this(tipoPedido, mensagemOpcional, itens, null);
    }
}
