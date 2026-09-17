package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.Logradouro;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LogradouroRepository extends BaseRepository<Logradouro, UUID> {
}
