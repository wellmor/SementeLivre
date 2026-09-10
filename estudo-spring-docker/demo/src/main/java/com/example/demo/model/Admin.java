package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_t")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class Admin extends Pessoa {

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_acesso", nullable = false, length = 20)
    private NivelAcesso nivelAcesso = NivelAcesso.ADMIN;

    // Getters and Setters

    public NivelAcesso getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(NivelAcesso nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}
