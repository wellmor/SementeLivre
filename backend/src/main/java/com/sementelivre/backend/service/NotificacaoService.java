package com.sementelivre.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sementelivre.backend.dto.NotificacaoRequestDTO;
import com.sementelivre.backend.dto.NotificacaoResponseDTO;
import com.sementelivre.backend.entity.Itens;
import com.sementelivre.backend.entity.Notificacao;
import com.sementelivre.backend.entity.Pedido;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.exception.RecursoNaoEncontradoException;
import com.sementelivre.backend.repository.NotificacaoRepository;

import jakarta.persistence.EntityManager;

@Service
public class NotificacaoService
        implements CrudService<NotificacaoRequestDTO, NotificacaoResponseDTO, UUID> {

    private final NotificacaoRepository notificacaoRepository;
    private final EntityManager entityManager;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            EntityManager entityManager) {

        this.notificacaoRepository = notificacaoRepository;
        this.entityManager = entityManager;
    }

    // CREATE
    @Override
    public NotificacaoResponseDTO criar(NotificacaoRequestDTO dto) {

        Notificacao notificacao = Notificacao.builder()
                .titulo(dto.titulo())
                .mensagem(dto.mensagem())
                .proprietario(buscarProprietario(dto.proprietarioId()))
                .pedidoRelacionado(buscarPedido(dto.pedidoRelacionadoId()))
                .build();

        // saveAndFlush grava na hora, assim a dataGeracao ja volta preenchida
        Notificacao salva = notificacaoRepository.saveAndFlush(notificacao);

        return toResponseDTO(salva);
    }

    public NotificacaoResponseDTO criarParaPedidoConfirmado(Pedido pedido) {
        entityManager.flush();
        Pedido pedidoGerenciado = entityManager.getReference(Pedido.class, pedido.getId());
        Proprietario proprietarioGerenciado = entityManager.getReference(
            Proprietario.class, pedido.getProprietarioRecebedor().getId());

        Notificacao notificacao = Notificacao.builder()
            .titulo("Pedido confirmado")
            .mensagem(montarMensagemDoPedido(pedido))
            .proprietario(proprietarioGerenciado)
            .pedidoRelacionado(pedidoGerenciado)
            .build();

        return toResponseDTO(notificacaoRepository.saveAndFlush(notificacao));
    }

    // Monta o texto da notificacao com os detalhes do pedido, como pede o CDU-26.
    // Exemplo: "Pedido de TROCA confirmado. Solicitante: Maria. Itens: 2 x Feijao Crioulo."
    private String montarMensagemDoPedido(Pedido pedido) {

        StringBuilder mensagem = new StringBuilder();

        if (pedido.getTipoPedido() != null) {
            mensagem.append("Pedido de ").append(pedido.getTipoPedido()).append(" confirmado.");
        } else {
            mensagem.append("Pedido confirmado.");
        }

        if (pedido.getUsuarioSolicitante() != null) {
            mensagem.append(" Solicitante: ").append(pedido.getUsuarioSolicitante().getNome()).append(".");
        }

        String itens = montarListaDeItens(pedido);

        if (!itens.isEmpty()) {
            mensagem.append(" Itens: ").append(itens).append(".");
        }

        return mensagem.toString();
    }

    // Junta os itens em um texto so: "2 x Feijao Crioulo, 1 x Milho"
    private String montarListaDeItens(Pedido pedido) {

        if (pedido.getItens() == null) {
            return "";
        }

        List<String> itens = new ArrayList<>();

        for (Itens item : pedido.getItens()) {
            String nomeProduto = "produto";

            if (item.getProduto() != null && item.getProduto().getNomePopular() != null) {
                nomeProduto = item.getProduto().getNomePopular();
            }

            itens.add(formatarQuantidade(item.getQuantidade()) + " x " + nomeProduto);
        }

        return String.join(", ", itens);
    }

    // A quantidade e Double: mostra 2 em vez de 2.0 quando nao tem casa decimal
    private String formatarQuantidade(Double quantidade) {

        if (quantidade == null) {
            return "0";
        }

        if (quantidade == Math.floor(quantidade)) {
            return String.valueOf(quantidade.longValue());
        }

        return String.valueOf(quantidade);
    }

    public void desvincularPedido(UUID pedidoId) {
        notificacaoRepository.findByPedidoRelacionadoId(pedidoId)
                .forEach(notificacao -> notificacao.setPedidoRelacionado(null));
        notificacaoRepository.flush();
    }

    // READ - por ID
    @Override
    public NotificacaoResponseDTO buscarPorId(UUID id) {
        Notificacao notificacao = buscarEntidadePorId(id);

        return toResponseDTO(notificacao);
    }

    // READ - todos
    @Override
    public List<NotificacaoResponseDTO> listar() {
        return notificacaoRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // UPDATE
    @Override
    public NotificacaoResponseDTO atualizar(UUID id, NotificacaoRequestDTO dto) {

        Notificacao notificacao = buscarEntidadePorId(id);

        notificacao.setTitulo(dto.titulo());
        notificacao.setMensagem(dto.mensagem());
        notificacao.setProprietario(buscarProprietario(dto.proprietarioId()));
        notificacao.setPedidoRelacionado(buscarPedido(dto.pedidoRelacionadoId()));

        Notificacao atualizada = notificacaoRepository.save(notificacao);

        return toResponseDTO(atualizada);
    }

    // UPDATE - marcar como lida
    public NotificacaoResponseDTO marcarComoLida(UUID id) {

        Notificacao notificacao = buscarEntidadePorId(id);

        notificacao.marcarComoLida();

        Notificacao atualizada = notificacaoRepository.save(notificacao);

        return toResponseDTO(atualizada);
    }

    // DELETE
    @Override
    public void deletar(UUID id) {
        Notificacao notificacao = buscarEntidadePorId(id);

        notificacaoRepository.delete(notificacao);
    }

    private Notificacao buscarEntidadePorId(UUID id) {
        return notificacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Notificação não encontrada: " + id
                ));
    }

    // Proprietario e Pedido ainda nao tem repository (sao placeholders),
    // entao buscamos direto pelo EntityManager
    private Proprietario buscarProprietario(UUID proprietarioId) {
        Proprietario proprietario = entityManager.find(Proprietario.class, proprietarioId);

        if (proprietario == null) {
            throw new RecursoNaoEncontradoException("Proprietário não encontrado: " + proprietarioId);
        }

        return proprietario;
    }

    // O pedido e opcional
    private Pedido buscarPedido(UUID pedidoId) {
        if (pedidoId == null) {
            return null;
        }

        Pedido pedido = entityManager.find(Pedido.class, pedidoId);

        if (pedido == null) {
            throw new RecursoNaoEncontradoException("Pedido não encontrado: " + pedidoId);
        }

        return pedido;
    }

    private NotificacaoResponseDTO toResponseDTO(Notificacao notificacao) {

        UUID pedidoRelacionadoId = null;

        if (notificacao.getPedidoRelacionado() != null) {
            pedidoRelacionadoId = notificacao.getPedidoRelacionado().getId();
        }

        return new NotificacaoResponseDTO(
                notificacao.getId(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.isLida(),
                notificacao.getDataGeracao(),
                notificacao.getDataLeitura(),
                notificacao.getProprietario().getId(),
                pedidoRelacionadoId
        );
    }
}
