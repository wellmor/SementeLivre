package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.Role;
import com.sementelivre.backend.entity.enums.PerfilEnum;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends BaseRepository<Role, UUID> {
    Optional<Role> findByNome(PerfilEnum nome);
}
