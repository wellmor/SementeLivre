package com.sementelivre.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sementelivre.backend.dto.ItemPedidoRequestDTO;
import com.sementelivre.backend.dto.PedidoRequestDTO;
import com.sementelivre.backend.dto.PedidoResponseDTO;
import com.sementelivre.backend.dto.PedidoUpdateDTO;
import com.sementelivre.backend.entity.Estoque;
import com.sementelivre.backend.entity.Itens;
import com.sementelivre.backend.entity.Pedido;
import com.sementelivre.backend.entity.Produto;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.enums.StatusPedido;
import com.sementelivre.backend.entity.enums.TipoPedido;
import com.sementelivre.backend.entity.repository.EstoqueRepository;
import com.sementelivre.backend.entity.repository.PedidoRepository;
import com.sementelivre.backend.entity.repository.ProdutoRepository;
import com.sementelivre.backend.exception.EstoqueInsuficienteException;
import com.sementelivre.backend.exception.RecursoNaoEncontradoException;
import com.sementelivre.backend.exception.TransicaoStatusInvalidaException;
import com.sementelivre.backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private PedidoService pedidoService;

    private UUID pedidoId;
    private UUID produtoId;
    private UUID proprietarioId;
    private UUID usuarioId;

    private Produto produto;
    private Proprietario proprietario;
    private Usuario usuario;
    private Estoque estoque;

    private static final double ESTOQUE_INICIAL = 10.0;
    private static final double QUANTIDADE_PEDIDA = 4.0;

    @BeforeEach
    void setUp() {
        pedidoService = new PedidoService(
                pedidoRepository,
                produtoRepository,
                estoqueRepository,
                usuarioRepository
        );

        pedidoId = UUID.randomUUID();
        produtoId = UUID.randomUUID();
        proprietarioId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();

        produto = Produto.builder()
                .id(produtoId)
                .nomePopular("Milho crioulo")
                .build();

        proprietario = new Proprietario();
        proprietario.setId(proprietarioId);

        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("João");
        usuario.setEmail("joao@exemplo.com");

        estoque = Estoque.builder()
                .id(UUID.randomUUID())
                .produto(produto)
                .proprietario(proprietario)
                .quantidade(ESTOQUE_INICIAL)
                .preco(15.0)
                .build();
    }

    // CRIAR

    @Test
    void deveCriarPedidoComStatusPendenteSemBaixarEstoque() {
        mockUsuarioEProdutoExistentes();
        mockEstoqueDisponivel();
        mockSalvarPedido();

        PedidoResponseDTO resposta = pedidoService.criar(requestComQuantidade(QUANTIDADE_PEDIDA));

        assertNotNull(resposta);
        assertEquals(StatusPedido.PENDENTE, resposta.status());
        assertEquals(1, resposta.itens().size());
        assertEquals(produtoId, resposta.itens().get(0).produtoId());

        // O pedido nasce PENDENTE: o estoque so e tocado na confirmacao
        assertEquals(ESTOQUE_INICIAL, estoque.getQuantidade());
        verify(estoqueRepository, never()).save(any(Estoque.class));
    }

    @Test
    void naoDeveCriarPedidoQuandoQuantidadeMaiorQueEstoque() {
        mockUsuarioEProdutoExistentes();
        mockEstoqueDisponivel();

        EstoqueInsuficienteException erro = assertThrows(
                EstoqueInsuficienteException.class,
                () -> pedidoService.criar(requestComQuantidade(ESTOQUE_INICIAL + 1))
        );

        assertTrueContem(erro.getMessage(), "Estoque insuficiente");
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void naoDeveCriarPedidoQuandoProprietarioNaoTemEstoqueDoProduto() {
        mockUsuarioEProdutoExistentes();

        when(estoqueRepository.findByProprietarioIdAndProdutoId(proprietarioId, produtoId))
                .thenReturn(Optional.empty());

        assertThrows(
                EstoqueInsuficienteException.class,
                () -> pedidoService.criar(requestComQuantidade(QUANTIDADE_PEDIDA))
        );

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void deveSomarQuantidadesDoMesmoProdutoAoValidarEstoque() {
        mockUsuarioEProdutoExistentes();
        mockEstoqueDisponivel();

        // 6 + 5 = 11 estoura os 10 em estoque, embora nenhum item sozinho estoure
        PedidoRequestDTO dto = new PedidoRequestDTO(
                TipoPedido.VENDA,
                "Dois itens do mesmo produto",
                usuarioId,
                proprietarioId,
                List.of(
                        new ItemPedidoRequestDTO(produtoId, 6.0, 15.0),
                        new ItemPedidoRequestDTO(produtoId, 5.0, 15.0)
                )
        );

        assertThrows(
                EstoqueInsuficienteException.class,
                () -> pedidoService.criar(dto)
        );

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void naoDeveCriarPedidoQuandoProdutoNaoExiste() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.criar(requestComQuantidade(QUANTIDADE_PEDIDA))
        );

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void naoDeveCriarPedidoQuandoUsuarioSolicitanteNaoExiste() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.criar(requestComQuantidade(QUANTIDADE_PEDIDA))
        );

        verify(produtoRepository, never()).findById(any(UUID.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // CONFIRMAR

    @Test
    void deveConfirmarPedidoEBaixarEstoque() {
        mockBuscaPedido(pedidoPendente());
        mockEstoqueParaAtualizacao();
        mockSalvarPedido();

        PedidoResponseDTO resposta = pedidoService.confirmar(pedidoId);

        assertEquals(StatusPedido.CONFIRMADO, resposta.status());
        assertEquals(ESTOQUE_INICIAL - QUANTIDADE_PEDIDA, estoque.getQuantidade());

        verify(estoqueRepository).save(estoque);
    }

    @Test
    void naoDeveConfirmarQuandoEstoqueCaiuEntreACriacaoEAConfirmacao() {
        // Outro pedido consumiu o estoque enquanto este estava PENDENTE
        estoque.setQuantidade(QUANTIDADE_PEDIDA - 1);

        mockBuscaPedido(pedidoPendente());
        mockEstoqueParaAtualizacao();

        assertThrows(
                EstoqueInsuficienteException.class,
                () -> pedidoService.confirmar(pedidoId)
        );

        verify(estoqueRepository, never()).save(any(Estoque.class));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void naoDeveConfirmarPedidoJaConfirmado() {
        mockBuscaPedido(pedidoComStatus(StatusPedido.CONFIRMADO));

        assertThrows(
                TransicaoStatusInvalidaException.class,
                () -> pedidoService.confirmar(pedidoId)
        );

        // Falha antes de encostar no estoque: nada de baixa dupla
        verify(estoqueRepository, never()).findParaAtualizacao(any(UUID.class), any(UUID.class));
        verify(estoqueRepository, never()).save(any(Estoque.class));
    }

    @Test
    void naoDeveConfirmarPedidoCancelado() {
        mockBuscaPedido(pedidoComStatus(StatusPedido.CANCELADO));

        assertThrows(
                TransicaoStatusInvalidaException.class,
                () -> pedidoService.confirmar(pedidoId)
        );

        verify(estoqueRepository, never()).save(any(Estoque.class));
    }

    // CANCELAR

    @Test
    void deveCancelarPedidoConfirmadoERestaurarEstoque() {
        // Estoque ja baixado pela confirmacao anterior
        estoque.setQuantidade(ESTOQUE_INICIAL - QUANTIDADE_PEDIDA);

        mockBuscaPedido(pedidoComStatus(StatusPedido.CONFIRMADO));
        mockEstoqueParaAtualizacao();
        mockSalvarPedido();

        PedidoResponseDTO resposta = pedidoService.cancelar(pedidoId);

        assertEquals(StatusPedido.CANCELADO, resposta.status());
        assertEquals(ESTOQUE_INICIAL, estoque.getQuantidade());

        verify(estoqueRepository).save(estoque);
    }

    @Test
    void deveCancelarPedidoPendenteSemMexerNoEstoque() {
        mockBuscaPedido(pedidoPendente());
        mockSalvarPedido();

        PedidoResponseDTO resposta = pedidoService.cancelar(pedidoId);

        assertEquals(StatusPedido.CANCELADO, resposta.status());
        assertEquals(ESTOQUE_INICIAL, estoque.getQuantidade());

        // Nunca houve baixa, entao nao pode haver devolucao
        verify(estoqueRepository, never()).findParaAtualizacao(any(UUID.class), any(UUID.class));
        verify(estoqueRepository, never()).save(any(Estoque.class));
    }

    @Test
    void naoDeveCancelarPedidoJaCancelado() {
        mockBuscaPedido(pedidoComStatus(StatusPedido.CANCELADO));

        assertThrows(
                TransicaoStatusInvalidaException.class,
                () -> pedidoService.cancelar(pedidoId)
        );

        // Cancelar duas vezes devolveria o estoque em dobro
        verify(estoqueRepository, never()).save(any(Estoque.class));
    }

    // EXCLUIR (CDU-14)

    @Test
    void deveRestaurarEstoqueAoExcluirPedidoConfirmado() {
        estoque.setQuantidade(ESTOQUE_INICIAL - QUANTIDADE_PEDIDA);

        Pedido pedido = pedidoComStatus(StatusPedido.CONFIRMADO);
        mockBuscaPedido(pedido);
        mockEstoqueParaAtualizacao();

        pedidoService.excluir(pedidoId);

        assertEquals(ESTOQUE_INICIAL, estoque.getQuantidade());
        verify(estoqueRepository).save(estoque);
        verify(pedidoRepository).delete(pedido);
    }

    @Test
    void deveExcluirPedidoPendenteSemMexerNoEstoque() {
        Pedido pedido = pedidoPendente();
        mockBuscaPedido(pedido);

        pedidoService.excluir(pedidoId);

        assertEquals(ESTOQUE_INICIAL, estoque.getQuantidade());
        verify(estoqueRepository, never()).save(any(Estoque.class));
        verify(pedidoRepository).delete(pedido);
    }

    // ATUALIZAR (CDU-13)

    @Test
    void deveAtualizarPedidoPendente() {
        mockBuscaPedido(pedidoPendente());
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        mockEstoqueDisponivel();
        mockSalvarPedido();

        PedidoUpdateDTO dto = new PedidoUpdateDTO(
                TipoPedido.TROCA,
                "Mensagem alterada",
                List.of(new ItemPedidoRequestDTO(produtoId, 2.0, 15.0))
        );

        PedidoResponseDTO resposta = pedidoService.atualizar(pedidoId, dto);

        assertEquals(TipoPedido.TROCA, resposta.tipoPedido());
        assertEquals("Mensagem alterada", resposta.mensagemOpcional());
        assertEquals(1, resposta.itens().size());
        assertEquals(2.0, resposta.itens().get(0).quantidade());
    }

    @Test
    void naoDeveAtualizarPedidoConfirmado() {
        mockBuscaPedido(pedidoComStatus(StatusPedido.CONFIRMADO));

        PedidoUpdateDTO dto = new PedidoUpdateDTO(
                TipoPedido.TROCA,
                "Tentativa de alteração",
                List.of(new ItemPedidoRequestDTO(produtoId, 2.0, 15.0))
        );

        assertThrows(
                TransicaoStatusInvalidaException.class,
                () -> pedidoService.atualizar(pedidoId, dto)
        );

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void naoDeveAtualizarQuandoNovaQuantidadeExcedeEstoque() {
        mockBuscaPedido(pedidoPendente());
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        mockEstoqueDisponivel();

        PedidoUpdateDTO dto = new PedidoUpdateDTO(
                TipoPedido.VENDA,
                "Quantidade acima do estoque",
                List.of(new ItemPedidoRequestDTO(produtoId, ESTOQUE_INICIAL + 5, 15.0))
        );

        assertThrows(
                EstoqueInsuficienteException.class,
                () -> pedidoService.atualizar(pedidoId, dto)
        );

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // CONSULTA

    @Test
    void deveListarPedidosComItens() {
        UUID proprietarioId = java.util.UUID.randomUUID();
        when(pedidoRepository.findAllByProprietarioRecebedorId(proprietarioId)).thenReturn(List.of(pedidoPendente()));

        List<PedidoResponseDTO> resultado = pedidoService.listarTodos(proprietarioId);

        assertEquals(1, resultado.size());
        assertEquals(pedidoId, resultado.get(0).id());
        assertEquals(1, resultado.get(0).itens().size());
    }

    @Test
    void deveLancarExcecaoAoBuscarPedidoInexistente() {
        when(pedidoRepository.findByIdComItens(pedidoId)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> pedidoService.buscarPorId(pedidoId)
        );
    }

    // AUXILIARES

    private PedidoRequestDTO requestComQuantidade(double quantidade) {
        return new PedidoRequestDTO(
                TipoPedido.VENDA,
                "Pedido de teste",
                usuarioId,
                proprietarioId,
                List.of(new ItemPedidoRequestDTO(produtoId, quantidade, 15.0))
        );
    }

    private Pedido pedidoPendente() {
        return pedidoComStatus(StatusPedido.PENDENTE);
    }

    private Pedido pedidoComStatus(StatusPedido status) {
        Pedido pedido = Pedido.builder()
                .id(pedidoId)
                .tipoPedido(TipoPedido.VENDA)
                .mensagemOpcional("Pedido de teste")
                .status(status)
                .usuarioSolicitante(usuario)
                .proprietarioRecebedor(proprietario)
                .build();

        pedido.adicionarItem(
                Itens.builder()
                        .id(UUID.randomUUID())
                        .produto(produto)
                        .quantidade(QUANTIDADE_PEDIDA)
                        .precoUnitario(15.0)
                        .build()
        );

        return pedido;
    }

    private void mockUsuarioEProdutoExistentes() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
    }

    private void mockEstoqueDisponivel() {
        when(estoqueRepository.findByProprietarioIdAndProdutoId(proprietarioId, produtoId))
                .thenReturn(Optional.of(estoque));
    }

    private void mockEstoqueParaAtualizacao() {
        when(estoqueRepository.findParaAtualizacao(proprietarioId, produtoId))
                .thenReturn(Optional.of(estoque));
    }

    private void mockBuscaPedido(Pedido pedido) {
        when(pedidoRepository.findByIdComItens(pedidoId)).thenReturn(Optional.of(pedido));
    }

    private void mockSalvarPedido() {
        when(pedidoRepository.save(any(Pedido.class)))
                .thenAnswer(invocation -> {
                    Pedido salvo = invocation.getArgument(0);
                    if (salvo.getId() == null) {
                        salvo.setId(pedidoId);
                    }
                    return salvo;
                });
    }

    private void assertTrueContem(String texto, String trecho) {
        org.junit.jupiter.api.Assertions.assertTrue(
                texto != null && texto.contains(trecho),
                "Esperava que a mensagem contivesse \"" + trecho + "\", mas foi: " + texto);
    }
}
