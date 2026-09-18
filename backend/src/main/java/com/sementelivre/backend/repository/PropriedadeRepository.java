package com.sementelivre.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sementelivre.backend.entity.Propriedade;

public interface PropriedadeRepository extends BaseRepository<Propriedade, UUID> {
    // Aqui você pode adicionar métodos de consulta personalizados, se necessário
    
}
