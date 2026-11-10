package com.sementelivre.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Schema(name = "PropriedadeDTO", description = "Dados de uma propriedade rural")
public class PropriedadeDTO {

    @NotNull
    @Schema(description = "Identificador da propriedade", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank
    @Schema(description = "Nome da propriedade", example = "Sítio Boa Vista", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Positive
    @Schema(description = "Tamanho da propriedade em hectares", example = "10.5", minimum = "0")
    private BigDecimal tamanhoHectares;

    @NotNull
    @Schema(description = "Data de cadastro", example = "2026-09-09T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime dataCadastro;

    @NotNull
    @Schema(description = "Data da última alteração", example = "2026-09-09T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime dataUltimaAlteracao;

    @NotBlank
    @Schema(description = "Nome do proprietário", example = "Maria da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    private String proprietarioNome;

    @NotNull
    @Schema(description = "Identificador da comunidade", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID comunidadeId;

    @NotBlank
    @Schema(description = "Nome da comunidade", example = "Comunidade Boa Esperança", requiredMode = Schema.RequiredMode.REQUIRED)
    private String comunidadeNome;

    @NotBlank
    @Schema(description = "UF do endereço", example = "MG", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logradouroUf;

    @NotBlank
    @Schema(description = "Município do endereço", example = "Rio Pomba", requiredMode = Schema.RequiredMode.REQUIRED)
    private String logradouroMunicipio;

}
