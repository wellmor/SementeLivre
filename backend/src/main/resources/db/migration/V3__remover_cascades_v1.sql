
ALTER TABLE usuario_t DROP CONSTRAINT usuario_t_pessoa_id_fkey;
ALTER TABLE usuario_t ADD CONSTRAINT usuario_t_pessoa_id_fkey
    FOREIGN KEY (pessoa_id) REFERENCES pessoa_t(id) ON DELETE RESTRICT;

ALTER TABLE proprietario_t DROP CONSTRAINT proprietario_t_pessoa_id_fkey;
ALTER TABLE proprietario_t ADD CONSTRAINT proprietario_t_pessoa_id_fkey 
    FOREIGN KEY (pessoa_id) REFERENCES pessoa_t(id) ON DELETE RESTRICT;

ALTER TABLE admin_t DROP CONSTRAINT admin_t_pessoa_id_fkey;
ALTER TABLE admin_t ADD CONSTRAINT admin_t_pessoa_id_fkey 
    FOREIGN KEY (pessoa_id) REFERENCES pessoa_t(id) ON DELETE RESTRICT;

ALTER TABLE propriedade_t DROP CONSTRAINT propriedade_t_proprietario_id_fkey;
ALTER TABLE propriedade_t ADD CONSTRAINT propriedade_t_proprietario_id_fkey 
    FOREIGN KEY (proprietario_id) REFERENCES proprietario_t(pessoa_id) ON DELETE RESTRICT;

ALTER TABLE estoque_t DROP CONSTRAINT estoque_t_proprietario_id_fkey;
ALTER TABLE estoque_t ADD CONSTRAINT estoque_t_proprietario_id_fkey 
    FOREIGN KEY (proprietario_id) REFERENCES proprietario_t(pessoa_id) ON DELETE RESTRICT;

ALTER TABLE estoque_t DROP CONSTRAINT estoque_t_produto_id_fkey;
ALTER TABLE estoque_t ADD CONSTRAINT estoque_t_produto_id_fkey 
    FOREIGN KEY (produto_id) REFERENCES produto_t(id) ON DELETE RESTRICT;

ALTER TABLE itens_pedido_t DROP CONSTRAINT itens_pedido_t_pedido_id_fkey;
ALTER TABLE itens_pedido_t ADD CONSTRAINT itens_pedido_t_pedido_id_fkey 
    FOREIGN KEY (pedido_id) REFERENCES pedido_t(id) ON DELETE RESTRICT;

ALTER TABLE notificacao_t DROP CONSTRAINT notificacao_t_proprietario_id_fkey;
ALTER TABLE notificacao_t ADD CONSTRAINT notificacao_t_proprietario_id_fkey 
    FOREIGN KEY (proprietario_id) REFERENCES proprietario_t(pessoa_id) ON DELETE RESTRICT;

ALTER TABLE relatorio_t DROP CONSTRAINT relatorio_t_proprietario_id_fkey;
ALTER TABLE relatorio_t ADD CONSTRAINT relatorio_t_proprietario_id_fkey
    FOREIGN KEY (proprietario_id) REFERENCES proprietario_t(pessoa_id) ON DELETE RESTRICT;