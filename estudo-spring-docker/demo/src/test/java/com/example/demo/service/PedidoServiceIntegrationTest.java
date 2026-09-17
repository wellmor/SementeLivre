package com.example.demo.service;

import com.example.demo.dto.ItemPedidoCreateDTO;
import com.example.demo.dto.PedidoCreateDTO;
import com.example.demo.dto.PedidoResponseDTO;
import com.example.demo.dto.PedidoUpdateDTO;
import com.example.demo.exception.PedidoInvalidoException;
import com.example.demo.exception.PedidoNaoEncontradoException;
import com.example.demo.model.Proprietario;
import com.example.demo.model.StatusPedido;
import com.example.demo.model.TipoDocumento;
import com.example.demo.model.TipoPedido;
import com.example.demo.model.Usuario;
import com.example.demo.repository.PessoaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PedidoServiceIntegrationTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    private UUID usuarioId;
    private UUID proprietarioId;

    @BeforeEach
    public void setup() {
        Usuario usuario = new Usuario();
        usuario.setNome("Solicitante Teste");
        usuario.setDocumento("11122233344");
        usuario.setTipoDocumento(TipoDocumento.CPF);
        usuario.setEmail("solicitante.crud@teste.com");
        usuario.setSenhaHash("hash123");
        usuarioId = pessoaRepository.save(usuario).getId();

        Proprietario proprietario = new Proprietario();
        proprietario.setNome("Recebedor Teste");
        proprietario.setDocumento("12345678901");
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setEmail("recebedor.crud@teste.com");
        proprietario.setSenhaHash("hash123");
        proprietario.setRg("MG-999999");
        proprietarioId = pessoaRepository.save(proprietario).getId();
    }

    private ItemPedidoCreateDTO item(double quantidade, Double preco) {
        ItemPedidoCreateDTO dto = new ItemPedidoCreateDTO();
        dto.setProdutoId(UUID.randomUUID());
        dto.setQuantidade(quantidade);
        dto.setPrecoUnitario(preco);
        return dto;
    }

    private PedidoCreateDTO pedidoComDoisItens() {
        PedidoCreateDTO dto = new PedidoCreateDTO();
        dto.setTipoPedido(TipoPedido.VENDA);
        dto.setMensagemOpcional("Tenho interesse");
        dto.setUsuarioSolicitanteId(usuarioId);
        dto.setProprietarioRecebedorId(proprietarioId);
        dto.setItens(List.of(item(2.5, 10.0), item(1.0, 7.5)));
        return dto;
    }

    @Test
    public void deveCriarPedidoComItensEmCascata() {
        PedidoResponseDTO criado = pedidoService.criar(pedidoComDoisItens());

        assertThat(criado.getId()).isNotNull();
        assertThat(criado.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(criado.getDataPedido()).isNotNull();
        assertThat(criado.getUsuarioSolicitanteId()).isEqualTo(usuarioId);
        assertThat(criado.getProprietarioRecebedorId()).isEqualTo(proprietarioId);
        assertThat(criado.getItens()).hasSize(2);
        assertThat(criado.getItens()).allSatisfy(i -> assertThat(i.getId()).isNotNull());

        entityManager.flush();
        Integer itensNoBanco = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM itens_pedido_t WHERE pedido_id = ?", Integer.class, criado.getId());
        assertThat(itensNoBanco).isEqualTo(2);
    }

    @Test
    public void deveListarEBuscarPedidoComItens() {
        UUID pedidoId = pedidoService.criar(pedidoComDoisItens()).getId();

        assertThat(pedidoService.listar()).isNotEmpty();

        PedidoResponseDTO encontrado = pedidoService.buscarPorId(pedidoId);
        assertThat(encontrado.getItens()).hasSize(2);
        assertThat(encontrado.getMensagemOpcional()).isEqualTo("Tenho interesse");
    }

    @Test
    public void deveAtualizarPedidoSubstituindoOsItens() {
        UUID pedidoId = pedidoService.criar(pedidoComDoisItens()).getId();

        // Simula a fronteira de request: o update chega numa sessao limpa,
        // como aconteceria num PUT depois do POST
        entityManager.flush();
        entityManager.clear();

        PedidoUpdateDTO update = new PedidoUpdateDTO();
        update.setTipoPedido(TipoPedido.TROCA);
        update.setMensagemOpcional("Mudei de ideia");
        update.setStatus(StatusPedido.CONFIRMADO);
        update.setItens(List.of(item(4.0, 3.0)));

        PedidoResponseDTO atualizado = pedidoService.atualizar(pedidoId, update);

        assertThat(atualizado.getTipoPedido()).isEqualTo(TipoPedido.TROCA);
        assertThat(atualizado.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
        assertThat(atualizado.getItens()).hasSize(1);

        // orphanRemoval apagou os dois itens antigos
        entityManager.flush();
        Integer itensNoBanco = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM itens_pedido_t WHERE pedido_id = ?", Integer.class, pedidoId);
        assertThat(itensNoBanco).isEqualTo(1);
    }

    @Test
    public void deveExcluirPedidoESeusItens() {
        UUID pedidoId = pedidoService.criar(pedidoComDoisItens()).getId();
        entityManager.flush();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM itens_pedido_t WHERE pedido_id = ?", Integer.class, pedidoId)).isEqualTo(2);

        pedidoService.deletar(pedidoId);

        entityManager.flush();
        Integer itensNoBanco = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM itens_pedido_t WHERE pedido_id = ?", Integer.class, pedidoId);
        assertThat(itensNoBanco).isZero();

        assertThatThrownBy(() -> pedidoService.buscarPorId(pedidoId))
                .isInstanceOf(PedidoNaoEncontradoException.class);
    }

    @Test
    public void deveRejeitarPedidoQuandoOSolicitanteNaoEUsuario() {
        PedidoCreateDTO dto = pedidoComDoisItens();
        dto.setUsuarioSolicitanteId(proprietarioId); // proprietario nao pode ser o solicitante

        assertThatThrownBy(() -> pedidoService.criar(dto))
                .isInstanceOf(PedidoInvalidoException.class)
                .hasMessageContaining("não é um usuário solicitante válido");
    }
}
