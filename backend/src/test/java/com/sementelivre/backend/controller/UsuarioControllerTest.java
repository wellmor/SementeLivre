package com.sementelivre.backend.controller;

import com.sementelivre.backend.dto.UsuarioCreateRequestDTO;
import com.sementelivre.backend.exception.EmailJaCadastradoException;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.service.PessoaService;
import com.sementelivre.backend.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.sementelivre.backend.exception.GlobalExceptionHandler;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private PessoaService pessoaService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void deveRetornar201CreatedQuandoCriarComSucesso() throws Exception {
        String json = "{" +
                "\"nome\":\"Teste Usuario\"," +
                "\"tipoDocumento\":\"CPF\"," +
                "\"documento\":\"12345678901\"," +
                "\"email\":\"teste@email.com\"," +
                "\"senha\":\"senha123\"," +
                "\"endereco\":{" +
                "   \"logradouro\":\"Rua A\"," +
                "   \"municipio\":\"Cidade\"," +
                "   \"uf\":\"MG\"" +
                "}" +
                "}";

        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setNome("Teste Usuario");
        u.setTipoDocumento(TipoDocumento.CPF);
        u.setDocumento("12345678901");
        u.setEmail("teste@email.com");

        when(usuarioService.criar(any(UsuarioCreateRequestDTO.class))).thenReturn(u);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(u.getId().toString()))
                .andExpect(jsonPath("$.nome").value("Teste Usuario"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    public void deveRetornar409ConflictQuandoEmailDuplicado() throws Exception {
        String json = "{" +
                "\"nome\":\"Teste Usuario\"," +
                "\"tipoDocumento\":\"CPF\"," +
                "\"documento\":\"12345678901\"," +
                "\"email\":\"duplicado@email.com\"," +
                "\"senha\":\"senha123\"," +
                "\"endereco\":{" +
                "   \"logradouro\":\"Rua A\"," +
                "   \"municipio\":\"Cidade\"," +
                "   \"uf\":\"MG\"" +
                "}" +
                "}";

        when(usuarioService.criar(any(UsuarioCreateRequestDTO.class)))
                .thenThrow(new EmailJaCadastradoException("E-mail já cadastrado no sistema."));

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado no sistema."));
    }

    @Test
    public void deveRetornar400BadRequestQuandoValidacaoFalhar() throws Exception {
        // UF invalida, sem senha
        String json = "{" +
                "\"nome\":\"Teste Usuario\"," +
                "\"tipoDocumento\":\"CPF\"," +
                "\"documento\":\"12345678901\"," +
                "\"email\":\"teste@email.com\"," +
                "\"endereco\":{" +
                "   \"logradouro\":\"Rua A\"," +
                "   \"municipio\":\"Cidade\"," +
                "   \"uf\":\"mg\"" +
                "}" +
                "}";

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
