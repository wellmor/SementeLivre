CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE status_comunidade_enum AS ENUM ('ATIVA', 'PENDENTE_APROVACAO', 'REJEITADA');
CREATE TYPE tipo_produto_enum AS ENUM ('HORTALICA', 'FRUTIFERA', 'FORRAGEIRA', 'CEREAL', 'LEGUMINOSA', 'VERDURA', 'MEDICINAL', 'OUTRAS');
CREATE TYPE especie_geral_enum AS ENUM ('FEIJAO', 'MILHO', 'ABOBORA', 'ALFACE', 'ARROZ', 'CEBOLA', 'ALHO', 'OUTRAS');
CREATE TYPE formato_produto_enum AS ENUM ('MUDA', 'SEMENTE');
CREATE TYPE pesagem_enum AS ENUM ('SACA', 'KG', 'GRAMA', 'MG', 'UNIDADE');
CREATE TYPE disponibilidade_produto_enum AS ENUM ('PARA_TROCA', 'PARA_VENDA', 'PARA_DOACAO', 'A_NEGOCIAR', 'INDISPONIVEL');
CREATE TYPE tipo_movimentacao_enum AS ENUM ('ENTRADA', 'SAIDA_VENDA', 'SAIDA_TROCA', 'SAIDA_DOACAO', 'CORRECAO', 'ZERAMENTO');
CREATE TYPE tipo_pedido_enum AS ENUM ('VENDA', 'TROCA', 'DOACAO');
CREATE TYPE status_pedido_enum AS ENUM ('PENDENTE', 'CONFIRMADO', 'CANCELADO');
CREATE TYPE tipo_relatorio_enum AS ENUM ('ESTOQUE_SEMENTES', 'PEDIDOS_REALIZADOS');


CREATE TABLE logradouro_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(10),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    municipio VARCHAR(100) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    cep VARCHAR(9),
    CONSTRAINT chk_uf CHECK (uf ~ '^[A-Z]{2}$')
);

CREATE TABLE pessoa_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_documento VARCHAR(10) NOT NULL,
    documento VARCHAR(14) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(15),
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    logradouro_id UUID REFERENCES logradouro_t(id) ON DELETE SET NULL,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_ultima_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_pessoa_documento UNIQUE (tipo_documento, documento),
    CONSTRAINT uk_pessoa_email UNIQUE (email),
    CONSTRAINT chk_tipo_documento CHECK (tipo_documento IN ('CPF', 'CNPJ')),
    CONSTRAINT chk_documento_tamanho CHECK (
        (tipo_documento = 'CPF' AND LENGTH(documento) = 11) OR
        (tipo_documento = 'CNPJ' AND LENGTH(documento) = 14)
    )
);

CREATE TABLE usuario_t(
    pessoa_id UUID PRIMARY KEY REFERENCES pessoa_t(id) ON DELETE CASCADE
);

CREATE TABLE proprietario_t(
    pessoa_id UUID PRIMARY KEY REFERENCES pessoa_t(id) ON DELETE CASCADE,
    rg VARCHAR(20) NOT NULL,
    exibir_no_site_publico BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uk_proprietario_rg UNIQUE (rg)
);

CREATE TABLE admin_t(
    pessoa_id UUID PRIMARY KEY REFERENCES pessoa_t(id) ON DELETE CASCADE,
    nivel_acesso VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    CONSTRAINT chk_nivel_acesso CHECK (nivel_acesso IN ('SUPER_ADMIN', 'ADMIN', 'MODERADOR'))
);

CREATE TABLE comunidade_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    logradouro_id UUID REFERENCES logradouro_t(id) ON DELETE SET NULL,
    status status_comunidade_enum NOT NULL DEFAULT 'PENDENTE_APROVACAO',            
    data_solicitacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_aprovacao TIMESTAMP
);

