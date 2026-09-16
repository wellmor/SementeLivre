package com.example.demo.service;

import com.example.demo.dto.ItemPedidoCreateDTO;
import com.example.demo.dto.ItemPedidoResponseDTO;
import com.example.demo.dto.PedidoCreateDTO;
import com.example.demo.dto.PedidoResponseDTO;
import com.example.demo.dto.PedidoUpdateDTO;
import com.example.demo.exception.PedidoInvalidoException;
import com.example.demo.exception.PedidoNaoEncontradoException;
import com.example.demo.model.Itens;
import com.example.demo.model.Pedido;
import com.example.demo.model.Pessoa;
import com.example.demo.model.Proprietario;
import com.example.demo.model.Usuario;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PessoaRepository pessoaRepository;

    public PedidoService(PedidoRepository pedidoRepository, PessoaRepository pessoaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public PedidoResponseDTO criar(PedidoCreateDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setTipoPedido(dto.getTipoPedido());
        pedido.setMensagemOpcional(dto.getMensagemOpcional());
        pedido.setUsuarioSolicitante(buscarUsuario(dto.getUsuarioSolicitanteId()));
        pedido.setProprietarioRecebedor(buscarProprietario(dto.getProprietarioRecebedorId()));

        for (ItemPedidoCreateDTO itemDto : dto.getItens()) {
            pedido.adicionarItem(mapToItem(itemDto));
        }

        // Os itens sao persistidos junto pelo cascade configurado em Pedido.itens
        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listar() {
        return pedidoRepository.findAllComItens().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(UUID id) {
        return mapToResponse(buscarEntidade(id));
    }

    @Transactional
    public PedidoResponseDTO atualizar(UUID id, PedidoUpdateDTO dto) {
        Pedido pedido = buscarEntidade(id);
        pedido.setTipoPedido(dto.getTipoPedido());
        pedido.setMensagemOpcional(dto.getMensagemOpcional());
        pedido.setStatus(dto.getStatus());

        // Substitui a lista inteira: o orphanRemoval apaga os itens que sairam
        pedido.getItens().clear();
        for (ItemPedidoCreateDTO itemDto : dto.getItens()) {
            pedido.adicionarItem(mapToItem(itemDto));
        }

        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Transactional
    public void deletar(UUID id) {
        pedidoRepository.delete(buscarEntidade(id));
    }

    // Metodos auxiliares

    private Pedido buscarEntidade(UUID id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNaoEncontradoException("Pedido não encontrado com o ID: " + id));
    }

    private Usuario buscarUsuario(UUID id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new PedidoInvalidoException("Usuário solicitante não encontrado com o ID: " + id));
        if (!(pessoa instanceof Usuario usuario)) {
            throw new PedidoInvalidoException("A pessoa " + id + " não é um usuário solicitante válido.");
        }
        return usuario;
    }

    private Proprietario buscarProprietario(UUID id) {
        Pessoa pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new PedidoInvalidoException("Proprietário recebedor não encontrado com o ID: " + id));
        if (!(pessoa instanceof Proprietario proprietario)) {
            throw new PedidoInvalidoException("A pessoa " + id + " não é um proprietário recebedor válido.");
        }
        return proprietario;
    }

    private Itens mapToItem(ItemPedidoCreateDTO dto) {
        Itens item = new Itens();
        item.setProdutoId(dto.getProdutoId());
        item.setQuantidade(dto.getQuantidade());
        item.setPrecoUnitario(dto.getPrecoUnitario());
        return item;
    }

    private PedidoResponseDTO mapToResponse(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setTipoPedido(pedido.getTipoPedido());
        dto.setMensagemOpcional(pedido.getMensagemOpcional());
        dto.setDataPedido(pedido.getDataPedido());
        dto.setStatus(pedido.getStatus());
        dto.setUsuarioSolicitanteId(pedido.getUsuarioSolicitante().getId());
        dto.setProprietarioRecebedorId(pedido.getProprietarioRecebedor().getId());
        dto.setItens(pedido.getItens().stream().map(this::mapToItemResponse).toList());
        return dto;
    }

    private ItemPedidoResponseDTO mapToItemResponse(Itens item) {
        ItemPedidoResponseDTO dto = new ItemPedidoResponseDTO();
        dto.setId(item.getId());
        dto.setProdutoId(item.getProdutoId());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnitario());
        return dto;
    }
}
