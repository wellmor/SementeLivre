package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Propriedade;
import com.sementelivre.backend.entity.enums.StatusComunidade;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropriedadeRepository extends BaseRepository<Propriedade, UUID> {

    List<Propriedade> findByProprietarioIdOrderByDataCadastroDesc(UUID proprietarioId);

    /**
     * Propriedades dos proprietários em comunidades com o status pedido, da
     * mais recente para a mais antiga, com a comunidade e o endereço dela.
     * O catálogo público usa a primeira de cada proprietário como comunidade
     * das ofertas sem comunidade de origem.
     */
    @Query("""
            select p from Propriedade p
            join fetch p.comunidade c
            join fetch c.logradouro
            where p.proprietario.id in :proprietarioIds
              and c.status = :status
            order by p.dataCadastro desc
            """)
    List<Propriedade> findByProprietariosEStatusDaComunidade(
            @Param("proprietarioIds") Collection<UUID> proprietarioIds,
            @Param("status") StatusComunidade status);

    boolean existsByIdAndProprietarioId(UUID id, UUID proprietarioId);

    /**
     * Comunidades de um produtor (issue #91).
     *
     * Nao existe relacao direta entre Proprietario e Comunidade: o vinculo
     * passa por Propriedade, que tem @ManyToOne para os dois. Como nenhum dos
     * lados expoe @OneToMany, o caminho so existe por consulta.
     *
     * O distinct e necessario porque um produtor pode ter varias propriedades
     * na mesma comunidade.
     */
    @Query("""
            select distinct c from Propriedade p
            join p.comunidade c
            where p.proprietario.id = :proprietarioId
            order by c.nome
            """)
    List<Comunidade> findComunidadesByProprietarioId(@Param("proprietarioId") UUID proprietarioId);

    @Query("select p.nome from Propriedade p where p.comunidade.id = :comunidadeId order by p.nome")
    List<String> findNomesByComunidadeId(@Param("comunidadeId") UUID comunidadeId);

    /** Remove as propriedades do produtor na exclusão da conta (LGPD). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Propriedade p where p.proprietario.id = :proprietarioId")
    void deleteAllByProprietarioId(@Param("proprietarioId") UUID proprietarioId);

    long countByProprietarioId(UUID proprietarioId);

    @Query("select p.logradouro.id from Propriedade p where p.proprietario.id = :proprietarioId")
    List<UUID> findLogradouroIdsByProprietarioId(@Param("proprietarioId") UUID proprietarioId);

}
