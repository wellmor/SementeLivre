package com.sementelivre.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.Disponibilidade;
import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.Pesagem;
import com.sementelivre.backend.entity.enums.TipoProduto;

/**
 * Uma oferta do catalogo publico (site, sem login).
 *
 * LGPD (RNF-06) -- do produtor sai so o nome de exibicao, e apenas quando ele
 * autorizou aparecer no site ({@code exibirNoSitePublico}); sem autorizacao,
 * {@code nomeProdutor} vem nulo. Nao adicione aqui id, documento, contato,
 * endereco nem qualquer outro dado pessoal do produtor.
 */
public record CatalogoProdutoResponseDTO(

        UUID id,
        String nomePopular,
        String nomeCientifico,
        String historico,
        String urlFoto,
        TipoProduto tipo,
        EspecieGeral especie,
        FormatoProduto formato,
        String familiaBotanica,

        UUID estoqueId,
        String descricao,
        Double preco,
        Double quantidade,
        Pesagem tipoPesagem,
        Disponibilidade disponibilidade,

        UUID comunidadeId,
        String comunidade,
        String municipio,
        String uf,
        boolean aceitaNegociacao,

        /* Nome de exibicao do produtor; nulo quando ele nao autorizou aparecer no site. */
        String nomeProdutor,
        /* Quando a semente/muda entrou no sistema (Produto.dataInclusao). */
        LocalDateTime dataInclusao,
        /* Ultima mudanca no produto ou na oferta (preco, saldo, disponibilidade...). */
        LocalDateTime dataUltimaAlteracao
) {
}