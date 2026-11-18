-- O admin principal do seed (V1_3) nasceu com um hash de senha que está no
-- repositório público: qualquer um pode tentar quebrá-lo offline. Esta
-- migration troca esse hash por um marcador que nunca confere com senha
-- nenhuma. O backend define a senha de verdade ao subir, a partir de
-- ADMIN_INICIAL_SENHA (ver AdminInicial e o RUNBOOK).
--
-- Só mexe na conta se o hash ainda for o do seed: se alguém já trocou a
-- senha, nada muda.

CREATE TEMP TABLE v14_admin_do_seed AS
SELECT u.pessoa_id
FROM usuario_t u
JOIN pessoa_t p ON p.id = u.pessoa_id
WHERE p.email = 'admin@sementelivre.com.br'
  AND u.senha_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- Sessões abertas com a senha antiga deixam de valer
DELETE FROM refresh_token_t WHERE usuario_id IN (SELECT pessoa_id FROM v14_admin_do_seed);

UPDATE usuario_t
SET senha_hash = 'SENHA_NAO_DEFINIDA'
WHERE pessoa_id IN (SELECT pessoa_id FROM v14_admin_do_seed);

DROP TABLE v14_admin_do_seed;
