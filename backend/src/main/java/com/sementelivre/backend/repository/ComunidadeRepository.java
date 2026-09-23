package com.sementelivre.backend.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.sementelivre.backend.entity.Comunidade;

@Repository
public interface ComunidadeRepository extends BaseRepository<Comunidade, UUID> {
    
    @Override
    @EntityGraph(attributePaths = {"logradouro"})
    List<Comunidade> findAll();
}
