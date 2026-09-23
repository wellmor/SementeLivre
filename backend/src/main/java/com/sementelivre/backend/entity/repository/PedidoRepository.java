package com.sementelivre.backend.entity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sementelivre.backend.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    // Carrega os itens junto para evitar N+1 na listagem.
    // distinct porque o join com itens multiplica as linhas de pedido.
    //lista pedidos isolando apenas os que o proprietario recebeu
    @Query("""
            select distinct p from Pedido p
            left join fetch p.itens i
            left join fetch i.produto
            where p.proprietarioRecebedor.id = :proprietarioId
            """)
    List<Pedido> findAllByProprietarioRecebedorId(@Param("proprietarioId") UUID proprietarioId);

    //para uso interno (admin)
    @Query("""
            select distinct p from Pedido p
            left join fetch p.itens i
            left join fetch i.produto
            where p.usuarioSolicitante.id = :usuarioId
            """)
    List<Pedido> findAllByUsuarioSolicitanteId(@Param("usuarioId") UUID usuarioId);

    @Query("""
            select p from Pedido p
            left join fetch p.itens i
            left join fetch i.produto
            where p.id = :id
            """)
    Optional<Pedido> findByIdComItens(@Param("id") UUID id);
}
