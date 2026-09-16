package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.Proprietario;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProprietarioRepository extends BaseRepository<Proprietario, UUID> {
    boolean existsByRg(String rg);
}
