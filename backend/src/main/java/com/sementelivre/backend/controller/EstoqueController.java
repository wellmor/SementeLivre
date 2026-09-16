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

import com.sementelivre.backend.dto.EstoqueRequestDTO;
import com.sementelivre.backend.dto.EstoqueResponseDTO;
import com.sementelivre.backend.service.EstoqueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<EstoqueResponseDTO> criar(
            @Valid @RequestBody EstoqueRequestDTO estoque) {

        return ResponseEntity.ok(
                estoqueService.criar(estoque)
        );
    }

    // READ - todos
    @GetMapping
    public ResponseEntity<List<EstoqueResponseDTO>> listarTodos() {

        return ResponseEntity.ok(
                estoqueService.listarTodos()
        );
    }

    // READ - por ID
    @GetMapping("/{id}")
    public ResponseEntity<EstoqueResponseDTO> buscarPorId(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                estoqueService.buscarPorId(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<EstoqueResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EstoqueRequestDTO estoque) {

        return ResponseEntity.ok(
                estoqueService.atualizar(id, estoque)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {

        estoqueService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}