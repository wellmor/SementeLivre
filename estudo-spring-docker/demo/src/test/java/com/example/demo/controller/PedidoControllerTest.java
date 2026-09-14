package com.example.demo.controller;

import com.example.demo.dto.ItemPedidoResponseDTO;
import com.example.demo.dto.PedidoCreateDTO;
import com.example.demo.dto.PedidoResponseDTO;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.PedidoNaoEncontradoException;
import com.example.demo.model.StatusPedido;
import com.example.demo.model.TipoPedido;
import com.example.demo.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PedidoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(pedidoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private PedidoResponseDTO respostaComUmItem(UUID pedidoId) {
        ItemPedidoResponseDTO item = new ItemPedidoResponseDTO();
        item.setId(UUID.randomUUID());
        item.setProdutoId(UUID.randomUUID());
        item.setQuantidade(2.5);
        item.setPrecoUnitario(10.0);

        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedidoId);
        dto.setTipoPedido(TipoPedido.VENDA);
        dto.setStatus(StatusPedido.PENDENTE);
        dto.setDataPedido(LocalDateTime.now());
        dto.setUsuarioSolicitanteId(UUID.randomUUID());
        dto.setProprietarioRecebedorId(UUID.randomUUID());
        dto.setItens(List.of(item));
        return dto;
    }

    @Test
    public void deveCriarPedidoComItensERetornar201() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        String json = "{"
                + "\"tipoPedido\":\"VENDA\","
                + "\"usuarioSolicitanteId\":\"" + UUID.randomUUID() + "\","
                + "\"proprietarioRecebedorId\":\"" + UUID.randomUUID() + "\","
                + "\"itens\":[{\"produtoId\":\"" + UUID.randomUUID() + "\",\"quantidade\":2.5,\"precoUnitario\":10.0}]"
                + "}";

        when(pedidoService.criar(any(PedidoCreateDTO.class))).thenReturn(respostaComUmItem(pedidoId));

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(pedidoId.toString()))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2.5));
    }

    @Test
    public void deveRetornar400QuandoPedidoVemSemItens() throws Exception {
        String json = "{"
                + "\"tipoPedido\":\"VENDA\","
                + "\"usuarioSolicitanteId\":\"" + UUID.randomUUID() + "\","
                + "\"proprietarioRecebedorId\":\"" + UUID.randomUUID() + "\","
                + "\"itens\":[]"
                + "}";

        mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.itens").exists());
    }

    @Test
    public void deveRetornarPedidoComItensPorId() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        when(pedidoService.buscarPorId(eq(pedidoId))).thenReturn(respostaComUmItem(pedidoId));

        mockMvc.perform(get("/pedidos/" + pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId.toString()))
                .andExpect(jsonPath("$.itens.length()").value(1));
    }

    @Test
    public void deveRetornar404NoFormatoPadraoQuandoPedidoNaoExiste() throws Exception {
        UUID pedidoId = UUID.randomUUID();
        when(pedidoService.buscarPorId(eq(pedidoId)))
                .thenThrow(new PedidoNaoEncontradoException("Pedido não encontrado com o ID: " + pedidoId));

        mockMvc.perform(get("/pedidos/" + pedidoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void deveRetornar204AoExcluirPedido() throws Exception {
        mockMvc.perform(delete("/pedidos/" + UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }
}
