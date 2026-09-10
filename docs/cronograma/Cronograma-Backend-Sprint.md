# Cronograma de Desenvolvimento — Semente Livre

**Stack App (PWA):** Next.js 15 · TypeScript · Firebase (Firestore, Auth, Storage) · PWA  
**Stack Site:** Next.js · TypeScript · shadcn/ui · Tailwind CSS · API Routes  
**Stack Backend (planejado):** Spring Boot (Java) · PostgreSQL · JWT · Docker  
**Duração:** 9 semanas (1 de preparação + 8 de desenvolvimento)  
**Equipe de desenvolvimento:** 8 devs + 2 líderes  
**Data de início:** A definir (Semanas 0–8)  

> **Nota:** O front-app (PWA) e o front-site já possuem implementação avançada com Next.js + Firebase. O cronograma abaixo foca na infraestrutura backend e integração. As fases de frontend estão descritas no specs/ do respectivo repositório.

---

## 1. Estrutura da Equipe

### Liderança

| Nome | Papel | Responsabilidades |
|------|-------|-------------------|
| **Karla** | Líder de Gestão | Configuração do ambiente, Kanban, review de código, testes unitários, acompanhamento semanal, distribuição de tarefas, reports |
| **Thales** | Líder de Integração | Testes de integração web, validação de contratos API, deploy, infraestrutura |

### Desenvolvedores

| Dev | Domínio Principal | Escopo |
|-----|-------------------|--------|
| **Dev 1** | Infraestrutura & Projeto | Setup Spring Boot, Docker, configurações, flyway, exceptions globais |
| **Dev 2** | Autenticação & Segurança | JWT, login, cadastro, recuperação de senha, filtros de segurança |
| **Dev 3** | Pessoa, Usuário & Proprietário | CRUD Pessoa, Usuario, Proprietario, validações de documento |
| **Dev 4** | Comunidade & Propriedade | CRUD Comunidade, Propriedade, aprovação/rejeição, lógica de dependências |
| **Dev 5** | Produto & Estoque | CRUD Produto, Estoque, movimentações, cálculo de saldo |
| **Dev 6** | Pedido & Itens | CRUD Pedido, Itens, validação de estoque, atualização automática |
| **Dev 7** | Notificação & Relatório | Notificações, relatórios (PDF/CSV), filtros, exportação |
| **Dev 8** | Infraestrutura Avançada & Integração | API REST docs, CORS, tratamento de erros, testes de integração, Docker final |

---

## 2. Fases do Desenvolvimento

### Fase 0 — Preparação & Estudos (Semana 0)

**Objetivo:** Garantir que todos os devs estejam preparados tecnicamente e que o ambiente de desenvolvimento esteja 100% configurado antes do início do código.

#### Desenvolvedores — Estudos & Aperfeiçoamento

| Dev | Tema de Estudo | Entregável |
|-----|----------------|------------|
| **Dev 1** | Docker + Docker Compose na prática, Spring Boot (IOC, DI, profiles), Flyway | Repositório de estudo com exemplos + cheatsheet |
| **Dev 2** | Spring Security, JWT (conceito + implementação), filtros HTTP, BCrypt | Protótipo de autenticação funcional (hello world JWT) |
| **Dev 3** | JPA/Hibernate (mapeamento, validações, JPQL), herança TPT, DTOs | Repositório de estudo com entidade Pessoa mapeada |
| **Dev 4** | JPA relationships (OneToMany, ManyToOne), Lombok, validações Bean Validation | Protótipo com Comunidade + Propriedade mapeadas |
| **Dev 5** | Spring Data JPA (queries, specifications), enums no PostgreSQL, upload de arquivos | Protótipo com Produto + Estoque mapeados |
| **Dev 6** | Transações em JPA, lógica de negócio em services, testes unitários com JUnit 5 | Protótipo com Pedido + Itens + teste de transação |
| **Dev 7** | Geração de PDF (iText/OpenPDF), CSV (OpenCSV), templates de relatório | Relatório simples gerando PDF e CSV |
| **Dev 8** | Swagger/OpenAPI, testes de integração (TestContainers), CI/CD básico | Projeto com Swagger + 1 teste de integração com banco |

#### Karla — Configuração do Ambiente & Kanban

