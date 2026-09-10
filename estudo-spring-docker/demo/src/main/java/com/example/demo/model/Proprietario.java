package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "proprietario_t")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class Proprietario extends Pessoa {

    @Column(nullable = false, unique = true, length = 20)
    private String rg;

    @Column(name = "exibir_no_site_publico", nullable = false)
    private boolean exibirNoSitePublico = false;

    // Getters and Setters

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public boolean isExibirNoSitePublico() {
        return exibirNoSitePublico;
    }

    public void setExibirNoSitePublico(boolean exibirNoSitePublico) {
        this.exibirNoSitePublico = exibirNoSitePublico;
    }
}
