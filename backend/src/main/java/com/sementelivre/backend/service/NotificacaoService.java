package com.sementelivre.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sementelivre.backend.dto.NotificacaoRequestDTO;
import com.sementelivre.backend.dto.NotificacaoResponseDTO;
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
