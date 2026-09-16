package com.sementelivre.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PessoaUpdateRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @Size(max = 15)
    private String telefone;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail com formato inválido")
    private String email;

    @Valid
    private LogradouroDTO endereco;

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
}
