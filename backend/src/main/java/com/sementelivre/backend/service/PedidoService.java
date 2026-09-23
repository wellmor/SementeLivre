package com.sementelivre.backend.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sementelivre.backend.dto.ItemPedidoRequestDTO;
import com.sementelivre.backend.dto.ItemPedidoResponseDTO;
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
import com.sementelivre.backend.entity.repository.EstoqueRepository;
import com.sementelivre.backend.entity.repository.PedidoRepository;
import com.sementelivre.backend.entity.repository.ProdutoRepository;
import com.sementelivre.backend.exception.EstoqueInsuficienteException;
import com.sementelivre.backend.exception.RecursoNaoEncontradoException;
import com.sementelivre.backend.exception.TransicaoStatusInvalidaException;
import com.sementelivre.backend.repository.UsuarioRepository;

/**
 * Regras de negocio do pedido (issue #67).
 *
 * Ciclo de vida e estoque andam juntos:
 *   - criar/alterar  : valida se ha estoque, mas NAO baixa (pedido nasce PENDENTE)
 *   - confirmar      : revalida e baixa o estoque
 *   - cancelar       : restaura o estoque apenas se ele tinha sido baixado
 *   - excluir        : restaura o estoque se o pedido estava CONFIRMADO (CDU-14)
 *
 * A baixa so acontece na confirmacao porque um pedido PENDENTE ainda pode ser
 * recusado pelo proprietario; reservar estoque antes disso bloquearia produto
 * de outros usuarios sem necessidade.
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            EstoqueRepository estoqueRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService) {

        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    // CREATE
    @Transactional
    public PedidoResponseDTO criar(PedidoRequestDTO dto) {

        Pedido pedido = new Pedido();
        pedido.setTipoPedido(dto.tipoPedido());
        pedido.setMensagemOpcional(dto.mensagemOpcional());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setUsuarioSolicitante(buscarUsuario(dto.usuarioSolicitanteId()));
        pedido.setProprietarioRecebedor(referenciaProprietario(dto.proprietarioRecebedorId()));

        preencherItens(pedido, dto.itens());

        reservarEstoque(pedido);

        // Os itens sao persistidos junto pelo cascade configurado em Pedido.itens
        return mapToResponse(pedidoRepository.save(pedido));
    }

    // READ - todos
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoRepository.findAllComItens()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // READ - por ID
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(UUID id) {
        return mapToResponse(buscarEntidadeComItens(id));
    }

    // UPDATE (CDU-13)
    @Transactional
    public PedidoResponseDTO atualizar(UUID id, PedidoUpdateDTO dto) {

        Pedido pedido = buscarEntidadeComItens(id);

        // So faz sentido alterar um pedido que ainda nao movimentou estoque.
        // Alterar um CONFIRMADO exigiria estornar a baixa antiga e refazer a
        // nova; o fluxo correto nesse caso e cancelar e abrir outro pedido.
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new TransicaoStatusInvalidaException(
                    "Apenas pedidos PENDENTE podem ser alterados. Status atual: "
                            + pedido.getStatus());
        }

        devolverEstoque(pedido);

        pedido.setTipoPedido(dto.tipoPedido());
        pedido.setMensagemOpcional(dto.mensagemOpcional());

        // Substitui a lista inteira: o orphanRemoval apaga os itens que sairam
        pedido.getItens().clear();
        preencherItens(pedido, dto.itens());

        reservarEstoque(pedido);

        return mapToResponse(pedidoRepository.save(pedido));
    }

    // DELETE (CDU-14)
    @Transactional
    public void excluir(UUID id) {

        Pedido pedido = buscarEntidadeComItens(id);

        // Se o estoque chegou a ser baixado, devolve antes de apagar o pedido,
        // senao a quantidade some do sistema junto com o registro.
        if (pedido.getStatus() == StatusPedido.PENDENTE
            || pedido.getStatus() == StatusPedido.CONFIRMADO) {
            devolverEstoque(pedido);
        }

        notificacaoService.desvincularPedido(pedido.getId());

        pedidoRepository.delete(pedido);
    }

    // CICLO DE VIDA

    @Transactional
    public PedidoResponseDTO confirmar(UUID id) {

        Pedido pedido = buscarEntidadeComItens(id);
        validarTransicao(pedido, StatusPedido.CONFIRMADO);

        pedido.setStatus(StatusPedido.CONFIRMADO);
        Pedido confirmado = pedidoRepository.save(pedido);
        notificacaoService.criarParaPedidoConfirmado(confirmado);

        return mapToResponse(confirmado);
    }

    @Transactional
    public PedidoResponseDTO cancelar(UUID id) {

        Pedido pedido = buscarEntidadeComItens(id);
        validarTransicao(pedido, StatusPedido.CANCELADO);

        if (pedido.getStatus() == StatusPedido.PENDENTE
            || pedido.getStatus() == StatusPedido.CONFIRMADO) {
            devolverEstoque(pedido);
        }

        pedido.setStatus(StatusPedido.CANCELADO);

        return mapToResponse(pedidoRepository.save(pedido));
    }

    // REGRAS DE ESTOQUE

    /** Reserva o estoque sob lock pessimista antes de persistir o pedido. */
    private void reservarEstoque(Pedido pedido) {

        UUID proprietarioId = pedido.getProprietarioRecebedor().getId();

        quantidadePorProduto(pedido).forEach((produtoId, quantidadePedida) -> {

            Estoque estoque = estoqueRepository
                    .findParaAtualizacao(proprietarioId, produtoId)
                    .orElseThrow(() -> new EstoqueInsuficienteException(
                            "O proprietário " + proprietarioId
                                    + " não possui estoque do produto " + produtoId));

            if (estoque.getQuantidade() < quantidadePedida) {
                throw new EstoqueInsuficienteException(
                        "Estoque insuficiente para o produto " + produtoId
                                + ": disponível " + estoque.getQuantidade()
                                + ", solicitado " + quantidadePedida);
            }

            estoque.setQuantidade(estoque.getQuantidade() - quantidadePedida);
            estoqueRepository.save(estoque);
        });
    }

    /**
     * Soma de volta o que foi reservado no registro. Se a linha de estoque
     * sumiu no meio do caminho nao ha o que restaurar, e o cancelamento nao
     * pode falhar por causa disso.
     */
    private void devolverEstoque(Pedido pedido) {

        UUID proprietarioId = pedido.getProprietarioRecebedor().getId();

        quantidadePorProduto(pedido).forEach((produtoId, quantidadeDevolvida) ->
                estoqueRepository
                        .findParaAtualizacao(proprietarioId, produtoId)
                        .ifPresent(estoque -> {
                            estoque.setQuantidade(estoque.getQuantidade() + quantidadeDevolvida);
                            estoqueRepository.save(estoque);
                        }));
    }

    /**
     * Agrupa por produto: o mesmo produto pode aparecer em mais de um item, e
     * validar item a item deixaria passar um pedido cuja soma estoura o estoque.
     * LinkedHashMap mantem a ordem dos itens, o que torna as mensagens de erro
     * previsiveis nos testes.
     */
    private Map<UUID, Double> quantidadePorProduto(Pedido pedido) {

        Map<UUID, Double> total = new LinkedHashMap<>();

        for (Itens item : pedido.getItens()) {
            total.merge(item.getProduto().getId(), item.getQuantidade(), Double::sum);
        }

        return total;
    }

    private void validarTransicao(Pedido pedido, StatusPedido destino) {

        if (!pedido.getStatus().podeTransicionarPara(destino)) {
            throw new TransicaoStatusInvalidaException(
                    "Transição inválida: " + pedido.getStatus() + " -> " + destino
                            + ". Transições permitidas a partir de " + pedido.getStatus()
                            + ": " + pedido.getStatus().proximosEstados());
        }
    }

    // AUXILIARES

    private void preencherItens(Pedido pedido, List<ItemPedidoRequestDTO> itens) {

        for (ItemPedidoRequestDTO itemDto : itens) {

            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: " + itemDto.produtoId()));

            Itens item = Itens.builder()
                    .produto(produto)
                    .quantidade(itemDto.quantidade())
                    .precoUnitario(itemDto.precoUnitario())
                    .build();

            pedido.adicionarItem(item);
        }
    }

    private Pedido buscarEntidadeComItens(UUID id) {
        return pedidoRepository.findByIdComItens(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pedido não encontrado: " + id));
    }

    private Usuario buscarUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário solicitante não encontrado: " + id));
    }

    // Mesma abordagem do EstoqueService: referencia so pelo id, sem ir ao banco
    private Proprietario referenciaProprietario(UUID id) {
        Proprietario proprietario = new Proprietario();
        proprietario.setId(id);
        return proprietario;
    }

    private PedidoResponseDTO mapToResponse(Pedido pedido) {

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getTipoPedido(),
                pedido.getMensagemOpcional(),
                pedido.getDataPedido(),
                pedido.getStatus(),
                pedido.getUsuarioSolicitante().getId(),
                pedido.getProprietarioRecebedor().getId(),
                pedido.getItens().stream().map(this::mapToItemResponse).toList()
        );
    }

    private ItemPedidoResponseDTO mapToItemResponse(Itens item) {

        return new ItemPedidoResponseDTO(
                item.getId(),
                item.getProduto().getId(),
                item.getQuantidade(),
                item.getPrecoUnitario()
        );
    }
}
