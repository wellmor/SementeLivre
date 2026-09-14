# Fase 1 — Fundação

**Projeto:** Semente Livre  
**Disciplina:** AAIFE3 — IF Sudeste MG Campus Rio Pomba  
**Duração:** 2 semanas (Semanas 1–2)  
**Equipe:** 8 desenvolvedores + 2 líderes

---

## Objetivo

Construir um projeto Spring Boot funcional com infraestrutura completa, todas as entidades do modelo de dados mapeadas com JPA e CRUD básico operacional de cada domínio, preparando o terreno para a lógica de negócio e segurança das Fases posteriores.

---

## Week 1 — Estruturação & Mapeamento (Semana 1)

> **Tema da semana:** Setup de infraestrutura, mapeamento de todas as entidades JPA e estrutura base de pacotes.

### Desenvolvedores — Infraestrutura & Mapeamento

Cada desenvolvedor estrutura sua parte do projeto nesta semana, com foco em deixar o projeto buildando e as entidades mapeadas antes de iniciar os CRUDs na Semana 2.

| Dev | Tarefas da Semana 1 | Entregável |
|-----|---------------------|------------|
| **Dev 1** | Setup do projeto Spring Boot, Docker Compose para PostgreSQL, integração Flyway, configuração de `application.properties` (profiles dev/prod) | Projeto buildando + banco subindo via Docker Compose |
| **Dev 2** | Configuração da dependência do Spring Security, adição das dependências de JWT (jjwt/api/impl/jackson) no `pom.xml` | Dependências de segurança prontas no projeto |
| **Dev 3** | Mapeamento das entidades Pessoa, Logradouro e enums (TipoDocumento), base da herança Table Per Type | Entidades Pessoa/Logradouro mapeadas com JPA |
| **Dev 4** | Mapeamento das entidades Comunidade e Propriedade, enums StatusComunidade, relacionamentos com Logradouro/Proprietário | Entidades Comunidade/Propriedade mapeadas |
| **Dev 5** | Mapeamento das entidades Produto e Estoque, enums (TipoProduto, EspecieGeral, FormatoProduto, etc.), relacionamentos com Comunidade/Proprietário | Entidades Produto/Estoque mapeadas |
| **Dev 6** | Mapeamento das entidades Pedido e Itens, enums (TipoPedido, StatusPedido), relacionamentos com Usuario/Proprietario/Produto | Entidades Pedido/Itens mapeadas |
| **Dev 7** | Modelagem das entidades Notificacao e Relatorio, enums (TipoRelatorio) | Entidades Notificacao/Relatorio mapeadas |
| **Dev 8** | Estrutura dos pacotes base do projeto (config, controller, service, repository, dto, exception, util), interfaces genéricas e definição das interfaces de base | Estrutura de pacotes padronizada + interfaces base |

> **Nota de integração (Dev 1 + Dev 8):** A estrutura de pacotes deve ser definida por Dev 8 em conjunto com o setup de Dev 1 para garantir uma única convenção antes do código avançar.

---

### Karla (Líder de Gestão) — Semana 1

| Tarefa | Descrição |
|--------|-----------|
| Cards da Fase 1 | Quebrar as semanas 1 e 2 em cards do GitHub Projects com labels, milestones e assignments por dev |
| Padrão de importações | Definir convenção de organização de imports e formatação de código (e.g. Checkstyle/EditorConfig) |
| Review inicial | Acompanhar o setup de Dev 1 e confirmar que o projeto roda localmente |
| Padrão de DTOs | Definir convenção de nomenclatura e estrutura de DTOs (Request/Response) antes dos CRUDs da Semana 2 |

---

### Thales (Líder de Integração) — Semana 1

| Tarefa | Descrição |
|--------|-----------|
| Contratos de API | Documentar os contratos REST de cada domínio (recursos, verbos, payloads, status codes) para os devs seguirem nos CRUDs da Semana 2 |
| Teste de smoke do setup | Garantir que o TestContainers sobe o PostgreSQL e a aplicação conecta de ponta a ponta |
| Padrão de exception | Validar a estrutura de tratamento de erros proposta por Dev 1/Dev 8 |

---

## Week 2 — CRUDs & Testes de Integração (Semana 2)

> **Tema da semana:** Implementação das camadas repository/service/controller para cada domínio e primeira rodada de testes de integração.

### Desenvolvedores — CRUDs Básicos

