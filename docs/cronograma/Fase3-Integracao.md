# Fase 3 — Integração & Funcionalidades Avançadas

**Projeto:** Semente Livre  
**Disciplina:** AAIFE3 — IF Sudeste MG Campus Rio Pomba  
**Duração:** 2 semanas (Semanas 5–6)  
**Equipe:** 8 desenvolvedores + 2 líderes

---

## Objetivo

Integrar o backend Spring Boot com o front-app (PWA Next.js + Firebase) e o front-site (Next.js + API Routes), entregar as funcionalidades avançadas (relatórios PDF/CSV, notificações automáticas, health checks) e aprovar os testes cross-domain entre as entidades.

---

## Week 1 — Integração Frontend × Backend (Semana 5)

> **Tema da semana:** Exposição dos endpoints REST para os dois frontends, geração de relatórios e primeira rodada de testes cross-domain.

### Desenvolvedores — Endpoints & Integração

Cada desenvolvedor disponibiliza os endpoints do seu domínio para consumo dos frontends e integra as funcionalidades que dependem de conexão externa.

| Dev | Tarefas da Semana 5 | Entregável |
|-----|----------------------|------------|
| **Dev 1** | Monitoramento da aplicação: health checks (`/actuator/health`), métricas (Micrometer/Prometheus), logs de produção acessíveis | Health checks + métricas básicas funcionando |
| **Dev 2** | Integração dos endpoints de autenticação (login, cadastro, refresh, recuperação) com o front-app (substituindo/consumindo via REST) | Auth do front-app consumindo o backend |
| **Dev 3** | Endpoints públicos de perfil de produtor para o front-site (RF-02 do site): dados do proprietário + sementes cultivadas | Endpoints de perfil público consumidos pelo front-site |
| **Dev 4** | Integração propriedade-comunidade no front-app (CRUD de propriedades com seleção/solicitação de comunidade via API) | Fluxo de propriedades do front-app integrado |
| **Dev 5** | Endpoints públicos de catálogo para o front-site (RF-01/RF-04/RF-05 do site): listagem, busca e filtros de sementes | Catálogo público do front-site consumindo o backend |
| **Dev 6** | Integração de pedidos no front-app (registrar, detalhar, confirmar e cancelar pedido com atualização de estoque via API) | Fluxo de pedidos do front-app integrado |
| **Dev 7** | Geração de PDF/CSV de relatórios (RF-07) e endpoints de relatórios para o front-app e front-site | Relatórios PDF/CSV gerados pelos endpoints |
| **Dev 8** | Testes cross-domain: pedido→estoque e notificação→pedido (fluxos completos entre domínios) com TestContainers | Testes cross-domain aprovados |

> **Nota de integração (Dev 5 + Dev 6):** O cadastro de pedido no front-site (CDU-04 do site) depende do catálogo público de Dev 5 e das regras de estoque/notificação do backend. Dev 6 deve validar o contrato de criação de pedido com Dev 5 antes da Semana 6.

---

### Karla (Líder de Gestão) — Semana 5

| Tarefa | Descrição |
|--------|-----------|
| Cards da Fase 3 | Quebrar as semanas 5 e 6 em cards do GitHub Projects com labels, milestones e assignments por dev |
| Revisão da integração | Revisar os PRs de integração frontend-backend (qualidade de código, tratamento de erros nos clientes) |
| Acompanhamento dos frontends | Garantir que os specs do front-app e do front-site estejam alinhados com os contratos REST em produção |

---

### Thales (Líder de Integração) — Semana 5

| Tarefa | Descrição |
|--------|-----------|
| Contratos finais | Consolidar os contratos de API definitivos consumidos pelos dois frontends (front-app e front-site) |
| Setup de integração | Configurar o ambiente de integração (backend + frontends rodando juntos) e validar o CORS entre as aplicações |
| Validação cross-domain | Acompanhar Dev 8 nos testes pedido→estoque e notificação→pedido |

---

## Week 2 — Validação dos Fluxos & Testes Web (Semana 6)

> **Tema da semana:** Otimização de performance, validação de ponta a ponta de todos os fluxos dos frontends e testes de integração web sob responsabilidade de Thales.

### Desenvolvedores — Validação & Otimização

| Dev | Tarefas da Semana 6 | Entregável |
|-----|----------------------|------------|
| **Dev 1** | Otimização de queries (índices faltantes, N+1), introdução de cache em endpoints de leitura de catálogo | Consultas otimizadas + cache de leitura |
| **Dev 2** | Testes de autenticação cross-domain: login/cadastro/refresh percorrendo front-app → API → banco | Autenticação validada de ponta a ponta |
| **Dev 3** | Integração pessoa/proprietário no front-app: cadastro de proprietário, perfil e alteração de dados via API | Cadastro/perfil do front-app integrado |
| **Dev 4** | Validação do fluxo completo de comunidade no front-site: consulta, perfil de propriedade/comunidade e solicitação de nova comunidade | Fluxo de comunidade do front-site validado |
| **Dev 5** | Validação do fluxo estoque ↔ pedido: visualização de estoque refletindo as movimentações geradas pelos pedidos | Fluxo estoque ↔ pedido validado |
| **Dev 6** | Validação do fluxo de pedido completo no front-site e front-app: registrar, confirmar, cancelar e histórico | Fluxo de pedido completo validado |
| **Dev 7** | Validação dos relatórios com dados reais: filtros, PDF/CSV e prévia de relatórios com dados inseridos pelos testes de integração | Relatórios validados com dados reais |
| **Dev 8** | Testes de integração web (em conjunto com Thales): fluxos de ponta a ponta navegando pelos frontends contra o backend real | Suite de testes web de integração |

