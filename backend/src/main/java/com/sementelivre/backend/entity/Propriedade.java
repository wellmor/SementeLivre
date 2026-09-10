package com.sementelivre.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "propriedade_t")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Propriedade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    //Escolhi BigDecimal para representar o tamanho em hectares, pois ele é mais preciso para valores decimais e é comumente usado
    @Column(name = "tamanho_hectares", nullable = false)
    private BigDecimal tamanhoHectares;

    @ManyToOne
    @JoinColumn(name = "logradouro_id", nullable = false)
    private Logradouro logradouro;

    @ManyToOne
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Proprietario proprietario;

    @ManyToOne
    @JoinColumn(name = "comunidade_id", nullable = false)
    private Comunidade comunidade;

    //Utilizei CreationTimestamp e UpdateTimestamp para que o Hibernate gerencie automaticamente as datas de criação e atualização da entidade, garantindo que esses campos sejam preenchidos corretamente sem a necessidade de intervenção manual.
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dataUltimaAlteracao;
}
