package com.sementelivre.backend.dto;

import com.sementelivre.backend.entity.enums.StatusComunidade;

import java.time.LocalDateTime;
import java.util.UUID;

public record ComunidadeResponseDTO(

        UUID id,
        String nome,
        StatusComunidade status,
        LocalDateTime dataSolicitacao,
        LocalDateTime dataAprovacao,
        //Parte ligada ao Logradouro
        String logradouroUf,
        String logradouroMunicipio
) {
}