CREATE TABLE propriedade_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL, 
    tamanho_hectares DOUBLE PRECISION NOT NULL CHECK (tamanho_hectares > 0),
    logradouro_id UUID REFERENCES logradouro_t(id) ON DELETE SET NULL,
    proprietario_id UUID NOT NULL REFERENCES proprietario_t(pessoa_id) ON DELETE CASCADE,
    comunidade_id UUID NOT NULL REFERENCES comunidade_t(id) ON DELETE RESTRICT,
    data_cadastro TIMESTAMP NOT NULL DEFAULT NOW(),
    data_ultima_alteracao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE produto_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_popular VARCHAR(150) NOT NULL,
    nome_cientifico VARCHAR(200),
    historico TEXT,
    url_foto VARCHAR(500) NOT NULL,
    tipo tipo_produto_enum NOT NULL,
    especie especie_geral_enum NOT NULL,
    formato formato_produto_enum NOT NULL,
    familia_botanica VARCHAR(100),
    comunidade_origem_id UUID REFERENCES comunidade_t(id) ON DELETE SET NULL,
    data_inclusao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_ultima_alteracao TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE estoque_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proprietario_id UUID NOT NULL REFERENCES proprietario_t(pessoa_id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produto_t(id) ON DELETE CASCADE,
    descricao VARCHAR(255),
    preco DOUBLE PRECISION CHECK (preco IS NULL OR preco >= 0),
    quantidade DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (quantidade >= 0),
    tipo_pesagem pesagem_enum NOT NULL,
    disponibilidade disponibilidade_produto_enum NOT NULL DEFAULT 'INDISPONIVEL',
    tipo_movimentacao tipo_movimentacao_enum NOT NULL DEFAULT 'ENTRADA',
    data_movimentacao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_ultima_atualizacao TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_estoque_proprietario_produto UNIQUE (proprietario_id, produto_id)
);

CREATE TABLE pedido_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_pedido tipo_pedido_enum NOT NULL,
    mensagem_opcional TEXT,
    data_pedido TIMESTAMP NOT NULL DEFAULT NOW(),
    status status_pedido_enum NOT NULL DEFAULT 'PENDENTE',
    usuario_solicitante_id UUID NOT NULL REFERENCES usuario_t(pessoa_id) ON DELETE RESTRICT,
    proprietario_recebedor_id UUID NOT NULL REFERENCES proprietario_t(pessoa_id) ON DELETE RESTRICT
);

CREATE TABLE itens_pedido_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL REFERENCES pedido_t(id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produto_t(id) ON DELETE RESTRICT,
    quantidade DOUBLE PRECISION NOT NULL CHECK (quantidade > 0),
    preco_unitario DOUBLE PRECISION CHECK (preco_unitario IS NULL OR preco_unitario >= 0)
);

CREATE TABLE notificacao_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(255) NOT NULL,
    mensagem TEXT NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT false,
    data_geracao TIMESTAMP NOT NULL DEFAULT NOW(),
    data_leitura TIMESTAMP,
    proprietario_id UUID NOT NULL REFERENCES proprietario_t(pessoa_id) ON DELETE CASCADE,
    pedido_relacionado_id UUID REFERENCES pedido_t(id) ON DELETE SET NULL
);

CREATE TABLE solicitacao_cadastro_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome_responsavel VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    nome_comunidade VARCHAR(150) NOT NULL,
    localizacao VARCHAR(255),
    documento_nome VARCHAR(255),
    documento_base64 TEXT,
    status VARCHAR(15) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'APROVADA', 'REJEITADA')),
    data_solicitacao TIMESTAMP NOT NULL DEFAULT NOW(),
    observacao TEXT
);

CREATE TABLE conta_produtor_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    nome VARCHAR(150) NOT NULL,
    comunidade_id UUID NOT NULL REFERENCES comunidade_t(id) ON DELETE RESTRICT
);

CREATE TABLE relatorio_t(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo tipo_relatorio_enum NOT NULL,
    filtros_utilizados JSONB,
    data_geracao TIMESTAMP NOT NULL DEFAULT NOW(),
    proprietario_id UUID NOT NULL REFERENCES proprietario_t(pessoa_id) ON DELETE CASCADE
);

--triggers
CREATE OR REPLACE FUNCTION fn_atualizar_data_alteracao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_ultima_alteracao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pessoa_atualizar_data 
    BEFORE UPDATE ON pessoa_t 
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_alteracao();

CREATE TRIGGER trg_propriedade_atualizar_data 
    BEFORE UPDATE ON propriedade_t 
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_alteracao();

CREATE TRIGGER trg_produto_atualizar_data 
    BEFORE UPDATE ON produto_t 
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_alteracao();

CREATE TRIGGER trg_estoque_atualizar_data 
    BEFORE UPDATE ON estoque_t 
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_alteracao();