| Tarefa | Descrição | Entregável |
|--------|-----------|------------|
| **Repositório GitHub** | Criar o repositório backend, proteger branch `main`, configurar CODEOWNERS | Repositório configurado |
| **GitHub Projects (Kanban)** | Criar o quadro de tarefas com colunas: Backlog, A Fazer, Em Progresso, Review, Concluído | Quadro funcional com todos os cards criados |
| **Cards das tasks** | Criar cards para cada task do cronograma (estudo, CRUD, testes, integração, deploy) | Cards com labels, milestones e assignments |
| **Milestones** | Criar milestones por fase: "Fase 0 - Preparação", "Fase 1 - Fundação", etc. | Milestones com datas definidas |
| **Labels** | Criar labels: `infra`, `auth`, `pessoa`, `comunidade`, `produto`, `estoque`, `pedido`, `notificacao`, `relatorio`, `teste`, `docs` | Labels organizadas |
| **Template de PR** | Criar template de Pull Request com checklist | Template funcional |
| **Template de Issue** | Criar template de Issue com campos: domínio, prioridade, descrição, critérios de aceite | Template funcional |
| **Documentação do projeto** | Organizar a pasta `docs/` no repositório com diagramas, modelo de dados, cronograma | Docs organizados |
| **Ambiente de dev** | Garantir que todos os devs consigam clonar e rodar o projeto (README atualizado) | README com instruções claras |

**Entregas da Fase 0:**
- Todos os devs com protótipo funcional do seu domínio de estudo
- Repositório backend configurado no GitHub
- Kanban completo com todos os cards e milestones
- Ambiente de desenvolvimento funcionando para todos
- Fluxo de branches e commits definido

---

### Fase 1 — Fundação (Semanas 1–2)

**Objetivo:** Projeto funcional com infraestrutura completa, entidades mapeadas e CRUD básico operacional.

| Semana | Dev 1 | Dev 2 | Dev 3 | Dev 4 | Dev 5 | Dev 6 | Dev 7 | Dev 8 |
|--------|-------|-------|-------|-------|-------|-------|-------|-------|
| **S1** | Setup Spring Boot, Docker Compose, Flyway, properties | Configuração Spring Security, dependências JWT | Mapeamento Pessoa, Logradouro, enums | Mapeamento Comunidade, Propriedade, StatusComunidade | Mapeamento Produto, enums (TipoProduto, etc.) | Mapeamento Pedido, Itens, enums | Modelos Notificacao, Relatorio | Estrutura pacotes base, interfaces |
| **S2** | Exceptions globais, handler, DTOs base, validações | Configuração completa JWT, filter chain | CRUD Pessoa/Usuario/Proprietário (repo+service+controller) | CRUD Comunidade/Propriedade (repo+service+controller) | CRUD Produto/Estoque (repo+service+controller) | CRUD Pedido/Itens (repo+service+controller) | CRUD Notificação/Relatório (repo+service+controller) | Testes de integração do banco, Docker Compose funcional |

**Entregas da Fase 1:**
- Projeto Spring Boot buildando e rodando via Docker
- PostgreSQL acessível via Docker Compose
- Todas as 10 entidades mapeadas com JPA
- 7 controllers REST funcionais (CRUD básico)
- Exceptions globais e tratamento de erros padronizado
- Testes de integração com banco rodando

---

### Fase 2 — Lógica de Negócio & Segurança (Semanas 3–4)

**Objetivo:** Validações de negócio implementadas, autenticação JWT operacional, regras de estoque e pedido funcionando.

| Semana | Dev 1 | Dev 2 | Dev 3 | Dev 4 | Dev 5 | Dev 6 | Dev 7 | Dev 8 |
|--------|-------|-------|-------|-------|-------|-------|-------|-------|
| **S3** | Migrações Flyway, seeders, indexação | Login completo, cadastro, recuperação de senha, refresh token | Validações: unicidade CPF/RG/email, hash senha, regras de pessoa | Validações: verificação de dependências antes de excluir, similaridade de nome | Validações: campos obrigatórios, upload foto, regras de espécie | Validações: estoque insuficiente, status do pedido, fluxo de ciclo | Lógica de geração automática de notificação pós-pedido | CORS configurado, documentação Swagger/OpenAPI |
| **S4** | Atualização Flyway v2, logs estruturados | Autenticação completa testada, Roles (PROPRIETARIO, ADMIN) | Testes unitários Pessoa/Proprietário | Testes unitários Comunidade/Propriedade | Testes unitários Produto/Estoque | Testes unitários Pedido/Itens | Testes unitários Notificação/Relatório | Testes de integração entre domínios |

**Entregas da Fase 2:**
- Autenticação JWT completa (login, cadastro, refresh, logout)
- Roles implementadas (PROPRIETARIO, ADMIN)
- Validações de negócio em todas as camadas
- Flyway com todas as migrações
- Testes unitários de cada domínio
- Swagger documentado

