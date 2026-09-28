package com.sementelivre.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sementelivre.backend.dto.NotificacaoRequestDTO;
import com.sementelivre.backend.dto.NotificacaoResponseDTO;
import com.sementelivre.backend.entity.Itens;
import com.sementelivre.backend.entity.Notificacao;
import com.sementelivre.backend.entity.Pedido;
import com.sementelivre.backend.entity.Produto;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.enums.TipoPedido;
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

        proprietario = new Proprietario();
        proprietario.setId(proprietarioId);

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

    // ---- CDU-26: notificacao automatica quando o pedido e confirmado (#69) ----

    /** Monta um pedido confirmado com 1 solicitante e 2 itens. */
    private Pedido pedidoConfirmado() {

        Usuario solicitante = mock(Usuario.class);
        when(solicitante.getNome()).thenReturn("Maria Silva");

        Produto feijao = Produto.builder().nomePopular("Feijao Crioulo").build();
        Produto milho = Produto.builder().nomePopular("Milho").build();

        return Pedido.builder()
                .id(UUID.randomUUID())
                .tipoPedido(TipoPedido.TROCA)
                .usuarioSolicitante(solicitante)
                .proprietarioRecebedor(proprietario)
                .itens(List.of(
                        Itens.builder().produto(feijao).quantidade(2.0).build(),
                        Itens.builder().produto(milho).quantidade(1.5).build()))
                .build();
    }

    /** Captura a notificacao que o service mandou salvar. */
    private Notificacao notificacaoSalva(Pedido pedido) {

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);

        when(notificacaoRepository.saveAndFlush(captor.capture())).thenReturn(notificacao);

        notificacaoService.criarParaPedidoConfirmado(pedido);

        return captor.getValue();
    }

    @Test
    void mensagemDoPedidoConfirmadoDeveTerTipoSolicitanteEItens() {
        Pedido pedido = pedidoConfirmado();

        String mensagem = notificacaoSalva(pedido).getMensagem();

        assertTrue(mensagem.contains("Pedido de TROCA confirmado"), mensagem);
        assertTrue(mensagem.contains("Solicitante: Maria Silva"), mensagem);
        assertTrue(mensagem.contains("Feijao Crioulo"), mensagem);
        assertTrue(mensagem.contains("Milho"), mensagem);
    }

    @Test
    void mensagemDeveMostrarQuantidadeSemCasaDecimalDesnecessaria() {
        Pedido pedido = pedidoConfirmado();

        String mensagem = notificacaoSalva(pedido).getMensagem();

        assertTrue(mensagem.contains("2 x Feijao Crioulo"), mensagem);
        assertTrue(mensagem.contains("1.5 x Milho"), mensagem);
    }

    @Test
    void notificacaoDoPedidoConfirmadoDeveManterTituloEDestinatario() {
        Pedido pedido = pedidoConfirmado();

        Notificacao salva = notificacaoSalva(pedido);

        // O titulo e usado pelo teste de integracao do Dev 8: nao pode mudar.
        assertEquals("Pedido confirmado", salva.getTitulo());
        assertFalse(salva.isLida());
    }

    @Test
    void mensagemDeveFuncionarQuandoPedidoNaoTemItens() {
        Pedido pedido = Pedido.builder()
                .id(UUID.randomUUID())
                .tipoPedido(TipoPedido.DOACAO)
                .proprietarioRecebedor(proprietario)
                .itens(List.of())
                .build();

        String mensagem = notificacaoSalva(pedido).getMensagem();

        assertEquals("Pedido de DOACAO confirmado.", mensagem);
    }

    // ---- S4 (#70): casos que ainda nao estavam cobertos ----

    @Test
    void deveCriarNotificacaoLigadaAUmPedido() {
        UUID pedidoId = UUID.randomUUID();
        Pedido pedido = Pedido.builder().id(pedidoId).build();

        NotificacaoRequestDTO comPedido = new NotificacaoRequestDTO(
                "Novo pedido recebido",
                "Voce recebeu um pedido.",
                proprietarioId,
                pedidoId
        );

        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(proprietario);
        when(entityManager.find(Pedido.class, pedidoId)).thenReturn(pedido);

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        when(notificacaoRepository.saveAndFlush(captor.capture())).thenReturn(notificacao);

        notificacaoService.criar(comPedido);

        assertEquals(pedidoId, captor.getValue().getPedidoRelacionado().getId());
    }

    @Test
    void naoDeveCriarNotificacaoSePedidoInformadoNaoExistir() {
        UUID pedidoId = UUID.randomUUID();

        NotificacaoRequestDTO comPedido = new NotificacaoRequestDTO(
                "Novo pedido recebido",
                "Voce recebeu um pedido.",
                proprietarioId,
                pedidoId
        );

        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(proprietario);
        when(entityManager.find(Pedido.class, pedidoId)).thenReturn(null);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> notificacaoService.criar(comPedido));

        verify(notificacaoRepository, never()).saveAndFlush(any(Notificacao.class));
    }

    @Test
    void naoDeveAtualizarNotificacaoInexistente() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> notificacaoService.atualizar(notificacaoId, dto));

        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }

    @Test
    void naoDeveAtualizarNotificacaoSeProprietarioNaoExistir() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.of(notificacao));
        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(null);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> notificacaoService.atualizar(notificacaoId, dto));

        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }

    @Test
    void marcarComoLidaDuasVezesNaoDeveTrocarADataDeLeitura() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.of(notificacao));
        when(notificacaoRepository.save(notificacao)).thenReturn(notificacao);

        LocalDateTime primeiraLeitura = notificacaoService.marcarComoLida(notificacaoId).dataLeitura();
        LocalDateTime segundaLeitura = notificacaoService.marcarComoLida(notificacaoId).dataLeitura();

        assertEquals(primeiraLeitura, segundaLeitura);
    }

    @Test
    void naoDeveMarcarComoLidaNotificacaoInexistente() {
        when(notificacaoRepository.findById(notificacaoId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> notificacaoService.marcarComoLida(notificacaoId));
    }

    @Test
    void desvincularPedidoDeveLimparOPedidoDasNotificacoesDaquelePedido() {
        UUID pedidoId = UUID.randomUUID();
        notificacao.setPedidoRelacionado(Pedido.builder().id(pedidoId).build());

        when(notificacaoRepository.findByPedidoRelacionadoId(pedidoId))
                .thenReturn(List.of(notificacao));

        notificacaoService.desvincularPedido(pedidoId);

        assertNull(notificacao.getPedidoRelacionado());
        verify(notificacaoRepository).flush();
    }

    @Test
    void notificacaoDoPedidoConfirmadoDeveApontarParaOPedidoEParaOProprietarioRecebedor() {
        Pedido pedido = pedidoConfirmado();

        // getReference devolve a entidade "gerenciada" pelo JPA; no mock precisamos dizer o que retornar
        when(entityManager.getReference(Pedido.class, pedido.getId())).thenReturn(pedido);
        when(entityManager.getReference(Proprietario.class, proprietarioId)).thenReturn(proprietario);

        Notificacao salva = notificacaoSalva(pedido);

        assertEquals(pedido.getId(), salva.getPedidoRelacionado().getId());
        assertEquals(proprietarioId, salva.getProprietario().getId());
    }
}
