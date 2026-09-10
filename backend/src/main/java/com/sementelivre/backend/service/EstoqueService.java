package com.sementelivre.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import com.sementelivre.backend.exception.RecursoNaoEncontradoException;
import com.sementelivre.backend.dto.EstoqueRequestDTO;
import com.sementelivre.backend.dto.EstoqueResponseDTO;
import com.sementelivre.backend.entity.Estoque;
import com.sementelivre.backend.entity.Produto;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.repository.EstoqueRepository;
import com.sementelivre.backend.entity.repository.ProdutoRepository;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;

    public EstoqueService(
            EstoqueRepository estoqueRepository,
            ProdutoRepository produtoRepository) {

        this.estoqueRepository = estoqueRepository;
        this.produtoRepository = produtoRepository;
    }

    // CREATE
    public EstoqueResponseDTO criar(EstoqueRequestDTO dto) {

        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado: " + dto.produtoId()
                ));

        Proprietario proprietario = Proprietario.builder()
                .id(dto.proprietarioId())
                .build();

        Estoque estoque = Estoque.builder()
                .proprietario(proprietario)
                .produto(produto)
                .descricao(dto.descricao())
                .preco(dto.preco())
                .quantidade(dto.quantidade())
                .tipoPesagem(dto.tipoPesagem())
                .disponibilidade(dto.disponibilidade())
                .tipoMovimentacao(dto.tipoMovimentacao())
                .dataMovimentacao(LocalDateTime.now())
                .dataUltimaAtualizacao(LocalDateTime.now())
                .build();

        Estoque salvo = estoqueRepository.save(estoque);

        return toResponseDTO(salvo);
    }

    // READ - todos
    public List<EstoqueResponseDTO> listarTodos() {
        return estoqueRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // READ - por ID
    public EstoqueResponseDTO buscarPorId(UUID id) {
        Estoque estoque = buscarEntidadePorId(id);

        return toResponseDTO(estoque);
    }

    // UPDATE
    public EstoqueResponseDTO atualizar(UUID id, EstoqueRequestDTO dto) {

        Estoque estoque = buscarEntidadePorId(id);

        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto não encontrado: " + dto.produtoId()
                ));

        Proprietario proprietario = Proprietario.builder()
                .id(dto.proprietarioId())
                .build();

        estoque.setProprietario(proprietario);
        estoque.setProduto(produto);
        estoque.setDescricao(dto.descricao());
        estoque.setPreco(dto.preco());
        estoque.setQuantidade(dto.quantidade());
        estoque.setTipoPesagem(dto.tipoPesagem());
        estoque.setDisponibilidade(dto.disponibilidade());
        estoque.setTipoMovimentacao(dto.tipoMovimentacao());

        estoque.setDataUltimaAtualizacao(LocalDateTime.now());

        Estoque atualizado = estoqueRepository.save(estoque);

        return toResponseDTO(atualizado);
    }

    // DELETE
    public void excluir(UUID id) {
        Estoque estoque = buscarEntidadePorId(id);

        estoqueRepository.delete(estoque);
    }

    private Estoque buscarEntidadePorId(UUID id) {
        return estoqueRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Estoque não encontrado: " + id
                ));
    }

    private EstoqueResponseDTO toResponseDTO(Estoque estoque) {

        return new EstoqueResponseDTO(
                estoque.getId(),
                estoque.getProprietario().getId(),
                estoque.getProduto().getId(),
                estoque.getDescricao(),
                estoque.getPreco(),
                estoque.getQuantidade(),
                estoque.getTipoPesagem(),
                estoque.getDisponibilidade(),
                estoque.getTipoMovimentacao(),
                estoque.getDataMovimentacao(),
                estoque.getDataUltimaAtualizacao()
        );
    }
}