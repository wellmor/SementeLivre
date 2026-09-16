package com.example.demo.controller;

import com.example.demo.dto.ProprietarioCreateRequestDTO;
import com.example.demo.model.Proprietario;
import com.example.demo.model.TipoDocumento;
import com.example.demo.exception.RgJaCadastradoException;
import com.example.demo.service.PessoaService;
import com.example.demo.service.ProprietarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.demo.exception.GlobalExceptionHandler;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProprietarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProprietarioService proprietarioService;

    @Mock
    private PessoaService pessoaService;

    @InjectMocks
    private ProprietarioController proprietarioController;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(proprietarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void deveRetornar201CreatedQuandoCriarComSucesso() throws Exception {
        String json = "{" +
                "\"nome\":\"Teste Proprietario\"," +
                "\"tipoDocumento\":\"CPF\"," +
                "\"documento\":\"12345678901\"," +
                "\"email\":\"prop@email.com\"," +
                "\"senha\":\"senha123\"," +
                "\"rg\":\"MG-123\"," +
                "\"exibirNoSitePublico\":true," +
                "\"endereco\":{" +
                "   \"logradouro\":\"Rua A\"," +
                "   \"municipio\":\"Cidade\"," +
                "   \"uf\":\"MG\"" +
                "}" +
                "}";

        Proprietario p = new Proprietario();
        p.setId(UUID.randomUUID());
        p.setNome("Teste Proprietario");
        p.setTipoDocumento(TipoDocumento.CPF);
        p.setDocumento("12345678901");
        p.setEmail("prop@email.com");
        p.setRg("MG-123");
        p.setExibirNoSitePublico(true);

        when(proprietarioService.criar(any(ProprietarioCreateRequestDTO.class))).thenReturn(p);

        mockMvc.perform(post("/api/proprietarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(p.getId().toString()))
                .andExpect(jsonPath("$.nome").value("Teste Proprietario"))
                .andExpect(jsonPath("$.rg").value("MG-123"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    public void deveRetornar409ConflictQuandoRgDuplicado() throws Exception {
        String json = "{" +
                "\"nome\":\"Teste Proprietario\"," +
                "\"tipoDocumento\":\"CPF\"," +
                "\"documento\":\"12345678901\"," +
                "\"email\":\"prop@email.com\"," +
                "\"senha\":\"senha123\"," +
                "\"rg\":\"MG-123\"," +
                "\"endereco\":{" +
                "   \"logradouro\":\"Rua A\"," +
                "   \"municipio\":\"Cidade\"," +
                "   \"uf\":\"MG\"" +
                "}" +
                "}";

        when(proprietarioService.criar(any(ProprietarioCreateRequestDTO.class)))
                .thenThrow(new RgJaCadastradoException("RG já cadastrado no sistema."));

        mockMvc.perform(post("/api/proprietarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("RG já cadastrado no sistema."));
    }

    @Test
    public void deveRetornar400BadRequestQuandoValidacaoFalhar() throws Exception {
        // Falta RG, que é obrigatorio para proprietario
        String json = "{" +
                "\"nome\":\"Teste Proprietario\"," +
                "\"tipoDocumento\":\"CPF\"," +
                "\"documento\":\"12345678901\"," +
                "\"email\":\"prop@email.com\"," +
                "\"senha\":\"senha123\"," +
                "\"endereco\":{" +
                "   \"logradouro\":\"Rua A\"," +
                "   \"municipio\":\"Cidade\"," +
                "   \"uf\":\"MG\"" +
                "}" +
                "}";

        mockMvc.perform(post("/api/proprietarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.details").isArray());
    }
}
