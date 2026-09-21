package com.sementelivre.backend.dto;

import com.sementelivre.backend.entity.enums.StatusComunidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ComunidadeRequestDTO(

        @NotBlank(message = "Nome da Comunidade é obrigatório.")
        @Size(max = 150, message = "O nome pode ter no máximo 150 caracteres.")
        String nome,


        @NotNull(message = "O ID do Logradouro ligado a esta comunidade é obrigatório.")
        UUID logradouroId


) {
}