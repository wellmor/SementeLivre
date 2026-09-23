package com.sementelivre.backend.entity.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sementelivre.backend.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
}