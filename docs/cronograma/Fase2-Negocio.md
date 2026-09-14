# Fase 2 — Lógica de Negócio & Segurança

**Projeto:** Semente Livre  
**Disciplina:** AAIFE3 — IF Sudeste MG Campus Rio Pomba  
**Duração:** 2 semanas (Semanas 3–4)  
**Equipe:** 8 desenvolvedores + 2 líderes

---

## Objetivo

Implementar as validações de negócio em todas as camadas, concluir a autenticação JWT com roles (PROPRIETARIO, ADMIN), e garantir as regras de estoque, pedido e notificação funcionando e cobertas por testes unitários e de integração.

---

## Week 1 — Validações & Autenticação Completa (Semana 3)

> **Tema da semana:** Validações de negócio, autenticação JWT completa (login, cadastro, refresh, recuperação) e migrações/seeders do banco.

### Desenvolvedores — Validações de Negócio & Segurança

Cada desenvolvedor implementa as regras de negócio do seu domínio e a infraestrutura de segurança/autenticação, preparando a base para os testes da Semana 2.

| Dev | Tarefas da Semana 3 | Entregável |
|-----|----------------------|------------|
| **Dev 1** | Migrações Flyway completas do schema, seeders de dados iniciais (admin, comunidades, produtos) e indexação otimizada | Schema completo via Flyway + dados de teste |
| **Dev 2** | Login completo, cadastro, recuperação de senha e refresh token com JWT | Autenticação JWT completa (access + refresh) |
| **Dev 3** | Validações de pessoa: unicidade de CPF/RG/email, hash de senha com BCrypt, regras de documento | Validações de negócio de pessoa implementadas |
| **Dev 4** | Validações de território: verificação de dependências antes de excluir, similaridade de nome de comunidade (CDU-21) | Validações de dependência e similaridade |
| **Dev 5** | Validações de catálogo: campos obrigatórios, upload de foto, regras de espécie/tipo | Validações de produto + upload de foto |
| **Dev 6** | Regras de negócio de pedido: estoque insuficiente, controle de status e fluxo de ciclo (confirmar/cancelar) | Regras de estoque/status/ciclo do pedido |
| **Dev 7** | Geração automática de notificação pós-pedido (CDU-26) integrada às regras de pedido/estoque | Notificação automática pós-pedido |
| **Dev 8** | Configuração de CORS (origens dos frontends) e documentação Swagger/OpenAPI | CORS configurado + API documentada no Swagger |

> **Nota de integração (Dev 6 + Dev 7):** A geração automática de notificação pós-pedido depende das regras de ciclo implementadas por Dev 6. Dev 7 deve alinhar com Dev 6 para integrar a notificação à conclusão do pedido.

---

### Karla (Líder de Gestão) — Semana 3

| Tarefa | Descrição |
|--------|-----------|
| Cards da Fase 2 | Quebrar as semanas 3 e 4 em cards do GitHub Projects com labels, milestones e assignments por dev |
| Review de validações | Revisar os PRs de validações de negócio de cada domínio |
| Acompanhamento de testes | Acompanhar a meta de cobertura de testes em conjunto com Thales |
| Dependências entre devs | Mapear e comunicar interações entre devs (ex.: Dev 6 ↔ Dev 7) |

---

### Thales (Líder de Integração) — Semana 3

| Tarefa | Descrição |
|--------|-----------|
| Contratos atualizados | Revisar e atualizar os contratos de API considerando validações e novos endpoints de autenticação |
| Validação da autenticação | Validar o fluxo de autenticação (login, cadastro, refresh, roles) de ponta a ponta contra o banco |

---

## Week 2 — Testes Unitários & Integração (Semana 4)

> **Tema da semana:** Autenticação testada com roles, testes unitários de cada domínio e testes de integração entre domínios.

### Desenvolvedores — Testes & Consolidação

