# Fase 0 — Preparação & Estudos

**Projeto:** Semente Livre  
**Disciplina:** AAIFE3 — IF Sudeste MG Campus Rio Pomba  
**Duração:** 1 semana  
**Equipe:** 8 desenvolvedores + 2 líderes

---

## Objetivo

Garantir que toda a equipe esteja tecnicamente preparada e que o ambiente de desenvolvimento esteja completamente configurado antes do início da implementação do backend.

---

## Atividades

### Desenvolvedores — Estudos Técnicos

Cada desenvolvedor será responsável por estudar e entregar um protótipo funcional do tema atribuído, garantindo domínio técnico antes do início do desenvolvimento em equipe.

| Dev | Tema de Estudo | Entregável |
|-----|----------------|------------|
| Dev 1 | Docker, Docker Compose, Spring Boot, Flyway | Repositório de estudo + cheatsheet |
| Dev 2 | Spring Security, JWT, filtros HTTP, BCrypt | Protótipo de autenticação funcional |
| Dev 3 | JPA/Hibernate, herança TPT, DTOs, validações | Entidade Pessoa mapeada e funcional |
| Dev 4 | Relacionamentos JPA, Lombok, Bean Validation | Protótipo Comunidade + Propriedade |
| Dev 5 | Spring Data JPA, enums PostgreSQL, upload | Protótipo Produto + Estoque |
| Dev 6 | Transações JPA, lógica em services, JUnit 5 | Protódio Pedido + Itens + teste |
| Dev 7 | Geração de PDF (iText), CSV (OpenCSV) | Relatório simples gerando PDF e CSV |
| Dev 8 | Swagger/OpenAPI, TestContainers, CI/CD | Projeto com Swagger + teste de integração |

---

### Karla (Líder de Gestão) — Ambiente & Kanban

| Tarefa | Descrição |
|--------|-----------|
| Repositório GitHub | Criar repositório, proteger branch `main`, configurar CODEOWNERS |
| Kanban (GitHub Projects) | Criar quadro com colunas: Backlog, A Fazer, Em Progresso, Review, Concluído |
| Cards | Criar cards para todas as tasks do projeto com labels e assignments |
| Milestones | Criar marcos por fase: Preparação, Fundação, Negócio, Integração, Qualidade |
| Templates | Criar templates de Pull Request e Issue padronizados |
| Documentação | Organizar pasta `docs/` com diagramas e modelo de dados |
| Ambiente | Garantir que todos consigam clonar e rodar o projeto (README atualizado) |

---

### Thales (Líder de Integração) — Infraestrutura de Testes

| Tarefa | Descrição |
|--------|-----------|
| Padrão de testes | Definir convenções de nomenclatura, estrutura de pastas e organização dos testes (unitários e de integração) |
| TestContainers | Configurar TestContainers para rodar testes de integração com PostgreSQL real em container |
| Fixture de dados | Criar classe utilitária para gerar dados de teste (pessoas, sementes, pedidos) de forma reutilizável |
| Template de teste | Criar template base de teste de integração (configuração de banco, setup/teardown, asserts padrão) |
| Cobertura | Definir meta mínima de cobertura de código (ex: 70%) e configurar ferramenta de medição (JaCoCo) |
| API Contracts | Documentar contratos das APIs principais para guiar os devs durante o desenvolvimento |

---

## Entregas da Fase 0

| Entregável | Responsável |
|------------|-------------|
| 8 protótipos funcionais (1 por dev) | Devs 1–8 |
| Repositório backend configurado | Karla |
| Kanban completo com cards e milestones | Karla |
| Ambiente de desenvolvimento funcionando | Karla + Dev 1 |
| Fluxo de branches e commits definido | Karla |
| Infraestrutura de testes configurada | Thales |
| Padrões e convenções de testes documentados | Thales |

---

## Cronograma

```
Semana 0 ──────────────────────────────────
  Devs 1-8: Estudos + protótipos   (7 dias)
  Karla:    Kanban + ambiente       (7 dias)
  Thales:   Infraestrutura testes   (7 dias)
───────────────────────────────────────────
```

---

*Documento de entrega — Projeto Semente Livre*  
*Versão 1.0 — Julho 2026*
