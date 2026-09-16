package com.example.demo.repository;

import com.example.demo.model.Itens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface  ItensRepository extends JpaRepository<Itens, UUID> {
    List<Itens> findByPedidoId(UUID pedidoId);
    List<Itens> findByProdutoId(UUID produtoId);
}
