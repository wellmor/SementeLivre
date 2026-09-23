package com.sementelivre.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.sementelivre.backend.BackendApplication;
import com.sementelivre.backend.dto.PedidoResponseDTO;
import com.sementelivre.backend.entity.Estoque;
import com.sementelivre.backend.entity.Pedido;
import com.sementelivre.backend.entity.enums.StatusPedido;
import com.sementelivre.backend.entity.repository.EstoqueRepository;
import com.sementelivre.backend.entity.repository.PedidoRepository;
import com.sementelivre.backend.entity.repository.ProdutoRepository;
import com.sementelivre.backend.exception.EstoqueInsuficienteException;
import com.sementelivre.backend.exception.TransicaoStatusInvalidaException;
import com.sementelivre.backend.repository.NotificacaoRepository;
import com.sementelivre.backend.service.EstoqueService;
import com.sementelivre.backend.service.PedidoService;

import jakarta.persistence.EntityManager;

@SpringBootTest(classes = BackendApplication.class)
class PedidoEstoqueIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final double ESTOQUE_INICIAL = 100.0;
    private static final double QUANTIDADE_PEDIDA = 20.0;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @Transactional
    void registrarPedido_deveDiminuirSaldoEstoque() {
        CrossDomainFixture.Scenario scenario = fixture(ESTOQUE_INICIAL);

        PedidoResponseDTO pedido = pedidoService.criar(CrossDomainFixture.pedido(scenario, QUANTIDADE_PEDIDA));
        entityManager.flush();
        entityManager.clear();

        Estoque estoque = estoque(scenario);
        Pedido persistido = pedidoRepository.findByIdComItens(pedido.id()).orElseThrow();

        assertEquals(ESTOQUE_INICIAL - QUANTIDADE_PEDIDA, estoque.getQuantidade());
        assertEquals(StatusPedido.PENDENTE, persistido.getStatus());
    }

    @Test
    @Transactional
    void cancelarPedido_deveRestaurarSaldoEstoque() {
        CrossDomainFixture.Scenario scenario = fixture(ESTOQUE_INICIAL);
        PedidoResponseDTO criado = pedidoService.criar(CrossDomainFixture.pedido(scenario, QUANTIDADE_PEDIDA));

        pedidoService.cancelar(criado.id());
        entityManager.flush();
        entityManager.clear();

        assertEquals(ESTOQUE_INICIAL, estoque(scenario).getQuantidade());
        assertEquals(StatusPedido.CANCELADO,
                pedidoRepository.findById(criado.id()).orElseThrow().getStatus());
    }

    @Test
    @Transactional
    void estoqueInsuficiente_deveRejeitarPedidoSemAlterarDados() {
        CrossDomainFixture.Scenario scenario = fixture(10.0);

        assertThrows(EstoqueInsuficienteException.class,
                () -> pedidoService.criar(CrossDomainFixture.pedido(scenario, QUANTIDADE_PEDIDA)));
        entityManager.flush();
        entityManager.clear();

        assertEquals(10.0, estoque(scenario).getQuantidade());
        assertEquals(0, pedidoRepository.count());
    }

    @Test
    @Transactional
    void cancelarPedidoDuasVezes_deveFalharSemAlterarEstoque() {
        CrossDomainFixture.Scenario scenario = fixture(ESTOQUE_INICIAL);
        PedidoResponseDTO criado = pedidoService.criar(CrossDomainFixture.pedido(scenario, QUANTIDADE_PEDIDA));

        pedidoService.cancelar(criado.id());
        assertThrows(TransicaoStatusInvalidaException.class, () -> pedidoService.cancelar(criado.id()));
        entityManager.flush();
        entityManager.clear();

        assertEquals(ESTOQUE_INICIAL, estoque(scenario).getQuantidade());
        assertEquals(StatusPedido.CANCELADO,
                pedidoRepository.findById(criado.id()).orElseThrow().getStatus());
    }

    @Test
    @Transactional
    void excluirPedidoPendente_deveExcluirItensERestaurarEstoque() {
        CrossDomainFixture.Scenario scenario = fixture(ESTOQUE_INICIAL);
        PedidoResponseDTO criado = pedidoService.criar(CrossDomainFixture.pedido(scenario, QUANTIDADE_PEDIDA));

        pedidoService.excluir(criado.id());
        entityManager.flush();
        entityManager.clear();

        assertEquals(ESTOQUE_INICIAL, estoque(scenario).getQuantidade());
        assertEquals(0, pedidoRepository.count());
    }

    @Test
    void falhaAoRegistrarPedido_deveFazerRollbackDePedidoEEstoque() {
        CrossDomainFixture.Scenario scenario = transactionTemplate.execute(status ->
                CrossDomainFixture.create(entityManager, produtoRepository, estoqueService, ESTOQUE_INICIAL));

        assertThrows(RuntimeException.class, () -> transactionTemplate.executeWithoutResult(status -> {
            pedidoService.criar(CrossDomainFixture.pedido(scenario, QUANTIDADE_PEDIDA));
            throw new TestRollbackException();
        }));

        assertEquals(ESTOQUE_INICIAL, estoque(scenario).getQuantidade());
        assertEquals(0, pedidoRepository.count());
    }

    @Test
    void falhaAoConfirmarPedido_deveFazerRollbackDePedidoEstoqueENotificacao() {
        CrossDomainFixture.Scenario scenario = transactionTemplate.execute(status -> {
            CrossDomainFixture.Scenario created = CrossDomainFixture.create(
                    entityManager, produtoRepository, estoqueService, ESTOQUE_INICIAL);
            pedidoService.criar(CrossDomainFixture.pedido(created, QUANTIDADE_PEDIDA));
            return created;
        });
        UUID pedidoId = pedidoRepository.findAll().stream().findFirst().orElseThrow().getId();

        assertThrows(RuntimeException.class, () -> transactionTemplate.executeWithoutResult(status -> {
            pedidoService.confirmar(pedidoId);
            throw new TestRollbackException();
        }));

        entityManager.clear();
        assertEquals(StatusPedido.PENDENTE, pedidoRepository.findById(pedidoId).orElseThrow().getStatus());
        assertEquals(ESTOQUE_INICIAL - QUANTIDADE_PEDIDA, estoque(scenario).getQuantidade());
        assertEquals(0, notificacaoRepository
            .findByProprietarioIdOrderByDataGeracaoDesc(scenario.proprietario().getId()).size());
    }

    private static final class TestRollbackException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private CrossDomainFixture.Scenario fixture(double quantidade) {
        return CrossDomainFixture.create(entityManager, produtoRepository, estoqueService, quantidade);
    }

    private Estoque estoque(CrossDomainFixture.Scenario scenario) {
        return estoqueRepository
                .findByProprietarioIdAndProdutoId(scenario.proprietario().getId(), scenario.produto().getId())
                .orElseThrow();
    }
}
