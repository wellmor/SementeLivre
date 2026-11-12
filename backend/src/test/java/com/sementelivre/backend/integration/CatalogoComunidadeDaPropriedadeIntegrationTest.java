package com.sementelivre.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

/**
 * Comunidade das ofertas no catálogo público (#100). O app não pede a
 * comunidade no cadastro do produto, então a oferta sem comunidade de origem
 * mostra a da propriedade mais recente do produtor numa comunidade ativa, e os
 * filtros por comunidade e município do site passam a encontrá-la.
 */
class CatalogoComunidadeDaPropriedadeIntegrationTest extends AbstractAuthIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String token, String corpo) {
        return req.header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private String criar(String url, String token, String corpo) throws Exception {
        String resposta = mockMvc.perform(json(post(url), token, corpo))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    /** Comunidade nova (pendente); aprovada pelo admin quando {@code aprovar}. */
    private String comunidade(String token, String nome, String municipio, boolean aprovar) throws Exception {
        String id = criar("/api/comunidades", token, """
                {"nome":"%s","endereco":{"logradouro":"Estrada Velha","municipio":"%s","uf":"MG"}}
                """.formatted(nome, municipio));
        if (aprovar) {
            mockMvc.perform(patch("/api/comunidades/" + id + "/aprovar").header(HttpHeaders.AUTHORIZATION, loginAdmin()))
                    .andExpect(status().isOk());
        }
        return id;
    }

    private void propriedade(Conta dono, String token, String comunidadeId) throws Exception {
        criar("/api/propriedades", token, """
                {"nome":"Sítio %s","tamanhoHectares":3,"proprietarioId":"%s","comunidadeId":"%s",
                 "endereco":{"logradouro":"Estrada","numero":"5","bairro":"Zona Rural","municipio":"Rio Pomba",
                             "uf":"MG","cep":"36180000"}}
                """.formatted(sufixoUnico(), dono.id(), comunidadeId));
    }

    /** Produto sem comunidade de origem (como o app cadastra) e a oferta dele. */
    private String oferta(Conta dono, String token, String nome) throws Exception {
        String produtoId = criar("/produtos", token, """
                {"nomePopular":"%s","nomeCientifico":"Teste","urlFoto":"/uploads/produtos/t.png","tipo":"LEGUMINOSA",
                 "especie":"FEIJAO","formato":"SEMENTE"}
                """.formatted(nome));
        criar("/estoques", token, """
                {"proprietarioId":"%s","produtoId":"%s","descricao":"Oferta","preco":10,"quantidade":50,
                 "tipoPesagem":"KG","disponibilidade":"PARA_VENDA","tipoMovimentacao":"ENTRADA"}
                """.formatted(dono.id(), produtoId));
        return produtoId;
    }

    /** Itens do catálogo público com esse nome de produto. */
    private List<Map<String, Object>> catalogo(String nome, String filtro, String valor) throws Exception {
        var req = get("/catalogo/produtos").param("nomePopular", nome).param("size", "50");
        if (filtro != null) {
            req.param(filtro, valor);
        }
        String resposta = mockMvc.perform(req).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.content");
    }

    @Test
    void ofertaSemComunidadeDeOrigemMostraADaPropriedadeEApareceNosFiltros() throws Exception {
        Conta dono = cadastrar();
        String token = login(dono.email(), SENHA);
        String comunidade = "Quilombo Catalogo " + sufixoUnico();
        propriedade(dono, token, comunidade(token, comunidade, "Mercês", true));
        String nome = "Feijão Catálogo " + sufixoUnico();
        String produtoId = oferta(dono, token, nome);

        List<Map<String, Object>> semFiltro = catalogo(nome, null, null);
        assertThat(semFiltro).hasSize(1);
        assertThat(semFiltro.get(0)).containsEntry("comunidade", comunidade).containsEntry("municipio", "Mercês");

        assertThat(catalogo(nome, "comunidade", comunidade)).hasSize(1);
        assertThat(catalogo(nome, "municipio", "Mercês")).hasSize(1);
        assertThat(catalogo(nome, "comunidade", "Outra Comunidade Qualquer")).isEmpty();
        assertThat(catalogo(nome, "municipio", "Ubá")).isEmpty();

        String detalhe = mockMvc.perform(get("/catalogo/produtos/" + produtoId))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(detalhe, "$.comunidade")).isEqualTo(comunidade);
        assertThat((String) JsonPath.read(detalhe, "$.municipio")).isEqualTo("Mercês");
    }

    @Test
    void comunidadePendenteOuRejeitadaNaoApareceNoSite() throws Exception {
        Conta dono = cadastrar();
        String token = login(dono.email(), SENHA);
        String pendente = "Comunidade Pendente " + sufixoUnico();
        propriedade(dono, token, comunidade(token, pendente, "Guarani", false));
        String nome = "Milho Pendente " + sufixoUnico();
        oferta(dono, token, nome);

        List<Map<String, Object>> itens = catalogo(nome, null, null);
        assertThat(itens).hasSize(1);
        assertThat(itens.get(0).get("comunidade")).isNull();
        assertThat(catalogo(nome, "comunidade", pendente)).isEmpty();
    }

    @Test
    void produtorSemComunidadeApareceNoCatalogo() throws Exception {
        Conta dono = cadastrar();
        String token = login(dono.email(), SENHA);
        criar("/api/propriedades", token, """
                {"nome":"Sítio Isolado %s","tamanhoHectares":3,"proprietarioId":"%s",
                 "endereco":{"logradouro":"Estrada","municipio":"Rio Pomba","uf":"MG"}}
                """.formatted(sufixoUnico(), dono.id()));
        String nome = "Fava Isolada " + sufixoUnico();
        String produtoId = oferta(dono, token, nome);

        List<Map<String, Object>> itens = catalogo(nome, null, null);
        assertThat(itens).hasSize(1);
        assertThat(itens.get(0).get("comunidade")).isNull();
        mockMvc.perform(get("/catalogo/produtos/" + produtoId)).andExpect(status().isOk());
    }

    @Test
    void valeAPropriedadeMaisRecenteEAComunidadeDeOrigemTemPrioridade() throws Exception {
        Conta dono = cadastrar();
        String token = login(dono.email(), SENHA);
        String antiga = "Comunidade Antiga " + sufixoUnico();
        String recente = "Comunidade Recente " + sufixoUnico();
        String antigaId = comunidade(token, antiga, "Tabuleiro", true);
        propriedade(dono, token, antigaId);
        propriedade(dono, token, comunidade(token, recente, "Silveirânia", true));

        String nomeSemOrigem = "Abóbora Sem Origem " + sufixoUnico();
        oferta(dono, token, nomeSemOrigem);
        assertThat(catalogo(nomeSemOrigem, null, null).get(0)).containsEntry("comunidade", recente);

        // Produto com comunidade de origem continua com ela. O POST /produtos não grava
        // esse campo (nenhum produto do app tem), então ela vai direto no banco
        String nomeComOrigem = "Abóbora Com Origem " + sufixoUnico();
        String comOrigemId = oferta(dono, token, nomeComOrigem);
        jdbcTemplate.update("UPDATE produto_t SET comunidade_origem_id = ? WHERE id = ?",
                UUID.fromString(antigaId), UUID.fromString(comOrigemId));
        assertThat(catalogo(nomeComOrigem, null, null).get(0)).containsEntry("comunidade", antiga);
        assertThat(catalogo(nomeComOrigem, "comunidade", recente)).isEmpty();
    }
}
