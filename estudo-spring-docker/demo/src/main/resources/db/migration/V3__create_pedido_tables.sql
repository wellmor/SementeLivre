-- =====================================================
-- SEMENTE LIVRE - Dominio de Pedidos (Dev 6 - issue #35)
-- PostgreSQL 15+ / H2 Compatible
-- =====================================================

-- Pedidos de sementes
CREATE TABLE pedido_t (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    tipo_pedido VARCHAR(10) NOT NULL,
    mensagem_opcional TEXT,
    data_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(12) NOT NULL DEFAULT 'PENDENTE',
    usuario_solicitante_id UUID NOT NULL REFERENCES usuario_t(pessoa_id),
    proprietario_recebedor_id UUID NOT NULL REFERENCES proprietario_t(pessoa_id),
    CONSTRAINT chk_pedido_tipo CHECK (tipo_pedido IN ('VENDA', 'TROCA', 'DOACAO')),
    CONSTRAINT chk_pedido_status CHECK (status IN ('PENDENTE', 'CONFIRMADO', 'CANCELADO'))
);

CREATE INDEX idx_pedido_solicitante ON pedido_t(usuario_solicitante_id);
CREATE INDEX idx_pedido_recebedor ON pedido_t(proprietario_recebedor_id);
CREATE INDEX idx_pedido_status ON pedido_t(status);
CREATE INDEX idx_pedido_data ON pedido_t(data_pedido);

-- Itens dentro de cada pedido (composicao: ON DELETE CASCADE)
CREATE TABLE itens_pedido_t (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    pedido_id UUID NOT NULL REFERENCES pedido_t(id) ON DELETE CASCADE,
    -- TODO(#33 - Dev 5): adicionar REFERENCES produto_t(id) quando produto_t existir
    produto_id UUID NOT NULL,
    quantidade DOUBLE PRECISION NOT NULL,
    preco_unitario DOUBLE PRECISION,
    CONSTRAINT chk_itens_quantidade CHECK (quantidade > 0)
);

CREATE INDEX idx_itens_pedido_pedido ON itens_pedido_t(pedido_id);
CREATE INDEX idx_itens_pedido_produto ON itens_pedido_t(produto_id);
