package com.sementelivre.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.sementelivre.backend.dto.NotificacaoRequestDTO;
import com.sementelivre.backend.dto.NotificacaoResponseDTO;
import com.sementelivre.backend.dto.PedidoRequestDTO;
import com.sementelivre.backend.dto.PedidoResponseDTO;
import com.sementelivre.backend.entity.Produto;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.enums.Disponibilidade;
import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.Pesagem;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.entity.enums.TipoMovimentacao;
import com.sementelivre.backend.entity.enums.TipoPedido;
import com.sementelivre.backend.entity.enums.TipoProduto;
import com.sementelivre.backend.entity.repository.ProdutoRepository;
import com.sementelivre.backend.repository.NotificacaoRepository;
import com.sementelivre.backend.service.EstoqueService;
import com.sementelivre.backend.service.NotificacaoService;
import com.sementelivre.backend.service.PedidoService;

import jakarta.persistence.EntityManager;

@SpringBootTest(classes = BackendApplication.class)
class NotificacaoPedidoIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Test
    @Transactional
    void devePersistirNotificacaoRelacionadaAoPedidoNoPostgres() {
        Proprietario proprietario = persistirProprietario();
        Usuario usuario = persistirUsuario();
        Produto produto = persistirProduto();

        estoqueService.criar(new EstoqueRequestDTO(
                proprietario.getId(),
                produto.getId(),
                "Estoque de notificação",
                15.0,
                5.0,
                Pesagem.KG,
                Disponibilidade.PARA_VENDA,
                TipoMovimentacao.ENTRADA
        ));

        PedidoResponseDTO pedido = pedidoService.criar(new PedidoRequestDTO(
                TipoPedido.VENDA,
                "Pedido relacionado à notificação",
                usuario.getId(),
                proprietario.getId(),
                List.of(new ItemPedidoRequestDTO(produto.getId(), 2.0, 15.0))
        ));

        NotificacaoResponseDTO notificacaoCriada = notificacaoService.criar(new NotificacaoRequestDTO(
                "Novo pedido recebido",
                "Um novo pedido foi criado para seu estoque.",
                proprietario.getId(),
                pedido.id()
        ));
        entityManager.flush();
        entityManager.clear();

        var persistida = notificacaoRepository.findById(notificacaoCriada.id()).orElseThrow();

        assertNotNull(persistida.getId());
        assertEquals("Novo pedido recebido", persistida.getTitulo());
        assertEquals("Um novo pedido foi criado para seu estoque.", persistida.getMensagem());
        assertFalse(persistida.isLida());
        assertEquals(proprietario.getId(), persistida.getProprietario().getId());
        assertEquals(pedido.id(), persistida.getPedidoRelacionado().getId());
    }

    private Proprietario persistirProprietario() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Proprietario proprietario = new Proprietario();
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setDocumento("529" + sufixo);
        proprietario.setNome("Proprietario Notificacao " + sufixo);
        proprietario.setEmail("proprietario.notificacao." + sufixo + "@teste.com");
        proprietario.setSenhaHash("hash123");
        proprietario.setRg("MG-N" + sufixo);
        entityManager.persist(proprietario);
        entityManager.flush();
        return proprietario;
    }

    private Usuario persistirUsuario() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        Usuario usuario = new Usuario();
        usuario.setTipoDocumento(TipoDocumento.CPF);
        usuario.setDocumento("529" + sufixo);
        usuario.setNome("Usuario Notificacao " + sufixo);
        usuario.setEmail("usuario.notificacao." + sufixo + "@teste.com");
        usuario.setSenhaHash("hash123");
        entityManager.persist(usuario);
        entityManager.flush();
        return usuario;
    }

    private Produto persistirProduto() {
        Produto produto = Produto.builder()
                .nomePopular("Feijão de integração")
                .nomeCientifico("Phaseolus vulgaris")
                .urlFoto("https://example.com/feijao.png")
                .tipo(TipoProduto.LEGUMINOSA)
                .especie(EspecieGeral.FEIJAO)
                .formato(FormatoProduto.SEMENTE)
                .dataInclusao(LocalDateTime.now())
                .dataUltimaAlteracao(LocalDateTime.now())
                .build();
        return produtoRepository.saveAndFlush(produto);
    }
}
