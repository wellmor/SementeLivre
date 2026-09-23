package com.sementelivre.backend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.TipoProduto;

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
@Table(name = "produto_t")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome_popular", nullable = false, length = 150)
    private String nomePopular;

    @Column(name = "nome_cientifico", length = 150)
    private String nomeCientifico;

    @Column(columnDefinition = "TEXT")
    private String historico;

    @Column(name = "url_foto", length = 500)
    private String urlFoto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoProduto tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EspecieGeral especie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormatoProduto formato;

    @Column(name = "familia_botanica", length = 150)
    private String familiaBotanica;

    @ManyToOne
    @JoinColumn(name = "comunidade_origem_id")
    private Comunidade comunidadeOrigem;

    @Column(name = "data_inclusao", nullable = false)
    private LocalDateTime dataInclusao;

    @Column(name = "data_ultima_alteracao")
    private LocalDateTime dataUltimaAlteracao;
}