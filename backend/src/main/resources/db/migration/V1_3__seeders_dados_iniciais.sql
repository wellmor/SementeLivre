INSERT INTO logradouro_t(id, logradouro, numero, bairro, municipio, uf, cep)
VALUES 
    (gen_random_uuid(), 'Rua Principal', '100', 'Centro', 'Rio Pomba', 'MG', '36180-000'),
    (gen_random_uuid(), 'Sítio dos Coelhos', 'S/N', 'Zona Rural', 'Rio Pomba', 'MG', '36180-000'
);


INSERT INTO pessoa_t(id, tipo_documento, documento, nome, email, senha_hash, logradouro_id)
VALUES(
    gen_random_uuid(), 
    'CPF', 
    '00000000000',
    'Administrador Geral', 
    'admin@sementelivre.com.br',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    (SELECT id FROM logradouro_t WHERE logradouro = 'Rua Principal' LIMIT 1)
);

INSERT INTO admin_t(pessoa_id, nivel_acesso)
VALUES((SELECT id FROM pessoa_t WHERE email = 'admin@sementelivre.com.br'), 'SUPER_ADMIN');

INSERT INTO comunidade_t(id, nome, logradouro_id, status)
VALUES(
    gen_random_uuid(), 
    'Quilombo dos Coelhos', 
    (SELECT id FROM logradouro_t WHERE logradouro = 'Sítio dos Coelhos' LIMIT 1), 
    'ATIVA'
);

INSERT INTO produto_t(nome_popular, nome_cientifico, url_foto, tipo, especie, formato, comunidade_origem_id)
VALUES(
    'Milho Crioulo',
    'Zea mays', 
    'https://placeholder.com/milho.jpg', 
    'CEREAL', 
    'MILHO', 
    'SEMENTE',
    (SELECT id FROM comunidade_t WHERE nome = 'Quilombo dos Coelhos' LIMIT 1)
);