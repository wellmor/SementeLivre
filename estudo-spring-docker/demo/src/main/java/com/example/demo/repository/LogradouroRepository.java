package com.example.demo.repository;

import com.example.demo.model.Logradouro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LogradouroRepository extends JpaRepository<Logradouro, UUID> {
}
