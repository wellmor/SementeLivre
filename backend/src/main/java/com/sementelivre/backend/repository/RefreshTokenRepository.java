package com.sementelivre.backend.repository;

import com.sementelivre.backend.entity.RefreshToken;
import com.sementelivre.backend.entity.Usuario;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends BaseRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    /** Usado na exclusão da conta: tira os tokens também do contexto de persistência. */
    void deleteByUsuario(Usuario usuario);

    /**
     * Usado a cada login e refresh. Apaga direto no banco: o deleteBy derivado
     * carrega os tokens e apaga um por um exigindo 1 linha afetada, e em dois
     * logins simultâneos da mesma conta o segundo achava a linha já apagada e
     * respondia 500. O flush antes grava a revogação pendente da rotação do refresh.
     */
    @Modifying(flushAutomatically = true)
    @Query("delete from RefreshToken r where r.usuario = :usuario")
    void apagarTodosDoUsuario(@Param("usuario") Usuario usuario);
}