| Dev | Tarefas da Semana 4 | Entregável |
|-----|----------------------|------------|
| **Dev 1** | Migração Flyway v2 (alterações da fase), logs estruturados (SLF4J/Logback) e logging de requisições HTTP | Migração v2 aplicada + logs estruturados |
| **Dev 2** | Autenticação completa testada, implementação das roles (PROPRIETARIO, ADMIN) e autorização por role nos endpoints | Roles funcionais + autenticação coberta por testes |
| **Dev 3** | Testes unitários de Pessoa/Usuario/Proprietario (JUnit 5 + Mockito) | Suíte de testes unitários do domínio de pessoas |
| **Dev 4** | Testes unitários de Comunidade/Propriedade | Suíte de testes unitários do domínio de território |
| **Dev 5** | Testes unitários de Produto/Estoque | Suíte de testes unitários do domínio de catálogo |
| **Dev 6** | Testes unitários de Pedido/Itens (incluindo regras de estoque/status/restauração) | Suíte de testes unitários do domínio de operações |
| **Dev 7** | Testes unitários de Notificacao/Relatorio | Suíte de testes unitários do domínio de alertas/relatórios |
| **Dev 8** | Testes de integração entre domínios (pedido→estoque, notificação→pedido) com TestContainers | Testes de integração cross-domain rodando |

> **Nota de cobertura:** A meta mínima de cobertura da fase (definida na preparação) deve ser verificada ao final da Semana 4, com apoio de Thales.

---

### Karla (Líder de Gestão) — Semana 4

| Tarefa | Descrição |
|--------|-----------|
| Review dos testes | Revisar os PRs de testes unitários e de integração |
| Meta de cobertura | Verificar se a meta mínima de cobertura da fase foi atingida |
| Atualização do Kanban | Movimentar os cards conforme os testes concluídos e registrar impedimentos |

---

### Thales (Líder de Integração) — Semana 4

| Tarefa | Descrição |
|--------|-----------|
| Validação da suíte | Apoiar Dev 8 nos testes de integração entre domínios e conferir a suíte completa |
| Meta de cobertura | Conferir e reportar a meta de cobertura da fase |

---

## Entregas da Fase 2

| Entregável | Responsável |
|------------|-------------|
| Autenticação JWT completa (login, cadastro, refresh, recuperação) | Dev 2 |
| Roles implementadas (PROPRIETARIO, ADMIN) | Dev 2 |
| Validações de negócio em todas as camadas | Devs 3–7 |
| Flyway com todas as migrações e seeders | Dev 1 |
| Geração automática de notificação pós-pedido | Dev 7 |
| CORS e Swagger/OpenAPI configurados | Dev 8 |
| Testes unitários de cada domínio | Devs 3–7 |
| Testes de integração entre domínios | Dev 8 |
| Meta de cobertura da fase verificada | Thales + Karla |
| Kanban da Fase 2 atualizado | Karla |

---

## Cronograma

```
Semana 3 ──────────────────────────────────
  Dev 1:  Migrações + seeders + índices    (7 dias)
  Dev 2:  Login/cadastro/refresh + JWT     (7 dias)
  Dev 3-7: Validações de negócio           (7 dias)
  Dev 8:  CORS + Swagger/OpenAPI           (7 dias)
  Karla:  Cards + review validações        (7 dias)
  Thales: Contratos + validação de auth    (7 dias)

Semana 4 ──────────────────────────────────
  Dev 1:  Flyway v2 + logs estruturados    (7 dias)
  Dev 2:  Roles + testes de auth           (7 dias)
  Dev 3-7: Testes unitários por domínio    (7 dias)
  Dev 8:  Testes de integração entre dom.  (7 dias)
  Karla:  Review testes + cobertura        (7 dias)
  Thales: Validação suíte + cobertura      (7 dias)
───────────────────────────────────────────
```

---

## Dependências (da Fase 1 para a Fase 2)

- **Dev 1 (Migrações/seeders)** deve concluir o schema para que as validações de negócio sejam testáveis contra dados reais
- **Dev 2 (Autenticação e roles)** habilita a validação de acesso aos endpoints no Frontend das próximas fases
- **Dev 7 (Notificação)** depende das regras de ciclo do **Dev 6 (Pedido)**
- **Dev 8 (Swagger/Testes)** depende dos controllers da Fase 1 e das validações da Fase 2

---

## DoD aplicado nesta fase

- [x] Código implementado e commitado na branch correta (`feat/nome-da-feature`)
- [x] Autenticação JWT completa e testada com roles
- [x] Validações de negócio em todas as camadas
- [x] Flyway com migrações e dados de teste aplicados
- [x] Testes unitários de cada domínio passando
- [x] Testes de integração entre domínios passando
- [x] Meta de cobertura atingida
- [x] Code review aprovado por Karla
- [x] Integração com banco testada (Thales)
- [x] Card do GitHub Projects atualizado para "Concluído"

---

*Documento de entrega — Projeto Semente Livre*  
*Versão 1.0 — Agosto 2026*