---

### Fase 3 — Integração & Funcionalidades Avançadas (Semanas 5–6)

**Objetivo:** Integração frontend-backend, funcionalidades avançadas (relatórios, notificações push), testes cross-domain.

| Semana | Dev 1 | Dev 2 | Dev 3 | Dev 4 | Dev 5 | Dev 6 | Dev 7 | Dev 8 |
|--------|-------|-------|-------|-------|-------|-------|-------|-------|
| **S5** | Monitoramento, health checks, métricas | Integração endpoints auth com frontend | Endpoints de perfil do site público | Integração propriedade-comunidade no front-app | Endpoints de catálogo público (front-site) | Integração pedido no front-app | Geração PDF/CSV, endpoints relatórios | Testes cross-domain (pedido→estoque, notificação→pedido) |
| **S6** | Otimização de queries, cache | Testes de autenticação cross-domain | Integração pessoa/proprietário no front-app | Validação fluxo completo comunidade (front-site) | Validação fluxo estoque ↔ pedido | Validação fluxo pedido completo | Validação relatórios com dados reais | Testes de integração web (Thales) |

**Entregas da Fase 3:**
- Front-app (PWA Next.js + Firebase) integrado com backend
- Front-site (Next.js + API Routes) integrado com backend
- Todas as rotas dos frontends funcionais
- Relatórios PDF e CSV gerando corretamente
- Notificações sendo criadas automaticamente
- Testes cross-domain aprovados
- Health checks e monitoramento básico

---

### Fase 4 — Qualidade & Deploy (Semanas 7–8)

**Objetivo:** Cobertura de testes, correção de bugs, otimização de performance, deploy final.

| Semana | Dev 1 | Dev 2 | Dev 3 | Dev 4 | Dev 5 | Dev 6 | Dev 7 | Dev 8 |
|--------|-------|-------|-------|-------|-------|-------|-------|-------|
| **S7** | Otimização Docker, multi-stage build | Testes de segurança (pen test básico) | Bug fixes Pessoa/Proprietário | Bug fixes Comunidade/Propriedade | Bug fixes Produto/Estoque | Bug fixes Pedido/Itens | Bug fixes Notificação/Relatório | Testes de carga e stress |
| **S8** | Deploy final, variáveis de ambiente | Validação JWT em produção | Validação cadastros em produção | Validação comunidades em produção | Validação estoque em produção | Validação pedidos em produção | Validação relatórios em produção | Deploy completo, smoke tests |

**Entregas da Fase 4:**
- Deploy funcional via Docker
- Todos os bugs conhecidos resolvidos
- Testes de carga aprovados
- Documentação de API finalizada
- Smoke tests passando em produção

---

## 3. Matriz de Responsabilidades por Domínio

| Domínio | Dev | Entidades | Controllers | Testes Unitários | Testes Integração |
|---------|-----|-----------|-------------|------------------|-------------------|
| Infraestrutura | Dev 1 | — | — | — | Dev 8 |
| Autenticação | Dev 2 | — | AuthController | Dev 2 | Dev 8 |
| Pessoa/Usuário | Dev 3 | Pessoa, Usuario, Logradouro | PessoaController, UsuarioController | Dev 3 | Thales |
| Proprietário | Dev 3 | Proprietario | ProprietarioController | Dev 3 | Thales |
| Comunidade | Dev 4 | Comunidade, StatusComunidade | ComunidadeController | Dev 4 | Thales |
| Propriedade | Dev 4 | Propriedade | PropriedadeController | Dev 4 | Thales |
| Produto | Dev 5 | Produto, TipoProduto, EspecieGeral, FormatoProduto | ProdutoController | Dev 5 | Thales |
| Estoque | Dev 5 | Estoque, Pesagem, DisponibilidadeProduto, TipoMovimentacao | EstoqueController | Dev 5 | Thales |
| Pedido | Dev 6 | Pedido, Itens, TipoPedido, StatusPedido | PedidoController | Dev 6 | Thales |
| Notificação | Dev 7 | Notificacao | NotificacaoController | Dev 7 | Thales |
| Relatório | Dev 7 | Relatorio, TipoRelatorio | RelatorioController | Dev 7 | Thales |
| Integração | Dev 8 | — | — | — | Dev 8 + Thales |

---

## 4. Dependências entre Sprints