| Dev | Tarefas da Semana 2 | Entregável |
|-----|----------------------|------------|
| **Dev 1** | Implementar tratamento global de exceções (`@RestControllerAdvice`), handler de erros padronizado, DTOs base (ErrorResponse) e validações de entrada | Tratamento de erros global + DTOs base |
| **Dev 2** | Configuração completa do Spring Security, filter chain base, bean `SecurityFilterChain` com rotas liberadas/inibidas para os CRUDs | Configuração de segurança compilando e sem quebrar os endpoints |
| **Dev 3** | CRUD Pessoa/Usuario/Proprietario/Admin: repository + service + controller (listar, buscar por id, criar, atualizar, excluir) | 3–4 CRUDs REST funcionais do domínio de pessoas |
| **Dev 4** | CRUD Comunidade/Propriedade: repository + service + controller | 2 CRUDs REST funcionais do domínio de território |
| **Dev 5** | CRUD Produto/Estoque: repository + service + controller | 2 CRUDs REST funcionais do domínio de catálogo |
| **Dev 6** | CRUD Pedido/Itens: repository + service + controller | 2 CRUDs REST funcionais do domínio de operações |
| **Dev 7** | CRUD Notificacao/Relatorio: repository + service + controller | 2 CRUDs REST funcionais do domínio de alertas/relatórios |
| **Dev 8** | Testes de integração do banco, validação do Docker Compose funcional de ponta a ponta com a aplicação | Testes de integração rodando + Docker Compose validado |

---

### Karla (Líder de Gestão) — Semana 2

| Tarefa | Descrição |
|--------|-----------|
| Review dos CRUDs | Revisar os PRs de cada domínio (qualidade de código, DTOs, validações) |
| Padronização | Garantir que todos os controllers sigam os contratos definidos por Thales na Semana 1 |
| Atualização do Kanban | Movimentar os cards conforme os CRUDs concluídos e registrar impedimentos no standup |

---

### Thales (Líder de Integração) — Semana 2

| Tarefa | Descrição |
|--------|-----------|
| Validação da API | Executar os endpoints CRUD contra o banco via TestContainers e confirmar o comportamento previsto nos contratos |
| Testes entre domínios (básico) | Verificar dependências FK entre entidades (Pessoa→Proprietario→Propriedade, Produto→Comunidade, etc.) |

---

## Entregas da Fase 1

| Entregável | Responsável |
|------------|-------------|
| Projeto Spring Boot buildando e rodando via Docker | Dev 1 + Dev 8 |
| PostgreSQL acessível via Docker Compose | Dev 1 |
| Todas as entidades mapeadas com JPA (Pessoa, Logradouro, Usuario, Proprietario, Admin, Comunidade, Propriedade, Produto, Estoque, Pedido, Itens, Notificacao, Relatorio) | Devs 3–7 |
| 7+ controllers REST funcionais (CRUD básico) | Devs 3–7 |
| Tratamento de exceções globais e erros padronizados | Dev 1 |
| Configuração Spring Security + dependências JWT compilando | Dev 2 |
| Estrutura de pacotes e convenções definidas | Dev 8 |
| Testes de integração com banco rodando | Dev 8 |
| Contratos de API documentados | Thales |
| Kanban da Fase 1 atualizado | Karla |

---

## Cronograma

```
Semana 1 ──────────────────────────────────
  Dev 1:  Setup + Docker + Flyway        (7 dias)
  Dev 2:  Dependências Security + JWT    (7 dias)
  Dev 3-7: Mapeamento das entidades       (7 dias)
  Dev 8:  Estrutura de pacotes + base    (7 dias)
  Karla:  Kanban + convenções            (7 dias)
  Thales: Contratos de API + smoke test  (7 dias)

Semana 2 ──────────────────────────────────
  Dev 1:  Exceptions globais + DTOs       (7 dias)
  Dev 2:  Filter chain + Security         (7 dias)
  Dev 3-7: CRUDs REST (painel)            (7 dias)
  Dev 8:  Testes de integração + Docker   (7 dias)
  Karla:  Review dos CRUDs + Kanban       (7 dias)
  Thales: Validação da API + integração   (7 dias)
───────────────────────────────────────────
```

---

## Dependências (da Fase 0 para a Fase 1)

- **Dev 1 (Setup)** deve concluir o ambiente da Semana 1 para os demais devs testarem suas entidades/CRUDs
- **Dev 8 (Estrutura de pacotes)** deve definir a base antes dos devs 3–7 criarem seus repositories/services/controllers
- **Thales (Contratos de API)** deve documentar os contratos antes dos devs 3–7 implementarem os controllers na Semana 2

---

## DoD aplicado nesta fase

- [x] Código implementado e commitado na branch correta (`feat/nome-da-feature`)
- [x] Projeto compilando e rodando via Docker Compose
- [x] Entidades mapeadas conforme o modelo conceitual
- [x] CRUDs básicos funcionais testados contra o banco
- [x] Code review aprovado por Karla
- [x] Integração com banco testada (Thales)
- [x] Card do GitHub Projects atualizado para "Concluído"

---

*Documento de entrega — Projeto Semente Livre*  
*Versão 1.0 — Agosto 2026*
