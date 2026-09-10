package com.sementelivre.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sementelivre.backend.dto.ProdutoRequestDTO;
import com.sementelivre.backend.dto.ProdutoResponseDTO;
import com.sementelivre.backend.service.ProdutoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(
            @Valid @RequestBody ProdutoRequestDTO produto) {

        return ResponseEntity.ok(
                produtoService.criar(produto)
        );
    }

    // READ - todos
    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarTodos() {

        return ResponseEntity.ok(
                produtoService.listarTodos()
        );
    }

    // READ - por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                produtoService.buscarPorId(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoRequestDTO produto) {

        return ResponseEntity.ok(
                produtoService.atualizar(id, produto)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {

        produtoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}