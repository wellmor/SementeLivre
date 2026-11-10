package com.sementelivre.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.sementelivre.backend.dto.ComunidadePublicaResponseDTO;
import com.sementelivre.backend.dto.ProdutorPublicoResponseDTO;
import com.sementelivre.backend.dto.SementePublicaResponseDTO;
import com.sementelivre.backend.entity.enums.Disponibilidade;
import com.sementelivre.backend.entity.enums.FormatoProduto;
import com.sementelivre.backend.entity.enums.TipoProduto;
import com.sementelivre.backend.exception.GlobalExceptionHandler;
import com.sementelivre.backend.exception.ResourceNotFoundException;
import com.sementelivre.backend.service.ProdutorPublicoService;

/**
 * Contrato HTTP dos endpoints publicos do produtor (issue #91).
 *
 * NAO usa @WebMvcTest de proposito: o slice carrega o SecurityConfig, que
 * depende de SecurityFilter -> JwtService, beans que nao existem no slice --
 * o contexto falha com "No qualifying bean of type JwtService". O standalone
 * setup e o padrao ja adotado por UsuarioControllerTest e
 * ProprietarioControllerTest neste modulo, roda sem contexto Spring nenhum e,
 * por isso, nao esbarra nas dividas de infra (JavaMailSender, Flyway/H2).
 *
 * Consequencia a registrar: o permitAll de GET /produtores/** configurado no
 * SecurityConfig NAO e exercitado aqui. Isso precisa de verificacao manual ou
 * de um teste de integracao, quando a infra de contexto estiver de pe.
 */
@ExtendWith(MockitoExtension.class)
class ProdutorPublicoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProdutorPublicoService produtorPublicoService;

    @InjectMocks
    private ProdutorPublicoController produtorPublicoController;

    private static final UUID PRODUTOR_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(produtorPublicoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void deveRetornar200ComPerfilPublico() throws Exception {
        when(produtorPublicoService.buscarPerfilPublico(PRODUTOR_ID)).thenReturn(perfil());

        mockMvc.perform(get("/produtores/{id}", PRODUTOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria das Sementes"))
                .andExpect(jsonPath("$.municipio").value("Rio Pomba"));
    }

    @Test
    void deveRetornar404QuandoProdutorInexistenteOuPrivado() throws Exception {
        when(produtorPublicoService.buscarPerfilPublico(any()))
                .thenThrow(new ResourceNotFoundException("Produtor não encontrado"));

        mockMvc.perform(get("/produtores/{id}", PRODUTOR_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void respostaPublicaNaoDeveConterDadoPessoalSensivel() throws Exception {
        when(produtorPublicoService.buscarPerfilPublico(PRODUTOR_ID)).thenReturn(perfil());

        String json = mockMvc.perform(get("/produtores/{id}", PRODUTOR_ID))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(json)
                .doesNotContain("documento")
                .doesNotContain("tipoDocumento")
                .doesNotContain("rg")
                .doesNotContain("telefone")
                .doesNotContain("email")
                .doesNotContain("senhaHash")
                .doesNotContain("senha")
                .doesNotContain("exibirNoSitePublico")
                .doesNotContain("dataCadastro");
    }

    @Test
    void deveRetornar200ComListaDeSementes() throws Exception {
        when(produtorPublicoService.listarSementes(PRODUTOR_ID)).thenReturn(List.of(semente()));

        mockMvc.perform(get("/produtores/{id}/sementes", PRODUTOR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomePopular").value("Feijão Preto"))
                .andExpect(jsonPath("$[0].disponibilidade").value("PARA_TROCA"));
    }

    @Test
    void deveRetornar200ComListaVaziaQuandoProdutorPublicoNaoTemSementes() throws Exception {
        when(produtorPublicoService.listarSementes(PRODUTOR_ID)).thenReturn(List.of());

        mockMvc.perform(get("/produtores/{id}/sementes", PRODUTOR_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void sementesDeveRetornar404QuandoProdutorInexistenteOuPrivado() throws Exception {
        when(produtorPublicoService.listarSementes(any()))
                .thenThrow(new ResourceNotFoundException("Produtor não encontrado"));

        mockMvc.perform(get("/produtores/{id}/sementes", PRODUTOR_ID))
                .andExpect(status().isNotFound());
    }

    private ProdutorPublicoResponseDTO perfil() {
        return new ProdutorPublicoResponseDTO(
                PRODUTOR_ID,
                "Maria das Sementes",
                "Rio Pomba",
                List.of(new ComunidadePublicaResponseDTO(UUID.randomUUID(), "Quilombo São José")),
                List.of(semente()));
    }

    private SementePublicaResponseDTO semente() {
        return new SementePublicaResponseDTO(
                UUID.randomUUID(),
                "Feijão Preto",
                "Phaseolus vulgaris",
                FormatoProduto.SEMENTE,
                TipoProduto.LEGUMINOSA,
                "Fabaceae",
                "https://exemplo/foto.jpg",
                Disponibilidade.PARA_TROCA);
    }
}
