-- Migration V2: Tabelas de Autenticação e Autorização

-- 1. Tabela de Roles
CREATE TABLE role_t (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- Inserir os perfis padrão do sistema
INSERT INTO role_t (nome) VALUES ('ROLE_ADMIN'), ('ROLE_USUARIO'), ('ROLE_PROPRIETARIO');

-- 2. Tabela de Usuários
CREATE TABLE usuario_t (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabela de Junção Usuário <-> Role
CREATE TABLE usuario_role_t (
    usuario_id UUID NOT NULL REFERENCES usuario_t(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES role_t(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, role_id)
);

-- 4. Tabela de Refresh Tokens
CREATE TABLE refresh_token_t (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    usuario_id UUID NOT NULL REFERENCES usuario_t(id) ON DELETE CASCADE,
    data_expiracao TIMESTAMP WITH TIME ZONE NOT NULL,
    revogado BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5. Tabela de Tokens de Recuperação de Senha
CREATE TABLE token_recuperacao_senha_t (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    usuario_id UUID NOT NULL REFERENCES usuario_t(id) ON DELETE CASCADE,
    data_expiracao TIMESTAMP WITH TIME ZONE NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);
