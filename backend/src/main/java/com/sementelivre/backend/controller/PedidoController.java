package com.sementelivre.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.sementelivre.backend.dto.PedidoRequestDTO;
import com.sementelivre.backend.dto.PedidoResponseDTO;
import com.sementelivre.backend.dto.PedidoUpdateDTO;
import com.sementelivre.backend.service.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(
            @Valid @RequestBody PedidoRequestDTO pedido) {

        return ResponseEntity.ok(
                pedidoService.criar(pedido)
        );
    }

    // READ - todos os pedidos de um proprietario especifico
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos(@RequestParam UUID proprietarioId) {

        return ResponseEntity.ok(
                pedidoService.listarTodos(proprietarioId)
        );
    }

    // READ - por ID
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                pedidoService.buscarPorId(id)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody PedidoUpdateDTO pedido) {

        return ResponseEntity.ok(
                pedidoService.atualizar(id, pedido)
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {

        pedidoService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    // CICLO DE VIDA
    // Endpoints proprios em vez de aceitar status no PUT: sao eles que
    // disparam a baixa e a restauracao do estoque.

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PedidoResponseDTO> confirmar(@PathVariable UUID id) {

        return ResponseEntity.ok(
                pedidoService.confirmar(id)
        );
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable UUID id) {

        return ResponseEntity.ok(
                pedidoService.cancelar(id)
        );
    }
}
