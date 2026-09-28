package com.sementelivre.backend.entity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * Verifica se o Proprietario tem algum Pedido vinculado como recebedor,
     * usado para bloquear exclusão de Propriedade com dependências (issue #63).
     *
     * Pedido tem dois vínculos com pessoa: usuarioSolicitante (quem fez o
     * pedido) e proprietarioRecebedor (quem vai atender/entregar). Aqui
     * verificamos proprietarioRecebedor, porque o contexto da exclusão é a
     * posse da propriedade pelo Proprietario — o pedido que ele recebeu como
     * dono é o que representa negócio ativo ligado a essa posse, não o pedido
     * que ele eventualmente fez como comprador.
     */
    boolean existsByProprietarioRecebedorId(UUID proprietarioId);

    /**
     * Exclui os pedidos em que a pessoa é solicitante ou recebedora. Os itens
     * caem por ON DELETE CASCADE na migration, então não precisam sair antes.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Pedido p where p.usuarioSolicitante.id = :id or p.proprietarioRecebedor.id = :id")
    void deleteAllEnvolvendoPessoa(@Param("id") UUID id);
}
