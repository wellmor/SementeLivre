package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.Proprietario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProprietarioRepository extends BaseRepository<Proprietario, UUID> {
    boolean existsByRg(String rg);

    /**
     * Busca um produtor apenas se ele optou por aparecer no site publico
     * (issue #91). Quem nao optou e tratado como inexistente pela camada de
     * cima, para nao revelar que a conta existe.
     *
     * O left join fetch do logradouro e proposital: Pessoa.logradouro e LAZY e
     * o municipio entra na resposta publica. Sem o fetch, a leitura dependeria
     * do open-in-view estar ligado.
     */
    @Query("""
            select p from Proprietario p
            left join fetch p.logradouro
            where p.id = :id
              and p.exibirNoSitePublico = true
            """)
    Optional<Proprietario> findByIdAndExibirNoSitePublicoTrue(@Param("id") UUID id);
}
