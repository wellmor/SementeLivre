package com.sementelivre.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sementelivre.backend.BackendApplication;
import com.sementelivre.backend.dto.EstoqueRequestDTO;
import com.sementelivre.backend.dto.ItemPedidoRequestDTO;
import com.sementelivre.backend.dto.PedidoRequestDTO;
import com.sementelivre.backend.dto.PedidoResponseDTO;
import com.sementelivre.backend.entity.Estoque;
import com.sementelivre.backend.entity.Pedido;
import com.sementelivre.backend.entity.Produto;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.enums.Disponibilidade;
import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.Pesagem;
import com.sementelivre.backend.entity.enums.StatusPedido;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.entity.enums.TipoMovimentacao;
import com.sementelivre.backend.entity.enums.TipoPedido;
import com.sementelivre.backend.entity.enums.TipoProduto;
import com.sementelivre.backend.entity.repository.EstoqueRepository;
import com.sementelivre.backend.entity.repository.PedidoRepository;
import com.sementelivre.backend.entity.repository.ProdutoRepository;
import com.sementelivre.backend.service.EstoqueService;
import com.sementelivre.backend.service.PedidoService;

import jakarta.persistence.EntityManager;

@SpringBootTest(classes = BackendApplication.class)
class PedidoEstoqueIntegrationTest extends AbstractPostgresIntegrationTest {

    private static final double QUANTIDADE_INICIAL = 10.0;
    private static final double QUANTIDADE_PEDIDA = 4.0;

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

    @Test
    @Transactional
    void deveConfirmarPedidoEBaixarEstoqueNoPostgres() {
        Proprietario proprietario = persistirProprietario();
        Usuario usuario = persistirUsuario();
        Produto produto = persistirProduto();

        persistirEstoque(proprietario, produto);

        PedidoResponseDTO pedidoCriado = pedidoService.criar(new PedidoRequestDTO(
                TipoPedido.VENDA,
                "Pedido de integração",
                usuario.getId(),
                proprietario.getId(),
                List.of(new ItemPedidoRequestDTO(produto.getId(), QUANTIDADE_PEDIDA, 15.0))
        ));

        PedidoResponseDTO pedidoConfirmado = pedidoService.confirmar(pedidoCriado.id());
        entityManager.flush();
        entityManager.clear();

        Pedido pedidoPersistido = pedidoRepository.findByIdComItens(pedidoCriado.id()).orElseThrow();
        Estoque estoquePersistido = estoqueRepository
                .findByProprietarioIdAndProdutoId(proprietario.getId(), produto.getId())
                .orElseThrow();

        assertEquals(StatusPedido.CONFIRMADO, pedidoPersistido.getStatus());
        assertEquals(QUANTIDADE_INICIAL - QUANTIDADE_PEDIDA, estoquePersistido.getQuantidade());
        assertEquals(produto.getId(), pedidoPersistido.getItens().get(0).getProduto().getId());
        assertEquals(StatusPedido.CONFIRMADO, pedidoConfirmado.status());
    }

    private Proprietario persistirProprietario() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Proprietario proprietario = new Proprietario();
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setDocumento("529" + sufixo);
        proprietario.setNome("Proprietario Pedido " + sufixo);
        proprietario.setEmail("proprietario.pedido." + sufixo + "@teste.com");
        proprietario.setSenhaHash("hash123");
        proprietario.setRg("MG-P" + sufixo);
        entityManager.persist(proprietario);
        entityManager.flush();
        return proprietario;
    }

    private Usuario persistirUsuario() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Usuario usuario = new Usuario();
        usuario.setTipoDocumento(TipoDocumento.CPF);
        usuario.setDocumento("529" + sufixo);
        usuario.setNome("Usuario Pedido " + sufixo);
        usuario.setEmail("usuario.pedido." + sufixo + "@teste.com");
        usuario.setSenhaHash("hash123");
        entityManager.persist(usuario);
        return usuario;
    }

    private Produto persistirProduto() {
        Produto produto = Produto.builder()
                .nomePopular("Milho de integração")
                .nomeCientifico("Zea mays")
                .urlFoto("https://example.com/milho.png")
                .tipo(TipoProduto.CEREAL)
                .especie(EspecieGeral.MILHO)
                .formato(FormatoProduto.SEMENTE)
                .dataInclusao(LocalDateTime.now())
                .dataUltimaAlteracao(LocalDateTime.now())
                .build();
        Produto salvo = produtoRepository.saveAndFlush(produto);
        assertNotNull(salvo.getId());
        return salvo;
    }

    private void persistirEstoque(Proprietario proprietario, Produto produto) {
        estoqueService.criar(new EstoqueRequestDTO(
                proprietario.getId(),
                produto.getId(),
                "Estoque de integração",
                15.0,
                QUANTIDADE_INICIAL,
                Pesagem.KG,
                Disponibilidade.PARA_VENDA,
                TipoMovimentacao.ENTRADA
        ));
    }
}
