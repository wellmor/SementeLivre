package com.exemplo.relatorios;

import java.time.LocalDate;

public class Produto {

    private String nomePopular;
    private String tipo;
    private String especie;
    private String formato;
    private LocalDate dataInclusao;

    public Produto() {
    }

    public Produto(String nomePopular, String tipo, String especie, String formato, LocalDate dataInclusao) {
        this.nomePopular = nomePopular;
        this.tipo = tipo;
        this.especie = especie;
        this.formato = formato;
        this.dataInclusao = dataInclusao;
    }

    public String getNomePopular() {
        return nomePopular;
    }

    public void setNomePopular(String nomePopular) {
        this.nomePopular = nomePopular;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public LocalDate getDataInclusao() {
        return dataInclusao;
    }

    public void setDataInclusao(LocalDate dataInclusao) {
        this.dataInclusao = dataInclusao;
    }
}
