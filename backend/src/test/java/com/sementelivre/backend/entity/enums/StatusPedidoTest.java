package com.sementelivre.backend.entity.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * A maquina de estados mora no enum, entao ela e testada direto aqui, sem
 * precisar de mock nenhum. O PedidoServiceTest cobre o efeito colateral de
 * cada transicao (baixa e restauracao de estoque).
 */
class StatusPedidoTest {

    @Test
    void pendenteDeveTransicionarParaConfirmadoOuCancelado() {
        assertTrue(StatusPedido.PENDENTE.podeTransicionarPara(StatusPedido.CONFIRMADO));
        assertTrue(StatusPedido.PENDENTE.podeTransicionarPara(StatusPedido.CANCELADO));
    }

    @Test
    void confirmadoDeveTransicionarApenasParaCancelado() {
        assertTrue(StatusPedido.CONFIRMADO.podeTransicionarPara(StatusPedido.CANCELADO));
        assertFalse(StatusPedido.CONFIRMADO.podeTransicionarPara(StatusPedido.PENDENTE));
    }

    @Test
    void canceladoDeveSerEstadoFinal() {
        assertEquals(Set.of(), StatusPedido.CANCELADO.proximosEstados());

        for (StatusPedido destino : StatusPedido.values()) {
            assertFalse(
                    StatusPedido.CANCELADO.podeTransicionarPara(destino),
                    "CANCELADO não deveria transicionar para " + destino);
        }
    }

    @Test
    void nenhumStatusDeveTransicionarParaSiMesmo() {
        for (StatusPedido status : StatusPedido.values()) {
            assertFalse(
                    status.podeTransicionarPara(status),
                    status + " não deveria transicionar para si mesmo");
        }
    }

    @Test
    void confirmadoNaoDeveVoltarASerPendente() {
        // Voltar para PENDENTE deixaria o estoque baixado sem pedido que o
        // justifique, entao a transicao nao pode existir.
        assertFalse(StatusPedido.CONFIRMADO.podeTransicionarPara(StatusPedido.PENDENTE));
        assertFalse(StatusPedido.CANCELADO.podeTransicionarPara(StatusPedido.PENDENTE));
    }
}
