-- Produtor de semente crioula pode ser isolado, sem vínculo com comunidade
-- nenhuma: a comunidade da propriedade passa a ser opcional. A chave
-- estrangeira continua (quando informada, a comunidade tem que existir).
ALTER TABLE propriedade_t ALTER COLUMN comunidade_id DROP NOT NULL;
