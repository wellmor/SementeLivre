package com.example.demo.repository;

import com.example.demo.model.Proprietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProprietarioRepository extends JpaRepository<Proprietario, UUID> {
    boolean existsByRg(String rg);
}
