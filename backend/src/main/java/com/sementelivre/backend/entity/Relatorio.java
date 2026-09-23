package com.sementelivre.backend.entity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.sementelivre.backend.entity.enums.TipoRelatorio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

/**
 * Registro histórico de um relatório gerado por um proprietário
 * (CDU-25 / CDU-17, requisito RF-07).
 *
 * <p>Esta entidade <b>não guarda o conteúdo</b> do relatório (o PDF ou o CSV):
 * ela guarda apenas o "pedido de geração" — qual tipo foi escolhido, quais
 * filtros foram aplicados e quando. O arquivo em si é produzido sob demanda
 * pelo serviço de exportação.</p>
 *
 * <p>Mapeia a tabela {@code relatorio_t} descrita em
 * docs/modelo-dados/Modelo-Conceitual-Banco-Dados.md.</p>
 */
@Entity
@Table(name = "relatorio_t")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // EnumType.STRING grava o nome da constante ("ESTOQUE_SEMENTES") na coluna.
    // Nunca use EnumType.ORDINAL: ele grava a posição (0, 1, 2...), e qualquer
    // reordenação do enum passaria a apontar para o valor errado.
    // Mesma abordagem já usada em Comunidade.status (StatusComunidade).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRelatorio tipo;

    /*
     * Campo JSONB — o ponto mais delicado deste mapeamento.
     *
     * Os filtros variam conforme o tipo de relatório (período, semente, tipo de
     * pedido...), então o modelo de dados optou por guardá-los como JSON em vez
     * de criar uma coluna para cada filtro possível.
     *
     * @JdbcTypeCode(SqlTypes.JSON) é o recurso nativo do Hibernate 6+ para
     * isso: ele serializa/desserializa o Map usando o Jackson (já presente no
     * projeto) e, no dialeto do PostgreSQL, cria a coluna como "jsonb"
     * automaticamente. Por isso NÃO fixamos columnDefinition = "jsonb" aqui —
     * deixar o dialeto decidir mantém o mapeamento portável para os testes.
     *
     * Aceita nulo: relatório sem filtros significa "todos os dados" (CDU-25).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filtros_utilizados")
    private Map<String, Object> filtrosUtilizados;

    @CreationTimestamp
    @Column(name = "data_geracao", nullable = false, updatable = false)
    private LocalDateTime dataGeracao;

    // Todo relatório pertence ao proprietário que o solicitou.
    // LAZY + optional = false pelos mesmos motivos explicados em Notificacao.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietario_id", nullable = false)
    private Proprietario proprietario;
}
