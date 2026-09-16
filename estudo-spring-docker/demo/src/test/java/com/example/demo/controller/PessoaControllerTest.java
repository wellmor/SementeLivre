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
    public void deveRetornar200OkQuandoBuscarPorId() throws Exception {
        UUID id = UUID.randomUUID();
        Pessoa p = new com.example.demo.model.Usuario();
        p.setId(id);
        p.setNome("Buscado");
        p.setEmail("buscado@teste.com");
        
        when(pessoaService.buscarPorId(id)).thenReturn(p);

        mockMvc.perform(get("/api/pessoas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Buscado"))
                .andExpect(jsonPath("$.tipoPessoa").value("USUARIO"));
    }
}
