package com.example.demo.dto;

import com.example.demo.model.StatusPedido;
import com.example.demo.model.TipoPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PedidoUpdateDTO {

    @NotNull(message = "O tipo do pedido é obrigatório")
    private TipoPedido tipoPedido;

    private String mensagemOpcional;

    @NotNull(message = "O status do pedido é obrigatório")
    private StatusPedido status;

    // O PUT substitui a lista inteira de itens; os removidos saem por orphanRemoval
    @Valid
    @NotEmpty(message = "O pedido deve ter pelo menos um item")
    private List<ItemPedidoCreateDTO> itens = new ArrayList<>();

    // Getters and Setters

    public TipoPedido getTipoPedido() {
        return tipoPedido;
    }

    public void setTipoPedido(TipoPedido tipoPedido) {
        this.tipoPedido = tipoPedido;
    }

    public String getMensagemOpcional() {
        return mensagemOpcional;
    }

    public void setMensagemOpcional(String mensagemOpcional) {
        this.mensagemOpcional = mensagemOpcional;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public List<ItemPedidoCreateDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoCreateDTO> itens) {
        this.itens = itens;
    }
}
