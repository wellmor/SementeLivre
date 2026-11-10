package com.sementelivre.backend.dto;

import java.util.UUID;

import com.sementelivre.backend.entity.enums.Disponibilidade;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.TipoProduto;

/**
 * Semente/muda de um produtor como ela aparece no site publico (issue #91).
 *
 * DECISAO A REVISAR: preco e quantidade NAO sao expostos. A #91 pede "as
 * sementes cultivadas" do produtor, nao a oferta comercial; preco e saldo de
 * estoque sao dado de negocio do produtor e ficam para um endpoint
 * autenticado, se o produto quiser. O campo disponibilidade fica, porque e o
 * que diz ao visitante se e troca, venda, doacao ou negociacao.
 */
public record SementePublicaResponseDTO(

        UUID produtoId,
        String nomePopular,
        String nomeCientifico,
        FormatoProduto formato,
        TipoProduto tipo,
        String familiaBotanica,
        String urlFoto,
        Disponibilidade disponibilidade

) {
}
