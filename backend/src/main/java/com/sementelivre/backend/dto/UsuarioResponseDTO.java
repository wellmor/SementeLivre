package com.sementelivre.backend.dto;

import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

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
    private Set<String> roles;

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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setTipoDocumento(usuario.getTipoDocumento());
        dto.setDocumento(usuario.getDocumento());
        dto.setNome(usuario.getNome());
        dto.setTelefone(usuario.getTelefone());
        dto.setEmail(usuario.getEmail());
        dto.setDataCadastro(usuario.getDataCadastro());
        dto.setDataUltimaAlteracao(usuario.getDataUltimaAlteracao());
        dto.setTipoPessoa("USUARIO");
        dto.setRoles(usuario.getRoles().stream()
                .map(role -> role.getNome().name())
                .collect(Collectors.toSet()));

        Logradouro l = usuario.getLogradouro();
        if (l != null) {
            LogradouroDTO endereco = new LogradouroDTO();
            endereco.setLogradouro(l.getLogradouro());
            endereco.setNumero(l.getNumero());
            endereco.setComplemento(l.getComplemento());
            endereco.setBairro(l.getBairro());
            endereco.setMunicipio(l.getMunicipio());
            endereco.setUf(l.getUf());
            endereco.setCep(l.getCep());
            dto.setEndereco(endereco);
        }

        return dto;
    }
}
