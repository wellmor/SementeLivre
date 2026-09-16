-- =====================================================
-- SCRIPT DE CRIAÇÃO DO BANCO DE DADOS
-- SEMENTE LIVRE - PostgreSQL 15+ / H2 Compatible
-- =====================================================

-- =====================================================
-- TABELAS
-- =====================================================

-- Tabela de logradouros (compartilhada)
CREATE TABLE logradouro_t (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(10),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    municipio VARCHAR(100) NOT NULL,
    uf VARCHAR(2) NOT NULL,
    cep VARCHAR(9),
    CONSTRAINT chk_uf CHECK (uf ~ '^[A-Z]{2}$')
);

-- Tabela base de pessoas (herança por tabela)
CREATE TABLE pessoa_t (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
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

CREATE INDEX idx_pessoa_logradouro ON pessoa_t(logradouro_id);

-- Tabela de usuários (herda identidade de pessoa)
CREATE TABLE usuario_t (
    pessoa_id UUID PRIMARY KEY REFERENCES pessoa_t(id) ON DELETE CASCADE
);

-- Tabela de proprietários (herda identidade de pessoa)
CREATE TABLE proprietario_t (
    pessoa_id UUID PRIMARY KEY REFERENCES pessoa_t(id) ON DELETE CASCADE,
    rg VARCHAR(20) NOT NULL,
    exibir_no_site_publico BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uk_proprietario_rg UNIQUE (rg)
);

CREATE INDEX idx_proprietario_rg ON proprietario_t(rg);

-- Tabela de administradores (herda identidade de pessoa)
CREATE TABLE admin_t (
    pessoa_id UUID PRIMARY KEY REFERENCES pessoa_t(id) ON DELETE CASCADE,
    nivel_acesso VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    CONSTRAINT chk_nivel_acesso CHECK (nivel_acesso IN ('SUPER_ADMIN', 'ADMIN', 'MODERADOR'))
);
