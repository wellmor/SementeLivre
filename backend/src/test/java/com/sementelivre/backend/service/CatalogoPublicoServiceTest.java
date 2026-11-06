package com.sementelivre.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sementelivre.backend.dto.CatalogoProdutoResponseDTO;
import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Estoque;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Produto;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.enums.Disponibilidade;
import com.sementelivre.backend.entity.enums.Pesagem;
import com.sementelivre.backend.entity.enums.StatusComunidade;
import com.sementelivre.backend.entity.enums.TipoProduto;
import com.sementelivre.backend.repository.EstoqueRepository;
import com.sementelivre.backend.repository.PropriedadeRepository;

/**
 * Campos do catalogo publico usados pelos filtros do site (RF-04 / CDU-01):
 * nome do produtor (so com autorizacao, LGPD) e datas de inclusao e de ultima
 * alteracao.
 *
 * Mockito puro, como o ProdutorPublicoServiceTest: a consulta em si e coberta
 * pelos testes de integracao com Postgres (Catalogo*IntegrationTest).
 */
@ExtendWith(MockitoExtension.class)
class CatalogoPublicoServiceTest {

    private static final LocalDateTime INCLUSAO = LocalDateTime.of(2026, 9, 1, 8, 0);

    @Mock
    private EstoqueRepository estoqueRepository;

    @Mock
    private PropriedadeRepository propriedadeRepository;

    private CatalogoPublicoService catalogoPublicoService;

    private final Pageable pagina = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        catalogoPublicoService = new CatalogoPublicoService(estoqueRepository, propriedadeRepository);
    }

    // ---------- nome do produtor (LGPD) ----------

    @Test
    void deveExporNomeDoProdutorQueAutorizouAparecerNoSite() {
        Estoque oferta = oferta(produtor("  Maria das Sementes ", true), produto(INCLUSAO, null), null, null);
        devolver(oferta);

        CatalogoProdutoResponseDTO dto = listarUnica();

        assertThat(dto.nomeProdutor()).isEqualTo("Maria das Sementes");
    }

    @Test
    void naoDeveExporNomeDoProdutorQueNaoAutorizou() {
        Estoque oferta = oferta(produtor("João Privado", false), produto(INCLUSAO, null), null, null);
        devolver(oferta);

        CatalogoProdutoResponseDTO dto = listarUnica();

        // A oferta continua no catálogo; só o nome fica de fora
        assertThat(dto.estoqueId()).isEqualTo(oferta.getId());
        assertThat(dto.nomeProdutor()).isNull();
    }

    @Test
    void nomeEmBrancoVemNulo() {
        Estoque oferta = oferta(produtor("   ", true), produto(INCLUSAO, null), null, null);
        devolver(oferta);

        assertThat(listarUnica().nomeProdutor()).isNull();
    }

    @Test
    void dtoDoCatalogoNaoTemOutrosDadosPessoaisDoProdutor() {
        List<String> campos = java.util.Arrays.stream(CatalogoProdutoResponseDTO.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toList();

        assertThat(campos).contains("nomeProdutor");
        assertThat(campos).doesNotContain(
                "produtorId", "proprietarioId", "email", "telefone", "documento", "rg",
                "logradouro", "cep", "exibirNoSitePublico");
    }

    // ---------- datas ----------

    @Test
    void deveExporDataDeInclusaoDoProduto() {
        Estoque oferta = oferta(produtor("Maria", true), produto(INCLUSAO, null), null, null);
        devolver(oferta);

        assertThat(listarUnica().dataInclusao()).isEqualTo(INCLUSAO);
    }

    @Test
    void ultimaAlteracaoEhAMaisRecenteEntreProdutoEOferta() {
        LocalDateTime produtoAlterado = INCLUSAO.plusDays(2);
        LocalDateTime ofertaAtualizada = INCLUSAO.plusDays(10);
        LocalDateTime movimentada = INCLUSAO.plusDays(5);
        Estoque oferta = oferta(produtor("Maria", true), produto(INCLUSAO, produtoAlterado),
                ofertaAtualizada, movimentada);
        devolver(oferta);

        assertThat(listarUnica().dataUltimaAlteracao()).isEqualTo(ofertaAtualizada);
    }

    @Test
    void ultimaAlteracaoConsideraOProdutoQuandoEleMudouDepoisDaOferta() {
        LocalDateTime produtoAlterado = INCLUSAO.plusDays(20);
        Estoque oferta = oferta(produtor("Maria", true), produto(INCLUSAO, produtoAlterado),
                INCLUSAO.plusDays(1), INCLUSAO.plusDays(1));
        devolver(oferta);

        assertThat(listarUnica().dataUltimaAlteracao()).isEqualTo(produtoAlterado);
    }

    @Test
    void semNenhumaAlteracaoAUltimaAlteracaoEhAInclusao() {
        Estoque oferta = oferta(produtor("Maria", true), produto(INCLUSAO, null), null, null);
        devolver(oferta);

        assertThat(listarUnica().dataUltimaAlteracao()).isEqualTo(INCLUSAO);
    }

    // ---------- apoio ----------

    private void devolver(Estoque... ofertas) {
        when(estoqueRepository.buscarCatalogoPublico(
                anyList(), anyString(), anyList(), anyList(), anyList(), anyString(), anyString(),
                any(StatusComunidade.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ofertas), pagina, ofertas.length));
    }

    private CatalogoProdutoResponseDTO listarUnica() {
        List<CatalogoProdutoResponseDTO> conteudo = catalogoPublicoService
                .listar(null, null, null, null, null, null, pagina)
                .getContent();
        assertThat(conteudo).hasSize(1);
        return conteudo.get(0);
    }

    private static Proprietario produtor(String nome, boolean exibirNoSitePublico) {
        Proprietario produtor = new Proprietario();
        produtor.setId(UUID.randomUUID());
        produtor.setNome(nome);
        produtor.setEmail("produtor@sementelivre.test");
        produtor.setTelefone("32999990000");
        produtor.setExibirNoSitePublico(exibirNoSitePublico);
        return produtor;
    }

    private static Produto produto(LocalDateTime inclusao, LocalDateTime ultimaAlteracao) {
        Comunidade comunidade = Comunidade.builder()
                .id(UUID.randomUUID())
                .nome("Quilombo São José")
                .logradouro(Logradouro.builder().municipio("Rio Pomba").uf("MG").build())
                .build();

        return Produto.builder()
                .id(UUID.randomUUID())
                .nomePopular("Feijão Crioulo")
                .tipo(TipoProduto.LEGUMINOSA)
                .comunidadeOrigem(comunidade)
                .dataInclusao(inclusao)
                .dataUltimaAlteracao(ultimaAlteracao)
                .build();
    }

    private static Estoque oferta(Proprietario produtor, Produto produto,
            LocalDateTime ultimaAtualizacao, LocalDateTime movimentacao) {
        return Estoque.builder()
                .id(UUID.randomUUID())
                .proprietario(produtor)
                .produto(produto)
                .preco(5.0)
                .quantidade(10.0)
                .tipoPesagem(Pesagem.KG)
                .disponibilidade(Disponibilidade.PARA_VENDA)
                .dataUltimaAtualizacao(ultimaAtualizacao)
                .dataMovimentacao(movimentacao)
                .build();
    }
}
