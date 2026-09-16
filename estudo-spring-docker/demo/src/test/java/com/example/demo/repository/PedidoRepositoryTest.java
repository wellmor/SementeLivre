package com.example.demo.repository;

import com.example.demo.model.Itens;
import com.example.demo.model.Pedido;
import com.example.demo.model.Proprietario;
import com.example.demo.model.StatusPedido;
import com.example.demo.model.TipoDocumento;
import com.example.demo.model.TipoPedido;
import com.example.demo.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Usuario criarUsuario(String email, String documento) {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setDocumento(documento);
        usuario.setTipoDocumento(TipoDocumento.CPF);
        usuario.setEmail(email);
        usuario.setSenhaHash("hash123");
        return pessoaRepository.save(usuario);
    }

    private Proprietario criarProprietario(String email, String documento, String rg) {
        Proprietario proprietario = new Proprietario();
        proprietario.setNome("Proprietario Teste");
        proprietario.setDocumento(documento);
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setEmail(email);
        proprietario.setSenhaHash("hash123");
        proprietario.setRg(rg);
        return pessoaRepository.save(proprietario);
    }

    private Itens criarItem(double quantidade, Double precoUnitario) {
        Itens item = new Itens();
        item.setProdutoId(UUID.randomUUID());
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(precoUnitario);
        return item;
    }

    @Test
    public void deveSalvarPedidoComItensEmCascata() {
        // Arrange
        Pedido pedido = new Pedido();
        pedido.setTipoPedido(TipoPedido.VENDA);
        pedido.setMensagemOpcional("Tenho interesse nessas sementes");
        pedido.setUsuarioSolicitante(criarUsuario("solicitante@teste.com", "11122233344"));
        pedido.setProprietarioRecebedor(criarProprietario("recebedor@teste.com", "12345678901", "MG-111111"));
        pedido.adicionarItem(criarItem(2.5, 10.0));
        pedido.adicionarItem(criarItem(1.0, 7.5));

        // Act
        Pedido salvo = pedidoRepository.save(pedido);
        pedidoRepository.flush();

        // Assert
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getStatus()).isEqualTo(StatusPedido.PENDENTE);
        assertThat(salvo.getDataPedido()).isNotNull();
        assertThat(salvo.getItens()).hasSize(2);
        assertThat(salvo.getItens()).allSatisfy(item -> {
            assertThat(item.getId()).isNotNull();
            assertThat(item.getPedido().getId()).isEqualTo(salvo.getId());
        });

        // Os itens foram gravados na tabela junto com o pedido (cascade)
        Integer contagemItens = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM itens_pedido_t WHERE pedido_id = ?", Integer.class, salvo.getId());
        assertThat(contagemItens).isEqualTo(2);

        // Enums gravados como String
        String tipoPedido = jdbcTemplate.queryForObject(
                "SELECT tipo_pedido FROM pedido_t WHERE id = ?", String.class, salvo.getId());
        assertThat(tipoPedido).isEqualTo("VENDA");

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM pedido_t WHERE id = ?", String.class, salvo.getId());
        assertThat(status).isEqualTo("PENDENTE");
    }

    @Test
    public void deveRecuperarPedidoComRelacionamentos() {
        Usuario solicitante = criarUsuario("solicitante2@teste.com", "22233344455");
        Proprietario recebedor = criarProprietario("recebedor2@teste.com", "98765432109", "MG-222222");

        Pedido pedido = new Pedido();
        pedido.setTipoPedido(TipoPedido.DOACAO);
        pedido.setUsuarioSolicitante(solicitante);
        pedido.setProprietarioRecebedor(recebedor);
        pedido.adicionarItem(criarItem(3.0, null));

        UUID pedidoId = pedidoRepository.saveAndFlush(pedido).getId();

        Pedido recuperado = pedidoRepository.findById(pedidoId).orElseThrow();
        assertThat(recuperado.getTipoPedido()).isEqualTo(TipoPedido.DOACAO);
        assertThat(recuperado.getUsuarioSolicitante().getId()).isEqualTo(solicitante.getId());
        assertThat(recuperado.getProprietarioRecebedor().getId()).isEqualTo(recebedor.getId());
        assertThat(recuperado.getItens()).hasSize(1);
        assertThat(recuperado.getItens().get(0).getPrecoUnitario()).isNull();

        assertThat(pedidoRepository.findByUsuarioSolicitanteId(solicitante.getId())).hasSize(1);
        assertThat(pedidoRepository.findByStatus(StatusPedido.PENDENTE)).isNotEmpty();
    }

    @Test
    public void deveRemoverItemOrfaoAoRemoverDoPedido() {
        Pedido pedido = new Pedido();
        pedido.setTipoPedido(TipoPedido.TROCA);
        pedido.setUsuarioSolicitante(criarUsuario("solicitante3@teste.com", "33344455566"));
        pedido.setProprietarioRecebedor(criarProprietario("recebedor3@teste.com", "45678912301", "MG-333333"));
        pedido.adicionarItem(criarItem(1.0, 5.0));
        pedido.adicionarItem(criarItem(4.0, 5.0));

        Pedido salvo = pedidoRepository.saveAndFlush(pedido);

        // Act: remove um item da colecao
        salvo.removerItem(salvo.getItens().get(0));
        pedidoRepository.saveAndFlush(salvo);

        // Assert: orphanRemoval apagou a linha do banco
        Integer contagemItens = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM itens_pedido_t WHERE pedido_id = ?", Integer.class, salvo.getId());
        assertThat(contagemItens).isEqualTo(1);
    }
}
