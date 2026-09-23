package com.sementelivre.backend.entity.enums;

/**
 * Tipos de relatório que um proprietário pode gerar no sistema (CDU-25 /
 * CDU-17, requisito RF-07).
 *
 * <p>Os valores seguem exatamente o que está definido no modelo de dados
 * (docs/modelo-dados, tabela {@code relatorio_t}, coluna {@code tipo}).
 * Como a coluna é gravada com {@code @Enumerated(EnumType.STRING)}, o nome
 * escrito aqui é literalmente o texto que vai para o banco — por isso
 * <b>renomear uma constante quebra os registros já salvos</b>. Para acrescentar
 * um novo tipo de relatório, adicione uma constante nova no fim da lista.</p>
 */
public enum TipoRelatorio {

    /** Relatório do estoque de sementes/mudas do proprietário. */
    ESTOQUE_SEMENTES,

    /** Relatório dos pedidos recebidos pelo proprietário. */
    PEDIDOS_REALIZADOS
}
