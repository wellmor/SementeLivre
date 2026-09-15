package com.sementelivre.backend.entity;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Essa classe serve unicamente como placeholder para o relacionamento com a entidade Propriedade
@Entity
@Table(name = "proprietario_t")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Proprietario { //Proprietario/Pessoa
    @Id
    private UUID id;

    private UUID pessoaId;
    
}
