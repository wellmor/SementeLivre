package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.RefreshToken;
import com.sementelivre.backend.entity.Usuario;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends BaseRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
}
