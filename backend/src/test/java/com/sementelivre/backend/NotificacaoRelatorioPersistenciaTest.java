package com.sementelivre.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.sementelivre.backend.entity.Notificacao;
import com.sementelivre.backend.entity.Pedido;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Relatorio;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.entity.enums.TipoRelatorio;
import com.sementelivre.backend.repository.NotificacaoRepository;
import com.sementelivre.backend.repository.RelatorioRepository;

import jakarta.persistence.EntityManager;

/**
 * Testes de persistência das entidades Notificacao e Relatorio (task #37).
 *
 * <p>Segue o mesmo formato do {@link PersistenciaTest} já existente:
 * {@code @DataJpaTest} sobe apenas a camada JPA (entidades + repositórios), com
 * o Hibernate criando o schema a partir das próprias entidades (ver
 * src/test/resources/application.yml). Ou seja, <b>se o mapeamento estiver
 * errado o teste falha logo na subida do contexto</b> — é exatamente essa a
 * verificação pedida no critério de aceite da task.</p>
 *
 * <p>Observação: o banco usado aqui é o H2 em memória. Ele valida a estrutura
 * do mapeamento (colunas, relacionamentos e a serialização do JSON), mas a
 * validação definitiva do tipo {@code jsonb} continua sendo contra o PostgreSQL
 * do docker-compose.</p>
 */
