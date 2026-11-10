-- Migration V7: usuario_t vira a conta de login de Admin e Proprietario.
--
-- Regra nova do modelo (MER): Pessoa -> Admin / Proprietario continua sendo
-- heranca (TPT), mas Usuario deixa de ser um tipo de Pessoa. Ele passa a ser a
-- conta de login (1:1) que um Admin ou um Proprietario tem. A estrutura de
-- usuario_t (pessoa_id PK/FK -> pessoa_t) nao muda; o que muda e:
--   * a senha sai de pessoa_t e vai para usuario_t;
--   * todo admin/proprietario existente ganha a sua conta, com o perfil certo;
--   * contas "soltas" (usuario_t sem admin/proprietario) deixam de existir;
--   * ROLE_USUARIO deixa de existir.

-- 1. Contas soltas: nao correspondem a nenhum tipo de pessoa do modelo novo.
--    Se alguma tiver pedidos, a migration para, para nao apagar pedidos sem
--    decisao de alguem.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pedido_t pe
        JOIN usuario_t u ON u.pessoa_id = pe.usuario_solicitante_id
        WHERE NOT EXISTS (SELECT 1 FROM admin_t a WHERE a.pessoa_id = u.pessoa_id)
          AND NOT EXISTS (SELECT 1 FROM proprietario_t p WHERE p.pessoa_id = u.pessoa_id)
    ) THEN
        RAISE EXCEPTION 'V7: existem pedidos feitos por usuarios que nao sao admin nem proprietario. Remova esses pedidos (ou os usuarios) antes de migrar.';
    END IF;
END $$;

CREATE TEMPORARY TABLE v7_contas_soltas AS
SELECT u.pessoa_id
FROM usuario_t u
WHERE NOT EXISTS (SELECT 1 FROM admin_t a WHERE a.pessoa_id = u.pessoa_id)
  AND NOT EXISTS (SELECT 1 FROM proprietario_t p WHERE p.pessoa_id = u.pessoa_id);

DELETE FROM refresh_token_t WHERE usuario_id IN (SELECT pessoa_id FROM v7_contas_soltas);
DELETE FROM token_recuperacao_senha_t WHERE usuario_id IN (SELECT pessoa_id FROM v7_contas_soltas);
DELETE FROM usuario_role_t WHERE usuario_id IN (SELECT pessoa_id FROM v7_contas_soltas);
DELETE FROM usuario_t WHERE pessoa_id IN (SELECT pessoa_id FROM v7_contas_soltas);
DELETE FROM pessoa_t WHERE id IN (SELECT pessoa_id FROM v7_contas_soltas);

DROP TABLE v7_contas_soltas;

-- 2. Conta para cada admin/proprietario que ainda nao tem.
INSERT INTO usuario_t (pessoa_id, ativo)
SELECT p.id, TRUE
FROM pessoa_t p
WHERE (EXISTS (SELECT 1 FROM admin_t a WHERE a.pessoa_id = p.id)
       OR EXISTS (SELECT 1 FROM proprietario_t pr WHERE pr.pessoa_id = p.id))
  AND NOT EXISTS (SELECT 1 FROM usuario_t u WHERE u.pessoa_id = p.id);

-- 3. Senha passa de pessoa_t para usuario_t.
ALTER TABLE usuario_t ADD COLUMN senha_hash VARCHAR(255);

UPDATE usuario_t u
SET senha_hash = p.senha_hash
FROM pessoa_t p
WHERE p.id = u.pessoa_id;

ALTER TABLE usuario_t ALTER COLUMN senha_hash SET NOT NULL;
ALTER TABLE pessoa_t DROP COLUMN senha_hash;

-- 4. Perfis: ROLE_ADMIN para admin, ROLE_PROPRIETARIO para proprietario.
INSERT INTO usuario_role_t (usuario_id, role_id)
SELECT a.pessoa_id, r.id
FROM admin_t a
JOIN role_t r ON r.nome = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO usuario_role_t (usuario_id, role_id)
SELECT p.pessoa_id, r.id
FROM proprietario_t p
JOIN role_t r ON r.nome = 'ROLE_PROPRIETARIO'
ON CONFLICT DO NOTHING;

DELETE FROM usuario_role_t
WHERE role_id IN (SELECT id FROM role_t WHERE nome = 'ROLE_USUARIO');

DELETE FROM role_t WHERE nome = 'ROLE_USUARIO';
