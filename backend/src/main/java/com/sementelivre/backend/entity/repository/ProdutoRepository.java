package com.sementelivre.backend.entity.repository;

import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sementelivre.backend.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    @Override
    @EntityGraph(attributePaths = {"comunidadeOrigem"})
    List<Produto> findAll();
}