@DataJpaTest
class NotificacaoRelatorioPersistenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private RelatorioRepository relatorioRepository;

    /**
     * Cria um Proprietario novo a cada chamada. O id vem do @GeneratedValue de
     * Pessoa (preenchido no persist), e os demais campos sao os obrigatorios da
     * heranca TPT. Email e rg sao unicos por chamada porque
     * {@link #repositorioDeveListarSomenteNotificacoesNaoLidasDoProprietario()}
     * persiste dois proprietarios na mesma transacao.
     */
    private Proprietario novoProprietario() {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);

        Proprietario proprietario = new Proprietario();
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setDocumento("52998224725");
        proprietario.setNome("Proprietario " + sufixo);
        proprietario.setEmail("proprietario." + sufixo + "@teste.com");
        proprietario.setSenhaHash("hash123");
        proprietario.setRg("MG-" + sufixo);
        return proprietario;
    }

    @Test
    void deveSalvarNotificacaoLigadaAProprietarioEPedido() {
        // Arrange
        Proprietario proprietario = novoProprietario();
        Pedido pedido = Pedido.builder().build();

        Notificacao notificacao = Notificacao.builder()
                .titulo("Novo pedido recebido")
                .mensagem("Voce recebeu um pedido de 2 kg de Feijao Crioulo.")
                .proprietario(proprietario)
                .pedidoRelacionado(pedido)
                .build();

        // Act
        entityManager.persist(proprietario);
        entityManager.persist(pedido);
        entityManager.persist(notificacao);
        entityManager.flush();
        entityManager.clear();

        Notificacao salva = entityManager.find(Notificacao.class, notificacao.getId());

        // Assert
        assertNotNull(salva.getId(), "o id deve ser gerado pelo Hibernate");
        assertEquals("Novo pedido recebido", salva.getTitulo());
        assertFalse(salva.isLida(), "notificacao nova nasce como nao lida");
        assertNull(salva.getDataLeitura(), "sem leitura, a data de leitura fica nula");
        assertNotNull(salva.getDataGeracao(), "@CreationTimestamp deve preencher a data de geracao");
        assertEquals(proprietario.getId(), salva.getProprietario().getId());
        assertEquals(pedido.getId(), salva.getPedidoRelacionado().getId());
    }

    @Test
    void deveAceitarNotificacaoSemPedidoRelacionado() {
        // Arrange - o relacionamento com Pedido e opcional (a FK aceita nulo).
        Proprietario proprietario = novoProprietario();
        Notificacao notificacao = Notificacao.builder()
                .titulo("Aviso do sistema")
                .mensagem("Seu cadastro foi aprovado.")
                .proprietario(proprietario)
                .build();

        // Act
        entityManager.persist(proprietario);
        entityManager.persist(notificacao);
        entityManager.flush();
        entityManager.clear();

        Notificacao salva = entityManager.find(Notificacao.class, notificacao.getId());

        // Assert
        assertNotNull(salva);
        assertNull(salva.getPedidoRelacionado());
    }

    @Test
    void marcarComoLidaDevePreencherDataDeLeitura() {
        // Arrange
        Proprietario proprietario = novoProprietario();
        Notificacao notificacao = Notificacao.builder()
                .titulo("Novo pedido recebido")
                .mensagem("Detalhes do pedido.")
                .proprietario(proprietario)
                .build();

        entityManager.persist(proprietario);
        entityManager.persist(notificacao);
        entityManager.flush();

        // Act
        notificacao.marcarComoLida();
        entityManager.flush();
        entityManager.clear();

        Notificacao salva = entityManager.find(Notificacao.class, notificacao.getId());

        // Assert
        assertTrue(salva.isLida());
        assertNotNull(salva.getDataLeitura());
    }

    @Test
    void repositorioDeveListarSomenteNotificacoesNaoLidasDoProprietario() {
        // Arrange - duas notificacoes do proprietario A (uma lida) e uma do B.
        Proprietario proprietarioA = novoProprietario();
        Proprietario proprietarioB = novoProprietario();

        Notificacao naoLidaDeA = Notificacao.builder()
                .titulo("Pedido 1").mensagem("...").proprietario(proprietarioA).build();

        Notificacao lidaDeA = Notificacao.builder()
                .titulo("Pedido 2").mensagem("...").proprietario(proprietarioA).build();
        lidaDeA.marcarComoLida();

        Notificacao naoLidaDeB = Notificacao.builder()
                .titulo("Pedido 3").mensagem("...").proprietario(proprietarioB).build();

        entityManager.persist(proprietarioA);
        entityManager.persist(proprietarioB);
        entityManager.persist(naoLidaDeA);
        entityManager.persist(lidaDeA);
        entityManager.persist(naoLidaDeB);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Notificacao> naoLidas = notificacaoRepository
                .findByProprietarioIdAndLidaFalseOrderByDataGeracaoDesc(proprietarioA.getId());
        long total = notificacaoRepository.countByProprietarioIdAndLidaFalse(proprietarioA.getId());

        // Assert
        assertEquals(1, naoLidas.size(), "deve trazer so a nao lida do proprietario A");
        assertEquals("Pedido 1", naoLidas.get(0).getTitulo());
        assertEquals(1, total);
    }

    @Test
    void deveSalvarRelatorioComFiltrosEmJson() {
        // Arrange - este e o teste do criterio "campo JSONB mapeado corretamente".
        Proprietario proprietario = novoProprietario();

        Map<String, Object> filtros = Map.of(
                "dataInicio", "2026-01-01",
                "dataFim", "2026-03-31",
                "especie", "FEIJAO");

        Relatorio relatorio = Relatorio.builder()
                .tipo(TipoRelatorio.ESTOQUE_SEMENTES)
                .filtrosUtilizados(filtros)
                .proprietario(proprietario)
                .build();

        // Act
        entityManager.persist(proprietario);
        entityManager.persist(relatorio);
        entityManager.flush();
        entityManager.clear();

        Relatorio salvo = entityManager.find(Relatorio.class, relatorio.getId());

        // Assert - o Map precisa voltar do banco com o mesmo conteudo.
        assertNotNull(salvo.getId());
        assertEquals(TipoRelatorio.ESTOQUE_SEMENTES, salvo.getTipo());
        assertNotNull(salvo.getDataGeracao());
        assertEquals(proprietario.getId(), salvo.getProprietario().getId());
        assertEquals(3, salvo.getFiltrosUtilizados().size());
        assertEquals("FEIJAO", salvo.getFiltrosUtilizados().get("especie"));
        assertEquals("2026-01-01", salvo.getFiltrosUtilizados().get("dataInicio"));
    }

    @Test
    void deveAceitarRelatorioSemFiltros() {
        // Arrange - sem filtros significa "relatorio completo" (CDU-25).
        Proprietario proprietario = novoProprietario();
        Relatorio relatorio = Relatorio.builder()
                .tipo(TipoRelatorio.PEDIDOS_REALIZADOS)
                .proprietario(proprietario)
                .build();

        // Act
        entityManager.persist(proprietario);
        entityManager.persist(relatorio);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Relatorio salvo = entityManager.find(Relatorio.class, relatorio.getId());
        assertNotNull(salvo);
        assertNull(salvo.getFiltrosUtilizados());
    }

    @Test
    void repositorioDeveFiltrarRelatoriosPorTipo() {
        // Arrange
        Proprietario proprietario = novoProprietario();

        Relatorio estoque = Relatorio.builder()
                .tipo(TipoRelatorio.ESTOQUE_SEMENTES).proprietario(proprietario).build();
        Relatorio pedidos = Relatorio.builder()
                .tipo(TipoRelatorio.PEDIDOS_REALIZADOS).proprietario(proprietario).build();

        entityManager.persist(proprietario);
        entityManager.persist(estoque);
        entityManager.persist(pedidos);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Relatorio> somenteEstoque = relatorioRepository
                .findByProprietarioIdAndTipoOrderByDataGeracaoDesc(
                        proprietario.getId(), TipoRelatorio.ESTOQUE_SEMENTES);

        // Assert
        assertEquals(1, somenteEstoque.size());
        assertEquals(TipoRelatorio.ESTOQUE_SEMENTES, somenteEstoque.get(0).getTipo());
    }
}
