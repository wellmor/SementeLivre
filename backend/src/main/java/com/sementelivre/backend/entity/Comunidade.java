package com.sementelivre.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.StatusComunidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "comunidade_t")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Comunidade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @ManyToOne
    @JoinColumn(name = "logradouro_id", nullable = false)
    private Logradouro logradouro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusComunidade status;

    @Column(nullable = false)
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAprovacao;



}
