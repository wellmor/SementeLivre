package com.sementelivre.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sementelivre.backend.dto.NotificacaoRequestDTO;
import com.sementelivre.backend.dto.NotificacaoResponseDTO;
import com.sementelivre.backend.entity.Notificacao;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.exception.RecursoNaoEncontradoException;
import com.sementelivre.backend.repository.NotificacaoRepository;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private EntityManager entityManager;

    private NotificacaoService notificacaoService;

    private UUID notificacaoId;
    private UUID proprietarioId;

    private Proprietario proprietario;
    private Notificacao notificacao;
    private NotificacaoRequestDTO dto;

    @BeforeEach
    void setUp() {
        notificacaoService = new NotificacaoService(
                notificacaoRepository,
                entityManager
        );

        notificacaoId = UUID.randomUUID();
        proprietarioId = UUID.randomUUID();

        proprietario = Proprietario.builder()
                .id(proprietarioId)
                .build();

        notificacao = Notificacao.builder()
                .id(notificacaoId)
                .titulo("Novo pedido recebido")
                .mensagem("Voce recebeu um pedido.")
                .proprietario(proprietario)
                .build();

        dto = new NotificacaoRequestDTO(
                "Novo pedido recebido",
                "Voce recebeu um pedido.",
                proprietarioId,
                null
        );
    }

    @Test
    void deveCriarNotificacao() {
        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(proprietario);
        when(notificacaoRepository.saveAndFlush(any(Notificacao.class))).thenReturn(notificacao);

        NotificacaoResponseDTO resposta = notificacaoService.criar(dto);

        assertNotNull(resposta);
        assertEquals("Novo pedido recebido", resposta.titulo());
        assertEquals(proprietarioId, resposta.proprietarioId());
        assertFalse(resposta.lida());
    }

    @Test
    void naoDeveCriarNotificacaoSeProprietarioNaoExistir() {
        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(null);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> notificacaoService.criar(dto));

        verify(notificacaoRepository, never()).saveAndFlush(any(Notificacao.class));
    }

    @Test
    void deveBuscarNotificacaoPorId() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.of(notificacao));

        NotificacaoResponseDTO resposta = notificacaoService.buscarPorId(notificacaoId);

        assertEquals(notificacaoId, resposta.id());
    }

    @Test
    void deveLancarErroAoBuscarNotificacaoInexistente() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> notificacaoService.buscarPorId(notificacaoId));
    }

    @Test
    void deveListarNotificacoes() {
        when(notificacaoRepository.findAll()).thenReturn(List.of(notificacao));

        List<NotificacaoResponseDTO> resposta = notificacaoService.listar();

        assertEquals(1, resposta.size());
    }

    @Test
    void deveAtualizarNotificacao() {
        NotificacaoRequestDTO novosDados = new NotificacaoRequestDTO(
                "Titulo atualizado",
                "Mensagem atualizada",
                proprietarioId,
                null
        );

        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.of(notificacao));
        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(proprietario);
        when(notificacaoRepository.save(notificacao)).thenReturn(notificacao);

        NotificacaoResponseDTO resposta = notificacaoService.atualizar(notificacaoId, novosDados);

        assertEquals("Titulo atualizado", resposta.titulo());
        assertEquals("Mensagem atualizada", resposta.mensagem());
    }

    @Test
    void deveMarcarNotificacaoComoLida() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.of(notificacao));
        when(notificacaoRepository.save(notificacao)).thenReturn(notificacao);

        NotificacaoResponseDTO resposta = notificacaoService.marcarComoLida(notificacaoId);

        assertTrue(resposta.lida());
        assertNotNull(resposta.dataLeitura());
    }

    @Test
    void deveDeletarNotificacao() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.of(notificacao));

        notificacaoService.deletar(notificacaoId);

        verify(notificacaoRepository).delete(notificacao);
    }

    @Test
    void naoDeveDeletarNotificacaoInexistente() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> notificacaoService.deletar(notificacaoId));

        verify(notificacaoRepository, never()).delete(any(Notificacao.class));
    }
}
