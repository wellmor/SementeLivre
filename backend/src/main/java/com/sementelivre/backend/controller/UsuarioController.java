package com.sementelivre.backend.controller;

import com.sementelivre.backend.dto.UsuarioCreateRequestDTO;
import com.sementelivre.backend.dto.UsuarioResponseDTO;
import com.sementelivre.backend.dto.PessoaUpdateRequestDTO;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.Pessoa;
import com.sementelivre.backend.service.PessoaService;
import com.sementelivre.backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PessoaService pessoaService;

    public UsuarioController(UsuarioService usuarioService, PessoaService pessoaService) {
        this.usuarioService = usuarioService;
        this.pessoaService = pessoaService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioCreateRequestDTO dto) {
        Usuario usuario = usuarioService.criar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(usuario.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapToResponse(usuario));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        List<UsuarioResponseDTO> usuarios = pessoaService.listarTodas().stream()
                .filter(p -> p instanceof Usuario)
                .map(p -> mapToResponse((Usuario) p))
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable UUID id) {
        Pessoa pessoa = pessoaService.buscarPorId(id);
        if (!(pessoa instanceof Usuario)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapToResponse((Usuario) pessoa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable UUID id, @Valid @RequestBody PessoaUpdateRequestDTO dto) {
        Pessoa pessoa = pessoaService.buscarPorId(id);
        if (!(pessoa instanceof Usuario)) {
            return ResponseEntity.notFound().build();
        }
        Usuario atualizado = (Usuario) pessoaService.atualizar(id, dto);
        return ResponseEntity.ok(mapToResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        Pessoa pessoa = pessoaService.buscarPorId(id);
        if (!(pessoa instanceof Usuario)) {
            return ResponseEntity.notFound().build();
        }
        pessoaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private UsuarioResponseDTO mapToResponse(Usuario u) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(u.getId());
        dto.setTipoDocumento(u.getTipoDocumento());
        dto.setDocumento(u.getDocumento());
        dto.setNome(u.getNome());
        dto.setTelefone(u.getTelefone());
        dto.setEmail(u.getEmail());
        dto.setDataCadastro(u.getDataCadastro());
        dto.setDataUltimaAlteracao(u.getDataUltimaAlteracao());
        dto.setTipoPessoa("USUARIO");

        if (u.getLogradouro() != null) {
            com.sementelivre.backend.dto.LogradouroDTO end = new com.sementelivre.backend.dto.LogradouroDTO();
            end.setLogradouro(u.getLogradouro().getLogradouro());
            end.setNumero(u.getLogradouro().getNumero());
            end.setComplemento(u.getLogradouro().getComplemento());
            end.setBairro(u.getLogradouro().getBairro());
            end.setMunicipio(u.getLogradouro().getMunicipio());
            end.setUf(u.getLogradouro().getUf());
            end.setCep(u.getLogradouro().getCep());
            dto.setEndereco(end);
        }

        return dto;
    }
}
