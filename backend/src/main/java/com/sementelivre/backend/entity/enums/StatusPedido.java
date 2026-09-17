package com.sementelivre.backend.entity.enums;

import java.util.Set;

/**
 * Ciclo de vida do pedido. Cada constante declara para quais estados pode
 * transicionar, de modo que a regra de transicao fique junto do proprio enum
 * em vez de espalhada pelo service.
 *
 * PENDENTE --> CONFIRMADO (baixa o estoque)
 * PENDENTE --> CANCELADO  (nada a restaurar, o estoque nunca foi baixado)
 * CONFIRMADO --> CANCELADO (restaura o estoque baixado na confirmacao)
 * CANCELADO --> (estado final)
 */
public enum StatusPedido {

    PENDENTE,
    CONFIRMADO,
    CANCELADO;

    public boolean podeTransicionarPara(StatusPedido destino) {
        return proximosEstados().contains(destino);
    }

    public Set<StatusPedido> proximosEstados() {
        return switch (this) {
            case PENDENTE -> Set.of(CONFIRMADO, CANCELADO);
            case CONFIRMADO -> Set.of(CANCELADO);
            case CANCELADO -> Set.of();
        };
    }
}
