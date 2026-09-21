package com.sementelivre.backend.entity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sementelivre.backend.entity.Estoque;

import jakarta.persistence.LockModeType;

public interface EstoqueRepository extends JpaRepository<Estoque, UUID> {

    /**
     * Localiza o estoque de um produto sob um proprietario. A tabela tem
     * unique (proprietario_id, produto_id), entao o resultado e no maximo um.
     *
     * O lock pessimista e necessario porque a baixa de estoque e um
     * read-modify-write: sem ele, dois pedidos confirmados ao mesmo tempo leem
     * a mesma quantidade e a segunda escrita sobrescreve a primeira, deixando
     * o estoque maior do que deveria (lost update).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e from Estoque e
            where e.proprietario.id = :proprietarioId
              and e.produto.id = :produtoId
            """)
    Optional<Estoque> findParaAtualizacao(
            @Param("proprietarioId") UUID proprietarioId,
            @Param("produtoId") UUID produtoId);

    @Query("""
            select e from Estoque e
            where e.proprietario.id = :proprietarioId
              and e.produto.id = :produtoId
            """)
    Optional<Estoque> findByProprietarioIdAndProdutoId(
            @Param("proprietarioId") UUID proprietarioId,
            @Param("produtoId") UUID produtoId);

    /**
     * Estoques de um proprietario que podem ser exibidos no site publico
     * (issue #91).
     *
     * DECISAO A REVISAR: "disponivel publicamente" foi interpretado como
     * disponibilidade diferente de INDISPONIVEL, ou seja PARA_TROCA,
     * PARA_VENDA, PARA_DOACAO e A_NEGOCIAR entram na listagem. O modelo nao tem
     * hoje nenhuma flag propria de visibilidade por produto ou por estoque -- o
     * unico controle declarado e Proprietario.exibirNoSitePublico. Se o produto
     * quiser separar "esta disponivel para negociar" de "pode aparecer no
     * site", isso precisa virar campo proprio.
     *
     * O join fetch do Produto evita N+1: logo apos a consulta o service le
     * nome, formato, tipo, familia e foto de cada produto para montar o DTO.
     */
    @Query("""
            select e from Estoque e
            join fetch e.produto p
            where e.proprietario.id = :proprietarioId
              and e.disponibilidade <> com.sementelivre.backend.entity.enums.Disponibilidade.INDISPONIVEL
            order by p.nomePopular
            """)
    List<Estoque> findVisiveisNoSitePublico(@Param("proprietarioId") UUID proprietarioId);
}
