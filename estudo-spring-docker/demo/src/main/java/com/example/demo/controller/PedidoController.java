package com.example.demo.controller;

import com.example.demo.dto.PedidoCreateDTO;
import com.example.demo.dto.PedidoResponseDTO;
import com.example.demo.dto.PedidoUpdateDTO;
import com.example.demo.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponseDTO criar(@Valid @RequestBody PedidoCreateDTO dto) {
        return pedidoService.criar(dto);
    }

    @GetMapping
    public List<PedidoResponseDTO> listar() {
        return pedidoService.listar();
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscarPorId(@PathVariable UUID id) {
        return pedidoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public PedidoResponseDTO atualizar(@PathVariable UUID id, @Valid @RequestBody PedidoUpdateDTO dto) {
        return pedidoService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable UUID id) {
        pedidoService.deletar(id);
    }
}
