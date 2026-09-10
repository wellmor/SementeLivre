package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario_t")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class Usuario extends Pessoa {
    // No extra fields for now
}
