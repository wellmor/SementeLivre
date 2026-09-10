package com.example.demo.controller;

import com.example.demo.dto.PessoaCreateDTO;
import com.example.demo.exception.EmailJaCadastradoException;
import com.example.demo.model.TipoDocumento;
import com.example.demo.model.Pessoa;
import com.example.demo.service.PessoaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.example.demo.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PessoaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PessoaService pessoaService;

    @InjectMocks
    private PessoaController pessoaController;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(pessoaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void deveRetornar409ConflictQuandoEmailDuplicado() throws Exception {
        PessoaCreateDTO dto = new PessoaCreateDTO();
        dto.setTipoDocumento(TipoDocumento.CPF);
        dto.setDocumento("12345678901");
        String json = "{\"tipoDocumento\":\"CPF\",\"documento\":\"12345678901\",\"nome\":\"Teste\",\"email\":\"teste@email.com\",\"senha\":\"senha123\",\"rg\":\"MG-1234\",\"tipoPessoa\":\"PROPRIETARIO\"}";

        when(pessoaService.criar(any(PessoaCreateDTO.class)))
                .thenThrow(new EmailJaCadastradoException("Email já cadastrado no sistema."));

        mockMvc.perform(post("/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email já cadastrado no sistema."));
    }

    @Test
    public void deveRetornar400BadRequestQuandoEmailInvalido() throws Exception {
        String json = "{\"tipoDocumento\":\"CPF\",\"documento\":\"12345678901\",\"nome\":\"Teste\",\"email\":\"emailinvalido\",\"senha\":\"senha123\",\"rg\":\"MG-1234\",\"tipoPessoa\":\"PROPRIETARIO\"}";

        mockMvc.perform(post("/pessoas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors.email").value("O email deve ser válido"));
    }

    @Test
    public void deveRetornar200OkQuandoBuscarPorId() throws Exception {
        UUID id = UUID.randomUUID();
        Pessoa p = new com.example.demo.model.Usuario();
        p.setId(id);
        p.setNome("Buscado");
        p.setEmail("buscado@teste.com");
        
        when(pessoaService.buscarPorId(id)).thenReturn(p);

        mockMvc.perform(get("/pessoas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Buscado"))
                .andExpect(jsonPath("$.tipoPessoa").value("USUARIO"));
    }
}
