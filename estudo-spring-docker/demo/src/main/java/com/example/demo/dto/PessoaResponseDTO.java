package com.example.demo.dto;

import com.example.demo.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public class PessoaResponseDTO {

    @Schema(description = "Identificador único da pessoa", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @Schema(description = "Tipo do documento de identificação", example = "CPF")
    private TipoDocumento tipoDocumento;

    @Schema(description = "Número do documento", example = "12345678901")
    private String documento;

    @Schema(description = "Nome completo da pessoa", example = "Maria da Silva")
    private String nome;

    @Schema(description = "Telefone de contato", example = "(32) 99999-0000")
    private String telefone;

    @Schema(description = "Email cadastrado", example = "maria@exemplo.com")
    private String email;

    @Schema(description = "Data em que a pessoa foi cadastrada", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCadastro;

    @Schema(description = "Data da última alteração do cadastro", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataUltimaAlteracao;

    @Schema(description = "Tipo da pessoa", example = "USUARIO", allowableValues = {"USUARIO", "PROPRIETARIO", "ADMIN"})
    private String tipoPessoa;

    // Getters and Setters

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
}
