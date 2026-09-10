package com.example.demo.controller;

import com.example.demo.dto.PessoaCreateDTO;
import com.example.demo.dto.PessoaResponseDTO;
import com.example.demo.model.Admin;
import com.example.demo.model.Pessoa;
import com.example.demo.model.Proprietario;
import com.example.demo.model.Usuario;
import com.example.demo.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/pessoas")
@Tag(name = "Pessoas", description = "Cadastro e consulta de pessoas (usuários, proprietários e admins)")
public class PessoaController {

    private final PessoaService pessoaService;

    public PessoaController(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Cadastrar uma pessoa",
            description = "Cria uma nova pessoa. O campo tipoPessoa (USUARIO, PROPRIETARIO ou ADMIN) " +
                    "define quais campos extras (rg, nivelAcesso) são considerados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pessoa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ou documento já cadastrado")
    })
    public PessoaResponseDTO criar(@Valid @RequestBody PessoaCreateDTO dto) {
        Pessoa pessoaCriada = pessoaService.criar(dto);
        return mapToResponse(pessoaCriada);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar pessoa por ID",
            description = "Retorna os dados de uma pessoa cadastrada a partir do identificador (UUID)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada")
    })
    public PessoaResponseDTO buscarPorId(@PathVariable UUID id) {
        Pessoa pessoa = pessoaService.buscarPorId(id);
        return mapToResponse(pessoa);
    }

    private PessoaResponseDTO mapToResponse(Pessoa p) {
        PessoaResponseDTO dto = new PessoaResponseDTO();
        dto.setId(p.getId());
        dto.setTipoDocumento(p.getTipoDocumento());
        dto.setDocumento(p.getDocumento());
        dto.setNome(p.getNome());
        dto.setTelefone(p.getTelefone());
        dto.setEmail(p.getEmail());
        dto.setDataCadastro(p.getDataCadastro());
        dto.setDataUltimaAlteracao(p.getDataUltimaAlteracao());

        if (p instanceof Proprietario) {
            dto.setTipoPessoa("PROPRIETARIO");
        } else if (p instanceof Admin) {
            dto.setTipoPessoa("ADMIN");
        } else if (p instanceof Usuario) {
            dto.setTipoPessoa("USUARIO");
        }

        return dto;
    }
}
