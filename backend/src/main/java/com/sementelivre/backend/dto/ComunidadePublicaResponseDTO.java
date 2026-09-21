package com.sementelivre.backend.dto;

import java.util.UUID;

/**
 * Comunidade como ela aparece no perfil publico de um produtor (issue #91).
 * Deliberadamente reduzida a identificacao: status, endereco e datas de
 * solicitacao/aprovacao sao informacao interna e ficam fora.
 */
public record ComunidadePublicaResponseDTO(

        UUID id,
        String nome

) {
}
