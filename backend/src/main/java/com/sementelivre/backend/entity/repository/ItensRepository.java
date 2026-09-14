package com.sementelivre.backend.entity.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sementelivre.backend.entity.Itens;

public interface ItensRepository extends JpaRepository<Itens, UUID> {
}
