package com.example.demo.dto;

import com.example.demo.model.TipoPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PedidoCreateDTO {

    @NotNull(message = "O tipo do pedido é obrigatório")
    private TipoPedido tipoPedido;

    private String mensagemOpcional;

    @NotNull(message = "O usuário solicitante é obrigatório")
    private UUID usuarioSolicitanteId;

    @NotNull(message = "O proprietário recebedor é obrigatório")
    private UUID proprietarioRecebedorId;

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

    public UUID getUsuarioSolicitanteId() {
        return usuarioSolicitanteId;
    }

    public void setUsuarioSolicitanteId(UUID usuarioSolicitanteId) {
        this.usuarioSolicitanteId = usuarioSolicitanteId;
    }

    public UUID getProprietarioRecebedorId() {
        return proprietarioRecebedorId;
    }

    public void setProprietarioRecebedorId(UUID proprietarioRecebedorId) {
        this.proprietarioRecebedorId = proprietarioRecebedorId;
    }

    public List<ItemPedidoCreateDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoCreateDTO> itens) {
        this.itens = itens;
    }
}
