package com.sementelivre.backend.dto;

import com.sementelivre.backend.entity.enums.TipoDocumento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UsuarioCreateRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotNull(message = "O tipo de documento é obrigatório")
    private TipoDocumento tipoDocumento;

    @NotBlank(message = "O documento é obrigatório")
    private String documento;

    private String telefone;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail com formato inválido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @NotNull(message = "O endereço é obrigatório")
    @Valid
    private LogradouroDTO endereco;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public LogradouroDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(LogradouroDTO endereco) {
        this.endereco = endereco;
    }
}
