package com.example.demo.dto;

import com.example.demo.model.TipoDocumento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PessoaCreateDTO {

    @Schema(description = "Tipo do documento de identificação", example = "CPF")
    @NotNull(message = "O tipo do documento é obrigatório")
    private TipoDocumento tipoDocumento;

    @Schema(description = "Número do documento (CPF com 11 dígitos ou CNPJ com 14)", example = "12345678901")
    @NotBlank(message = "O documento é obrigatório")
    private String documento;

    @Schema(description = "Nome completo da pessoa", example = "Maria da Silva")
    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @Schema(description = "Telefone de contato", example = "(32) 99999-0000")
    private String telefone;

    @Schema(description = "Email usado para login", example = "maria@exemplo.com")
    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email deve ser válido")
    private String email;

    @Schema(description = "Senha de acesso (mínimo 8 caracteres)", example = "senhaSegura123")
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    private String senha;

    @Schema(description = "Tipo da pessoa", example = "USUARIO", allowableValues = {"USUARIO", "PROPRIETARIO", "ADMIN"})
    private String tipoPessoa;

    @Schema(description = "RG - obrigatório apenas quando tipoPessoa = PROPRIETARIO", example = "MG-123456")
    private String rg;

    @Schema(description = "Nível de acesso - obrigatório apenas quando tipoPessoa = ADMIN", example = "SUPER_ADMIN")
    private String nivelAcesso;

    @AssertTrue(message = "Tamanho do documento inválido para o tipo especificado")
    public boolean isDocumentoValido() {
        if (tipoDocumento == null || documento == null) {
            return true;
        }
        if (tipoDocumento == TipoDocumento.CPF && documento.length() != 11) {
            return false;
        }
        if (tipoDocumento == TipoDocumento.CNPJ && documento.length() != 14) {
            return false;
        }
        return true;
    }

    // Getters and Setters

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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
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

    public String getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }
}
