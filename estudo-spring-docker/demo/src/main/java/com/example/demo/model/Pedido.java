package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedido_t")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pedido", nullable = false, length = 10)
    private TipoPedido tipoPedido;

    @Column(name = "mensagem_opcional", columnDefinition = "TEXT")
    private String mensagemOpcional;

    @Column(name = "data_pedido", nullable = false, updatable = false)
    private LocalDateTime dataPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StatusPedido status = StatusPedido.PENDENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_solicitante_id", nullable = false)
    private Usuario usuarioSolicitante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietario_recebedor_id", nullable = false)
    private Proprietario proprietarioRecebedor;

    // Composicao: os itens nao existem fora do pedido (cascade + orphanRemoval)
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itens> itens = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.dataPedido = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusPedido.PENDENTE;
        }
    }

    // Metodos utilitarios para manter os dois lados do relacionamento sincronizados

    public void adicionarItem(Itens item) {
        this.itens.add(item);
        item.setPedido(this);
    }

    public void removerItem(Itens item) {
        this.itens.remove(item);
        item.setPedido(null);
    }

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

    public Usuario getUsuarioSolicitante() {
        return usuarioSolicitante;
    }

    public void setUsuarioSolicitante(Usuario usuarioSolicitante) {
        this.usuarioSolicitante = usuarioSolicitante;
    }

    public Proprietario getProprietarioRecebedor() {
        return proprietarioRecebedor;
    }

    public void setProprietarioRecebedor(Proprietario proprietarioRecebedor) {
        this.proprietarioRecebedor = proprietarioRecebedor;
    }

    public List<Itens> getItens() {
        return itens;
    }

    public void setItens(List<Itens> itens) {
        this.itens = itens;
    }
}
