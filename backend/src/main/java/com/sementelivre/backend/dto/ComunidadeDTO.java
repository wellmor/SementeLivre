package com.sementelivre.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.StatusComunidade;

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
public class ComunidadeDTO {
    @NotNull
    private UUID id;

    @NotBlank(message = "O nome não pode ser nulo")
    private String nome;

    @NotNull
    private StatusComunidade status;

    @NotNull
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAprovacao;

    //Aqui está os dados do Logradouro (de forma "achatada") relacionado comunidade que está sendo referida
    @NotBlank
    private String uf;

    @NotBlank
    private String municipio;


}
