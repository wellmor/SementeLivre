package com.sementelivre.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sementelivre.backend.entity.Comunidade;

public interface ComunidadeRepository extends BaseRepository<Comunidade, UUID> {
    // Aqui você pode adicionar métodos de consulta personalizados, se necessário


    
}
