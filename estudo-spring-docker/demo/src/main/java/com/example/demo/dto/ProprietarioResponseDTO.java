package com.example.demo.dto;

public class ProprietarioResponseDTO extends PessoaResponseDTO {

    private String rg;
    private Boolean exibirNoSitePublico;

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public Boolean getExibirNoSitePublico() {
        return exibirNoSitePublico;
    }

    public void setExibirNoSitePublico(Boolean exibirNoSitePublico) {
        this.exibirNoSitePublico = exibirNoSitePublico;
    }
}
