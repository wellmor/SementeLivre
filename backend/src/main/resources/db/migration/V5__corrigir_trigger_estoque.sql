CREATE OR REPLACE FUNCTION fn_atualizar_data_estoque()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_ultima_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_estoque_atualizar_data ON estoque_t;

CREATE TRIGGER trg_estoque_atualizar_data
    BEFORE UPDATE ON estoque_t
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_estoque();
