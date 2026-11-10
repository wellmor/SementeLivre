package com.sementelivre.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.StatusComunidade;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Schema(name = "ComunidadeDTO", description = "Dados de uma comunidade rural")
public class ComunidadeDTO {
    @NotNull
    @Schema(description = "Identificador da comunidade", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @NotBlank(message = "O nome não pode ser nulo")
    @Schema(description = "Nome da comunidade", example = "Comunidade Boa Esperança", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotNull
    @Schema(description = "Situação da comunidade", example = "ATIVA", requiredMode = Schema.RequiredMode.REQUIRED)
    private StatusComunidade status;

    @NotNull
    @Schema(description = "Data da solicitação", example = "2026-09-09T10:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime dataSolicitacao;

    @Schema(description = "Data da aprovação", example = "2026-09-10T14:00:00")
    private LocalDateTime dataAprovacao;

    @NotBlank
    @Schema(description = "Unidade federativa do endereço", example = "MG", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uf;

    @NotBlank
    @Schema(description = "Município do endereço", example = "Rio Pomba", requiredMode = Schema.RequiredMode.REQUIRED)
    private String municipio;


}