> **Nota de integração (Dev 8 + Thales):** Os testes de integração web da Semana 6 são liderados por Thales; Dev 8 apoia com a automatização e a manutenção dos testes de ponta a ponta.

---

### Karla (Líder de Gestão) — Semana 6

| Tarefa | Descrição |
|--------|-----------|
| Review dos fluxos | Revisar os PRs das validações de fluxo dos frontends |
| Critérios de aceite | Conferir se as rotas dos frontends cumprem os critérios de aceite dos CDUs correspondentes |
| Atualização do Kanban | Movimentar os cards conforme a validação dos fluxos e registrar impedimentos |

---

### Thales (Líder de Integração) — Semana 6

| Tarefa | Descrição |
|--------|-----------|
| Testes de integração web | Liderar os testes de ponta a ponta dos dois frontends contra o backend (fluxos de auth, catálogo, propriedade, pedido, relatório e notificação) |
| Validação de contratos | Conferir que todas as rotas dos frontends consomem os endpoints conforme os contratos finais da Semana 5 |
| Report de integração | Registrar o resultado dos testes web e os ajustes necessários para a Fase 4 |

---

## Entregas da Fase 3

| Entregável | Responsável |
|------------|-------------|
| Front-app (PWA Next.js + Firebase) integrado com backend | Devs 2–7 |
| Front-site (Next.js + API Routes) integrado com backend | Devs 3–7 |
| Todas as rotas dos frontends funcionais | Devs 2–7 |
| Relatórios PDF e CSV gerando corretamente | Dev 7 |
| Notificações sendo criadas automaticamente após pedidos | Backend (Fase 2) validado na integração |
| Testes cross-domain aprovados (pedido→estoque, notificação→pedido) | Dev 8 |
| Health checks e monitoramento básico | Dev 1 |
| Otimização de queries e cache de leitura | Dev 1 |
| Testes de integração web de ponta a ponta | Thales + Dev 8 |

---

## Cronograma

```
Semana 5 ──────────────────────────────────
  Dev 1:  Health checks + métricas          (7 dias)
  Dev 2:  Integração auth no front-app      (7 dias)
  Dev 3:  Endpoints perfil público (site)   (7 dias)
  Dev 4:  Integração propriedade-comunidade (7 dias)
  Dev 5:  Endpoints catálogo público (site) (7 dias)
  Dev 6:  Integração pedidos no front-app   (7 dias)
  Dev 7:  Relatórios PDF/CSV + endpoints    (7 dias)
  Dev 8:  Testes cross-domain               (7 dias)
  Karla:  Cards + review integração         (7 dias)
  Thales: Contratos finais + setup CORS     (7 dias)

Semana 6 ──────────────────────────────────
  Dev 1:  Otimização de queries + cache     (7 dias)
  Dev 2:  Testes de auth cross-domain       (7 dias)
  Dev 3:  Integração pessoa/proprietário    (7 dias)
  Dev 4:  Fluxo completo comunidade (site)  (7 dias)
  Dev 5:  Validação fluxo estoque ↔ pedido  (7 dias)
  Dev 6:  Validação fluxo pedido completo   (7 dias)
  Dev 7:  Validação relatórios com dados    (7 dias)
  Dev 8:  Testes de integração web (Thales) (7 dias)
  Karla:  Review fluxos + Kanban            (7 dias)
  Thales: Testes web de ponta a ponta       (7 dias)
───────────────────────────────────────────
```

---

## Dependências (da Fase 2 para a Fase 3)

- **Dev 1 (Monitoramento)** depende da infraestrutura da Fase 1 (Docker Compose) e da configuração de produção da Fase 2
- **Devs 2–7 (Integração)** dependem dos controllers, validações e autenticação prontos na Fase 2
- **Dev 5 (Catálogo público)** depende dos contratos de API de Thales e das regras de disponibilidade de produto da Fase 2
- **Dev 6 (Pedidos)** depende das regras de estoque/ciclo do pedido e da notificação automática da Fase 2 (Dev 6 + Dev 7)
- **Dev 8 (Testes cross-domain)** depende dos testes entre domínios da Fase 2 e da integração dos frontends da Semana 5

---

## DoD aplicado nesta fase

- [x] Código implementado e commitado na branch correta (`feat/nome-da-feature`)
- [x] Front-app (PWA) consumindo os endpoints do backend
- [x] Front-site consumindo os endpoints do backend
- [x] Todas as rotas dos frontends funcionais e validadas
- [x] Relatórios PDF e CSV gerando corretamente
- [x] Notificações sendo criadas automaticamente após pedidos
- [x] Testes cross-domain aprovados
- [x] Health checks e monitoramento básico ativos
- [x] Code review aprovado por Karla
- [x] Integração de ponta a ponta validada (Thales)
- [x] Card do GitHub Projects atualizado para "Concluído"

---

*Documento de entrega — Projeto Semente Livre*  
*Versão 1.0 — Agosto 2026*