package com.sementelivre.backend.controller;

import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Role;
import com.sementelivre.backend.entity.Usuario;
import com.sementelivre.backend.entity.enums.PerfilEnum;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.exception.ResourceNotFoundException;
import com.sementelivre.backend.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.sementelivre.backend.exception.GlobalExceptionHandler;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Usuario contaDeProprietario(UUID id) {
        Proprietario proprietario = new Proprietario();
        proprietario.setId(id);
        proprietario.setNome("Teste Proprietario");
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setDocumento("12345678901");
        proprietario.setEmail("teste@email.com");

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setPessoa(proprietario);
        usuario.setSenhaHash("hash-bcrypt-simulado");
        usuario.getRoles().add(Role.builder().nome(PerfilEnum.ROLE_PROPRIETARIO).build());
        return usuario;
    }

    @Test
    public void deveRetornarContaComDadosDaPessoaSemExporSenha() throws Exception {
        UUID id = UUID.randomUUID();
        when(usuarioService.buscarPorId(id)).thenReturn(contaDeProprietario(id));

        mockMvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.nome").value("Teste Proprietario"))
                .andExpect(jsonPath("$.email").value("teste@email.com"))
                .andExpect(jsonPath("$.tipoPessoa").value("PROPRIETARIO"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_PROPRIETARIO"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    public void deveRetornar404QuandoContaNaoExiste() throws Exception {
        UUID id = UUID.randomUUID();
        when(usuarioService.buscarPorId(id))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        mockMvc.perform(get("/api/usuarios/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void deveRetornar204AoExcluirConta() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/usuarios/{id}", id))
                .andExpect(status().isNoContent());

        verify(usuarioService).excluir(id);
    }

    @Test
    public void deveRetornar404AoExcluirContaInexistente() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Usuário não encontrado com o ID: " + id))
                .when(usuarioService).excluir(id);

        mockMvc.perform(delete("/api/usuarios/{id}", id))
                .andExpect(status().isNotFound());
    }
}
