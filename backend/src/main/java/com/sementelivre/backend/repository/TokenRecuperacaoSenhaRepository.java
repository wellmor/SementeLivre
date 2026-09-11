package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.TokenRecuperacaoSenha;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRecuperacaoSenhaRepository extends BaseRepository<TokenRecuperacaoSenha, UUID> {
    Optional<TokenRecuperacaoSenha> findByToken(String token);
}
