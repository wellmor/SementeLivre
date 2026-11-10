package com.sementelivre.backend.dto;

import java.util.List;
import java.util.UUID;

/**
 * Perfil publico de um produtor (issue #91), servido sem autenticacao.
 *
 * LGPD -- este record e a fronteira do que sai para o site publico. NAO
 * adicione aqui: documento, tipoDocumento, rg, telefone, email, senhaHash,
 * exibirNoSitePublico, endereco completo (apenas o municipio) nem datas de
 * auditoria. A entidade Proprietario/Pessoa nunca deve ser serializada
 * diretamente por um endpoint publico.
 *
 * municipio e nulo quando o produtor nao tem logradouro cadastrado
 * (Pessoa.logradouro e opcional).
 */
public record ProdutorPublicoResponseDTO(

        UUID id,
        String nome,
        String municipio,
        List<ComunidadePublicaResponseDTO> comunidades,
        List<SementePublicaResponseDTO> sementes

) {
}
