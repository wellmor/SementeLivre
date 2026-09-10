package com.sementelivre.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
public class PropriedadeDTO {
    
    @NotNull
    private UUID id;

    @NotBlank
    private String nome;

    @Positive
    private BigDecimal tamanhoHectares;

    @NotNull
    private LocalDateTime dataCadastro;

    @NotNull
    private LocalDateTime dataUltimaAlteracao;

    //Dados do Proprietario de forma "achatada" relacionado a propriedade que está sendo referida
    @NotBlank
    private String proprietarioNome;

    //Dados da Comunidade de forma "achatada" relacionado a propriedade que está sendo referida
    @NotNull
    private UUID comunidadeId;

    @NotBlank
    private String comunidadeNome;

    //Dados do Logradouro de forma "achatada" relacionado a propriedade que está sendo referida
    @NotBlank
    private String logradouroUf;
    
    @NotBlank
    private String logradouroMunicipio;

}
