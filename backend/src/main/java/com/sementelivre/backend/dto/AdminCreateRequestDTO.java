package com.sementelivre.backend.dto;

import com.sementelivre.backend.entity.enums.NivelAcesso;
import com.sementelivre.backend.entity.enums.TipoDocumento;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Novo admin, cadastrado por um SUPER_ADMIN. A senha é provisória: quem cria
 * passa para a pessoa, que troca no perfil (ou pelo "esqueci a senha").
 */
public record AdminCreateRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome pode ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "O e-mail pode ter no máximo 255 caracteres")
        String email,

        @NotNull(message = "O tipo de documento é obrigatório")
        TipoDocumento tipoDocumento,

        @NotBlank(message = "O documento é obrigatório")
        String documento,

        // Mesma regra da troca de senha; o BCrypt considera no máximo 72 bytes
        @NotBlank(message = "A senha provisória é obrigatória")
        @Size(min = 8, max = 72, message = "A senha provisória deve ter entre 8 e 72 caracteres")
        String senha,

        // Vazio = ADMIN. Só ADMIN ou SUPER_ADMIN (MODERADOR não tem tela nem regra própria).
        NivelAcesso nivelAcesso
) {
}
