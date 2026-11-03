package com.sementelivre.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

/**
 * Estoque zerado sai do site e volta sozinho com o saldo (#127). Antes, um
 * pedido confirmado que esgotava o estoque deixava a oferta "Para Venda" com
 * 0 kg no catálogo público. A disponibilidade escolhida pelo produtor não é
 * trocada, para a oferta voltar igual quando o estoque volta.
 */
class CatalogoEstoqueZeradoIntegrationTest extends AbstractAuthIntegrationTest {

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String token, String corpo) {
        return req.header(HttpHeaders.AUTHORIZATION, token).contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private String criar(String url, String token, String corpo) throws Exception {
        String resposta = mockMvc.perform(json(post(url), token, corpo))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private List<Object> catalogo(String nome) throws Exception {
        String resposta = mockMvc.perform(get("/catalogo/produtos").param("nomePopular", nome))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.content");
    }

    @Test
    void ofertaZeradaSaiDoSiteEVoltaQuandoOEstoqueVolta() throws Exception {
        Conta dono = cadastrar();
        String token = login(dono.email(), SENHA);
        String nome = "Feijão Zerado " + sufixoUnico();
        String produtoId = criar("/produtos", token, """
                {"nomePopular":"%s","nomeCientifico":"Teste","urlFoto":"/uploads/produtos/t.png","tipo":"LEGUMINOSA",
                 "especie":"FEIJAO","formato":"SEMENTE"}
                """.formatted(nome));
        String estoqueId = criar("/estoques", token, """
                {"proprietarioId":"%s","produtoId":"%s","descricao":"Oferta","preco":10,"quantidade":5,
                 "tipoPesagem":"KG","disponibilidade":"PARA_VENDA","tipoMovimentacao":"ENTRADA"}
                """.formatted(dono.id(), produtoId));
        assertThat(catalogo(nome)).hasSize(1);

        // Pedido confirmado que esgota o estoque
        String pedido = "/pedidos/" + criar("/pedidos", token, """
                {"tipoPedido":"VENDA","proprietarioRecebedorId":"%s",
                 "itens":[{"produtoId":"%s","quantidade":5,"precoUnitario":10}],"comprador":{"nome":"Esgota"}}
                """.formatted(dono.id(), produtoId));
        mockMvc.perform(patch(pedido + "/confirmar").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());

        assertThat(catalogo(nome)).isEmpty();
        mockMvc.perform(get("/catalogo/produtos/" + produtoId)).andExpect(status().isNotFound());
        // A disponibilidade escolhida continua gravada
        mockMvc.perform(get("/estoques/" + estoqueId).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$.quantidade").value(0.0))
                .andExpect(jsonPath("$.disponibilidade").value("PARA_VENDA"));

        // Cancelar o pedido devolve o estoque: a oferta volta sem o produtor mexer
        mockMvc.perform(patch(pedido + "/cancelar").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
        assertThat(catalogo(nome)).hasSize(1);
        mockMvc.perform(get("/catalogo/produtos/" + produtoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ofertas[0].quantidade").value(5.0));

        // Pedido em pedido_t: outros testes contam a tabela inteira
        mockMvc.perform(delete("/api/pessoas/" + dono.id() + "/conta").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());
    }
}
