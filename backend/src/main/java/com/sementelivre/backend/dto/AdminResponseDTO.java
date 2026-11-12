package com.sementelivre.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sementelivre.backend.entity.Admin;
import com.sementelivre.backend.entity.enums.NivelAcesso;

/** Admin na lista da área de administração. */
public record AdminResponseDTO(
        UUID id,
        String nome,
        String email,
        NivelAcesso nivelAcesso,
        LocalDateTime dataCadastro
) {
    public static AdminResponseDTO de(Admin admin) {
        return new AdminResponseDTO(admin.getId(), admin.getNome(), admin.getEmail(),
                admin.getNivelAcesso(), admin.getDataCadastro());
    }
}