```
Semana 0 (Preparação)
    │
    ├── Devs 1-8 (Estudos + protótipos)
    ├── Karla (Ambiente + Kanban)
    └── Thales (Preparação testes)
                                │
Semana 1-2 (Fundação) ◄────────┘
    │
    ├── Dev 1 (Infra) ──────────┐
    ├── Dev 2 (Auth) ───────────┤
    ├── Dev 3-7 (Models+CRUD) ──┤
    └── Dev 8 (Testes) ─────────┘
                                │
Semana 3-4 (Negócio) ◄─────────┘
    │
    ├── Dev 2 (JWT completo) ────┐
    ├── Dev 3-7 (Validações) ────┤
    └── Dev 8 (Swagger+CORS) ────┘
                                  │
Semana 5-6 (Integração) ◄────────┘
    │
    ├── Dev 3-7 (Frontend) ──────┐
    ├── Dev 7 (PDF/CSV) ─────────┤
    └── Dev 8 (Cross-domain) ────┘
                                  │
Semana 7-8 (Qualidade) ◄─────────┘
    │
    └── Todos (Bug fixes + Deploy)
```

---

## 5. Fluxo de Commits & Rastreabilidade

**Padrão de branch:**
```
feat/nome-da-feature
```

**Padrão de commit:**
```
#feat/nome-da-feature #<numero-do-card>

Descrição breve da alteração realizada.
```

**Exemplo:**
```
#feat/crud-pessoa #49

Implementa CRUD completo de Pessoa com validação de unicidade de CPF e email.
```

**Regras:**
1. Toda feature branch deve ser criada a partir de `main`
2. Todo commit deve referenciar o card do GitHub Projects
3. Todo PR deve ser revisado por pelo menos 1 líder antes do merge
4. Karla faz review de qualidade do código
5. Thales faz review de integração e testes

---

## 6. Reuniões & Acompanhamento

| Evento | Frequência | Responsável | Participantes |
|--------|------------|-------------|---------------|
| Daily (standup) | Diária (15 min) | Karla | Todos |
| Sprint Review | Quinzenal | Karla + Thales | Todos |
| Code Review | Contínuo | Karla | Dev + reviewer |
| Integração | Semanal | Thales | Dev 8 + dev do domínio |
| Retrospectiva | Fim de cada fase | Karla | Todos |

---

## 7. Riscos & Mitigações

| Risco | Impacto | Mitigação |
|-------|---------|-----------|
| Devs sem experiência em Spring Boot | Alto | Semana 0 de estudos + Thales oferece suporte técnico |
| Atraso em infraestrutura (Dev 1) | Alto | Dev 8 auxilia; priorizar setup mínimo viável |
| Conflitos de merge entre domínios | Médio | Branches por feature, commits frequentes |
| Integração frontend-backend tardia | Alto | Contratos de API definidos desde a Semana 1 |
| Indisponibilidade de dev | Alto | Documentação clara; outro dev do grupo assume |
| Kanban desatualizado | Médio | Karla atualiza diariamente nos standups |

---

## 8. Definição de Pronto (DoD)

Uma tarefa é considerada **pronta** quando:

- [x] Código implementado e commitado na branch correta
- [x] Testes unitários escritos e passando
- [x] Code review aprovado por Karla
- [x] Integração com banco testada (Thales)
- [x] Documentação da API atualizada (se aplicável)
- [x] Card do GitHub Projects atualizado para "Concluído"

---

## 9. Cronograma Visual (Gantt Simplificado)

```
Semana:  0        1    2    3    4    5    6    7    8
         ──────── ──── ──── ──── ──── ──── ──── ──── ────
Dev 1    [Estudo ][====Fundação====][==Negócio==][==Integração==][==Qualidade==]
Dev 2    [Estudo ][====Fundação====][==Negócio==][==Integração==][==Qualidade==]
Dev 3    [Estudo ][====Fundação====][==Negócio==][==Integração==][==Qualidade==]
Dev 4    [Estudo ][====Fundação====][==Negócio==][==Integração==][==Qualidade==]
Dev 5    [Estudo ][====Fundação====][==Negócio==][==Integração==][==Qualidade==]
Dev 6    [Estudo ][====Fundação====][==Negócio==][==Integração==][==Qualidade==]
Dev 7    [Estudo ][====Fundação====][==Negócio==][==Integração==][==Qualidade==]
Dev 8    [Estudo ][====Fundação====][==Negócio==][==Integração==][==Qualidade==]
Karla    [===Kanban + Ambiente===][Review contínuo──────────────────────────────]
Thales   [==Prep Testes==][═══════Testes Integração══════════════════════════════]
```

---

*Documento elaborado para o projeto Semente Livre — IF Sudeste MG Campus Rio Pomba*  
*Versão 2.0 — Agosto 2026*
