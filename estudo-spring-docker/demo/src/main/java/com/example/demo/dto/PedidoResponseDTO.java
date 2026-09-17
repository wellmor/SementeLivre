package com.example.demo.dto;

import com.example.demo.model.StatusPedido;
import com.example.demo.model.TipoPedido;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PedidoResponseDTO {

    private UUID id;
    private TipoPedido tipoPedido;
    private String mensagemOpcional;
    private LocalDateTime dataPedido;
    private StatusPedido status;
    private UUID usuarioSolicitanteId;
    private UUID proprietarioRecebedorId;
    private List<ItemPedidoResponseDTO> itens = new ArrayList<>();

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDateTime dataPedido) {
        this.dataPedido = dataPedido;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
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

    public List<ItemPedidoResponseDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoResponseDTO> itens) {
        this.itens = itens;
    }
}
