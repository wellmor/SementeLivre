package com.sementelivre.backend.entity.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sementelivre.backend.entity.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, UUID> {
}