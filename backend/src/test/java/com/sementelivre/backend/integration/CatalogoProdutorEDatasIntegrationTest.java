package com.sementelivre.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;

/**
 * Campos do catálogo público usados pelos filtros do site (RF-04 / CDU-01).
 *
 * LGPD (RNF-06): o nome do produtor só sai quando ele autorizou aparecer no
 * site (exibirNoSitePublico); sem isso a oferta continua no catálogo, mas sem
 * nome. Nenhum outro dado pessoal (e-mail, telefone, documento) aparece.
 */
class CatalogoProdutorEDatasIntegrationTest extends AbstractAuthIntegrationTest {

    private Conta cadastrar(boolean exibirNoSitePublico) throws Exception {
        String sufixo = sufixoUnico();
        String email = "catalogo-" + sufixo + "@teste.com";
        String documento = cpfValido();
        String rg = "MG-" + sufixo;
        String corpo = cadastroJson(email, documento, rg)
                .replace("\"telefone\"", "\"exibirNoSitePublico\": " + exibirNoSitePublico + ",\n  \"telefone\"");

        String resposta = mockMvc.perform(post("/auth/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new Conta(JsonPath.read(resposta, "$.id"), email, documento, rg);
    }

    private String criar(String url, String token, String corpo) throws Exception {
        String resposta = mockMvc.perform(post(url)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private void ofertar(Conta dono, String nome) throws Exception {
        String token = login(dono.email(), SENHA);
        String produtoId = criar("/produtos", token, """
                {"nomePopular":"%s","nomeCientifico":"Teste","urlFoto":"/uploads/produtos/t.png","tipo":"LEGUMINOSA",
                 "especie":"FEIJAO","formato":"SEMENTE"}
                """.formatted(nome));
        criar("/estoques", token, """
                {"proprietarioId":"%s","produtoId":"%s","descricao":"Oferta","preco":10,"quantidade":5,
                 "tipoPesagem":"KG","disponibilidade":"PARA_VENDA","tipoMovimentacao":"ENTRADA"}
                """.formatted(dono.id(), produtoId));
    }

    private Map<String, Object> unicaOferta(String nome) throws Exception {
        String resposta = mockMvc.perform(get("/catalogo/produtos").param("nomePopular", nome))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Map<String, Object>> conteudo = JsonPath.read(resposta, "$.content");
        assertThat(conteudo).hasSize(1);
        return conteudo.get(0);
    }

    @Test
    void produtorQueAutorizouApareceComONomeEAOfertaTrazAsDatas() throws Exception {
        String nome = "Feijão Público " + sufixoUnico();
        ofertar(cadastrar(true), nome);

        Map<String, Object> oferta = unicaOferta(nome);

        assertThat(oferta.get("nomeProdutor")).isEqualTo("Proprietario Perfil");
        assertThat((String) oferta.get("dataInclusao")).matches("\\d{4}-\\d{2}-\\d{2}T.*");
        assertThat((String) oferta.get("dataUltimaAlteracao")).matches("\\d{4}-\\d{2}-\\d{2}T.*");
        assertThat(oferta).doesNotContainKeys(
                "email", "telefone", "documento", "rg", "proprietarioId", "produtorId", "exibirNoSitePublico");
    }

    @Test
    void produtorQueNaoAutorizouNaoTemONomeExposto() throws Exception {
        String nome = "Feijão Privado " + sufixoUnico();
        ofertar(cadastrar(false), nome);

        Map<String, Object> oferta = unicaOferta(nome);

        assertThat(oferta).containsKey("nomeProdutor");
        assertThat(oferta.get("nomeProdutor")).isNull();
        assertThat(oferta.get("dataInclusao")).isNotNull();
    }
}
