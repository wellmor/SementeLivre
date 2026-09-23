-- Migration V4: coluna 'ativo' de usuario_t.
--
-- Ate aqui 'ativo' existia apenas na entidade Usuario (flat) e nunca foi criada
-- por migration nenhuma. Com Usuario passando a herdar de Pessoa (TPT), os dados
-- de identidade ficam em pessoa_t e usuario_t guarda so o que e especifico do
-- usuario -- neste caso, a flag de conta ativa usada por UserDetails.isEnabled().
--
-- Aditiva: nao altera coluna existente, nao mexe em PK nem em FK. Linhas
-- existentes assumem TRUE pelo DEFAULT.
ALTER TABLE usuario_t ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
