package com.sementelivre.backend.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sementelivre.backend.entity.Comunidade;

@Repository
public interface ComunidadeRepository extends BaseRepository<Comunidade, UUID> {
    
    @Override
    @EntityGraph(attributePaths = {"logradouro"})
    List<Comunidade> findAll();

    /**
    * Pesquisa e retorna uma lista de somente os nomes(String) das Comunidades para
     * ser utilizado na verificação de nomes iguais/parecidos (Issue #63)
    * */
    @Query("select c.nome from Comunidade c")
    List<String> findAllNames();

    /**
     * Nomes de todas as Comunidades, exceto a de id informado. Usado na checagem
     * de similaridade ao renomear, para a comunidade não colidir com ela mesma (issue #126).
     */
    @Query("select c.nome from Comunidade c where c.id <> :id")
    List<String> findAllNamesExceto(@Param("id") UUID id);
}
