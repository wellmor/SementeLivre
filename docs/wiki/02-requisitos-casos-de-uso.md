# 02. Requisitos e Casos de Uso

> Documento completo em [`requisitos/DocumentoGeral-Req-CDU.md`](../requisitos/DocumentoGeral-Req-CDU.md) — padrão IEEE/ANSI 830-1998.

O sistema é dividido em **Aplicativo Semente Livre** (gestão interna) e **Site Semente Livre** (público).

---

## 2.1 Aplicativo Semente Livre

### Usuários
Produtores rurais familiares com base agroecológica, familiarizados com smartphones.

### Requisitos Funcionais

| ID | Requisito | Descrição resumida |
|---|---|---|
| RF-01 | Manter Proprietário | CRUD de proprietário (nome\*, RG\*\*, CPF\*\*, telefone, e-mail, endereço) |
| RF-02 | Manter Propriedade | CRUD de propriedade (nome, tamanho em hectares, endereço, comunidade) |
| RF-03 | Manter Semente | CRUD de sementes/mudas com foto, tipo, preço, disponibilidade |
| RF-05 | Manter Pedidos | Pedidos de saída (venda/troca/doação) com atualização automática de estoque |
| RF-06 | Autenticação e Acesso | Login, cadastro, recuperação de senha, sessão expira em 30 min |
| RF-07 | Gerar Relatórios | Listagens com filtros e exportação em PDF e CSV |
| RF-08 | Gerar Notificação | Notificação ao registrar pedido, com histórico lida/não lida |

> O RF-04 foi removido da versão atual do documento de requisitos.

### Requisitos Não Funcionais (destaques)

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-01 | Operar em smartphones online | Essencial |
| RNF-02 | Android 5.0+ (Lollipop) | Essencial |
| RNF-03 | Interface amigável, paleta do IF, acessibilidade ARIA, PT-BR | Essencial |
| RNF-04 | Persistência em nuvem com suporte offline e sincronização | Essencial |
| RNF-06 | Execução ágil das operações | Importante |
| RNF-07 | Uptime ≥ 99,5%, backup diário | Importante |
| RNF-08 | HTTPS/SSL, hashing+salting, sessão 30 min | Essencial |
| RNF-09 | Conformidade com LGPD | Essencial |
| RNF-10 | Código modular, curva de aprendizado ≤ 2 semanas | Importante |

### Casos de Uso (CDU) — Aplicativo

| CDU | Descrição |
|---|---|
| CDU-01 | Alterar dados cadastrais do proprietário |
| CDU-02 | Cadastrar propriedade |
| CDU-03 | Alterar propriedade |
| CDU-04 | Excluir propriedade (bloqueada se houver dependências) |
| CDU-05 | Gerar relatórios |
| CDU-06 | Autenticar proprietário |
| CDU-07 | Recuperar senha |
| CDU-08 | Cadastrar semente (exige ao menos 1 propriedade) |
| CDU-09 | Alterar semente |
| CDU-10 | Excluir semente (preserva pedidos anteriores) |
| CDU-11 | Consultar semente |
| CDU-12 | Consultar pedidos |
| CDU-13 | Alterar pedido (recalcula estoque) |
| CDU-14 | Excluir pedido (restaura estoque) |
| CDU-15 | Cadastrar proprietário (novo usuário) |
| CDU-16 | Consultar propriedade |
| CDU-17 | Exportar relatório (PDF/CSV) |
| CDU-18 | Visualizar estoque |
| CDU-19 | Sair do sistema |
| CDU-20 | Selecionar comunidade |
| CDU-21 | Solicitar comunidade (status pendente de aprovação) |
| CDU-22 | Cadastrar estoque |
| CDU-23 | Alterar estoque (bloqueia saldo negativo) |
| CDU-24 | Excluir estoque (zera e marca indisponível) |
| CDU-25 | Selecionar relatório |
| CDU-26 | Gerar notificação de pedido |

---

## 2.2 Site Semente Livre

### Usuários
Pessoas interessadas em sementes crioulas para consulta, compra, troca ou doação.

### Requisitos Funcionais

| ID | Requisito |
|---|---|
| RF-01 | Listar sementes (vitrine pública) |
| RF-02 | Listar produtor (perfil público) |
| RF-03 | Listar propriedades |
| RF-04 | Buscar/filtrar (semente, município, comunidade, produtor, tipo de pedido, mapa) |
| RF-05 | Visualizar detalhes |
| RF-06 | Imprimir relatórios em PDF |
| RF-07 | Comprar/trocar/solicitar doação (contato ou pedido) |
| RF-08 | Mapa de sementes (geolocalização) — desejável |

### Casos de Uso — Site

| CDU | Descrição |
|---|---|
| CDU-01 | Listar sementes (com filtros e impressão PDF) |
| CDU-02 | Listar proprietários |
| CDU-03 | Listar propriedades |
| CDU-04 | Pedido de compra/troca/doação |

---

## 2.3 Funcionalidades Implementadas na Prática

Além do que consta no documento formal, o sistema implementado inclui módulos adicionais mapeados durante o desenvolvimento:

- **Backend:** CRUDs de Pessoa, Proprietário, Produto, Estoque, Pedido, Notificação, Relatório, Usuário e autenticação completa.
- **front-app (PWA):** dashboard, sementes, estoque, propriedades, pedidos, relatórios, notificações e perfil.
- **front-site (Site):** cadastro/login, catálogo, dashboard com catálogo, propriedades, solicitações de cadastro, pedidos e área administrativa de aprovação de comunidades/produtores.

---

*[Voltar à Wiki](README.md) · [01. Visão Geral](01-visao-geral.md) · [03. Arquitetura e Tecnologias](03-arquitetura-tecnologias.md)*