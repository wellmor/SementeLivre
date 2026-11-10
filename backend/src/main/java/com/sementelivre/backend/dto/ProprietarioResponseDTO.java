package com.sementelivre.backend.dto;

import com.sementelivre.backend.entity.enums.TipoDocumento;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProprietarioResponseDTO {

    private UUID id;
    private TipoDocumento tipoDocumento;
    private String documento;
    private String nome;
    private String telefone;
    private String email;
    private LogradouroDTO endereco;
    private LocalDateTime dataCadastro;
    private LocalDateTime dataUltimaAlteracao;
    private String tipoPessoa;
    private String rg;
    private boolean exibirNoSitePublico;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LogradouroDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(LogradouroDTO endereco) {
        this.endereco = endereco;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataUltimaAlteracao() {
        return dataUltimaAlteracao;
    }

    public void setDataUltimaAlteracao(LocalDateTime dataUltimaAlteracao) {
        this.dataUltimaAlteracao = dataUltimaAlteracao;
    }

    public String getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(String tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

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
