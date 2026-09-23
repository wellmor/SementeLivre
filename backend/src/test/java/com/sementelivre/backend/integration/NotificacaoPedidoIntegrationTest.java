package com.sementelivre.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sementelivre.backend.BackendApplication;
import com.sementelivre.backend.dto.PedidoResponseDTO;
import com.sementelivre.backend.entity.Estoque;
import com.sementelivre.backend.entity.Notificacao;
import com.sementelivre.backend.entity.enums.StatusPedido;
import com.sementelivre.backend.entity.repository.EstoqueRepository;
import com.sementelivre.backend.entity.repository.PedidoRepository;
import com.sementelivre.backend.repository.NotificacaoRepository;
import com.sementelivre.backend.service.EstoqueService;
import com.sementelivre.backend.service.PedidoService;

import jakarta.persistence.EntityManager;

@SpringBootTest(classes = BackendApplication.class)
class NotificacaoPedidoIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final double ESTOQUE_INICIAL = 100.0;
    private static final double QUANTIDADE_PEDIDA = 20.0;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private com.sementelivre.backend.entity.repository.ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Test
    @Transactional
    void confirmarPedido_deveCriarNotificacaoNaoLidaSemNovaBaixa() {
        CrossDomainFixture.Scenario scenario = CrossDomainFixture.create(
                entityManager, produtoRepository, estoqueService, ESTOQUE_INICIAL);
        PedidoResponseDTO criado = pedidoService.criar(
                CrossDomainFixture.pedido(scenario, QUANTIDADE_PEDIDA));

        pedidoService.confirmar(criado.id());
        entityManager.flush();
        entityManager.clear();

        var pedido = pedidoRepository.findByIdComItens(criado.id()).orElseThrow();
        Estoque estoque = estoqueRepository
                .findByProprietarioIdAndProdutoId(
                        scenario.proprietario().getId(), scenario.produto().getId())
                .orElseThrow();
        List<Notificacao> notificacoes = notificacaoRepository
                .findByProprietarioIdOrderByDataGeracaoDesc(scenario.proprietario().getId());

        assertEquals(StatusPedido.CONFIRMADO, pedido.getStatus());
        assertEquals(ESTOQUE_INICIAL - QUANTIDADE_PEDIDA, estoque.getQuantidade());
        assertEquals(1, notificacoes.size());
        assertFalse(notificacoes.get(0).isLida());
        assertEquals(pedido.getId(), notificacoes.get(0).getPedidoRelacionado().getId());
        assertEquals(scenario.proprietario().getId(), notificacoes.get(0).getProprietario().getId());
        assertEquals("Pedido confirmado", notificacoes.get(0).getTitulo());
    }

    @Test
    @Transactional
    void excluirPedido_deveManterNotificacaoComPedidoRelacionadoNulo() {
        CrossDomainFixture.Scenario scenario = CrossDomainFixture.create(
                entityManager, produtoRepository, estoqueService, ESTOQUE_INICIAL);
        PedidoResponseDTO criado = pedidoService.criar(
                CrossDomainFixture.pedido(scenario, QUANTIDADE_PEDIDA));
        pedidoService.confirmar(criado.id());
        entityManager.flush();

        pedidoService.excluir(criado.id());
        entityManager.flush();
        entityManager.clear();

        Notificacao notificacao = notificacaoRepository
                .findByProprietarioIdOrderByDataGeracaoDesc(scenario.proprietario().getId())
                .get(0);
        assertEquals(null, notificacao.getPedidoRelacionado());
    }
}
