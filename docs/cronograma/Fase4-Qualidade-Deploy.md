# Fase 4 — Qualidade & Deploy

**Projeto:** Semente Livre  
**Disciplina:** AAIFE3 — IF Sudeste MG Campus Rio Pomba  
**Duração:** 2 semanas (Semanas 7–8)  
**Equipe:** 8 desenvolvedores + 2 líderes

---

## Objetivo

Elevar a cobertura de testes e a qualidade do software, corrigir bugs conhecidos de todos os domínios, otimizar a performance (incluindo Docker multi-stage) e realizar o deploy final completo do sistema, com validação em ambiente de produção e smoke tests.

---

## Week 1 — Qualidade, Segurança & Correções (Semana 7)

> **Tema da semana:** Otimização de infraestrutura, testes de segurança e carga, e correção de bugs de todos os domínios.

### Desenvolvedores — Correções & Otimização

Cada desenvolvedor corrige os bugs e débitos técnicos do seu domínio identificados nas fases anteriores, enquanto a infraestrutura é otimizada para produção.

| Dev | Tarefas da Semana 7 | Entregável |
|-----|----------------------|------------|
| **Dev 1** | Otimização do Docker: multi-stage build (imagem enxuta de runtime), redução de tamanho da imagem e tempos de build/start | Dockerfile multi-stage otimizado |
| **Dev 2** | Testes de segurança: pen test básico na API (injeção, tokens, headers HTTP, rate limit), validação de exposição de dados sensíveis | Relatório de segurança + correções aplicadas |
| **Dev 3** | Bug fixes do domínio Pessoa/Usuario/Proprietario: validações, unicidade, DTOs e tratamento de erros | Correções do domínio de pessoas revisadas |
| **Dev 4** | Bug fixes do domínio Comunidade/Propriedade: dependências de exclusão, similaridade de nome e status da comunidade | Correções do domínio de território revisadas |
| **Dev 5** | Bug fixes do domínio Produto/Estoque: upload de foto, disponibilidade, saldo e movimentações | Correções do domínio de catálogo revisadas |
| **Dev 6** | Bug fixes do domínio Pedido/Itens: regras de status, ciclo, restauração de estoque e validações | Correções do domínio de operações revisadas |
| **Dev 7** | Bug fixes do domínio Notificacao/Relatorio: geração automática, PDF/CSV e filtros de relatórios | Correções do domínio de alertas/relatórios revisadas |
| **Dev 8** | Testes de carga e stress da API (concorrência, tempos de resposta, limites de requisições) e validação da meta de cobertura | Testes de carga aprovados + cobertura reportada |

> **Nota de integração (Dev 1 + Dev 8):** Os testes de carga de Dev 8 rodam contra a imagem Docker otimizada de Dev 1 — os dois devem validar a build multi-stage antes dos testes de stress começarem.

---

### Karla (Líder de Gestão) — Semana 7

| Tarefa | Descrição |
|--------|-----------|
| Cards da Fase 4 | Quebrar as semanas 7 e 8 em cards do GitHub Projects com labels, milestones e assignments por dev |
| Triagem de bugs | Consolidar a lista de bugs conhecidos (issues e feedback das fases anteriores) e distribuir por domínio |
| Review das correções | Revisar os PRs de bug fix de cada domínio antes do fechamento |

---

### Thales (Líder de Integração) — Semana 7

| Tarefa | Descrição |
|--------|-----------|
| Ambiente de homologação | Preparar o ambiente de homologação (staging) espelhando a produção para os testes de carga e segurança |
| Validação de segurança | Auditar junto de Dev 2 as correções de segurança apontadas no pen test |
| Regressão web | Rodar uma suíte de regressão dos fluxos web (Fase 3) após os bug fixes |

---

## Week 2 — Deploy Final & Validação em Produção (Semana 8)

> **Tema da semana:** Deploy completo, validação de cada domínio em produção e smoke tests finais.

### Desenvolvedores — Validação em Produção

| Dev | Tarefas da Semana 8 | Entregável |
|-----|----------------------|------------|
| **Dev 1** | Deploy final: imagem Docker de produção, variáveis de ambiente/secrets, orquestração e backup configurado | Deploy final funcional via Docker |
| **Dev 2** | Validação do fluxo JWT em produção: login, cadastro, refresh, recuperação e expiração de sessão no ambiente real | JWT validado em produção |
| **Dev 3** | Validação dos cadastros em produção: pessoa, usuário, proprietário e integridade dos dados reais | Cadastros validados em produção |
| **Dev 4** | Validação de comunidades e propriedades em produção: fluxos completos com aprovação/rejeição | Comunidades/propriedades validados em produção |
| **Dev 5** | Validação do estoque em produção: saldos, movimentações, disponibilidade e upload de foto | Estoque validado em produção |
| **Dev 6** | Validação de pedidos em produção: ciclo completo (registrar, confirmar, cancelar) com atualização de estoque | Pedidos validados em produção |
| **Dev 7** | Validação de relatórios e notificações em produção: PDF/CSV e notificações pós-pedido | Relatórios/notificações validados em produção |
| **Dev 8** | Deploy completo com smoke tests: subir produção, executar os smoke tests de todos os domínios e finalizar a documentação de API | Smoke tests passando + deploy concluído |

---

### Karla (Líder de Gestão) — Semana 8

| Tarefa | Descrição |
|--------|-----------|
| Checklist de produção | Montar e acompanhar o checklist de deploy (variáveis de ambiente, domínio, HTTPS, backup, monitoramento) |
| Fechamento do Kanban | Encerrar os cards da Fase 4 e atualizar os milestones para "Concluído" |
| Documentação de entrega | Finalizar a documentação de entrega da disciplina (relatórios de teste, cobertura e deploy) |

---

### Thales (Líder de Integração) — Semana 8

| Tarefa | Descrição |
|--------|-----------|
| Smoke tests em produção | Validar os fluxos críticos de ponta a ponta no ambiente real (auth, catálogo, propriedade, pedido, relatório) |
| Validação de disponibilidade | Conferir uptime/health checks e os indicadores de monitoramento em produção |
| Documentação de API finalizada | Revisar e fechar a documentação de API (Swagger/OpenAPI) para entrega |

---

## Entregas da Fase 4

| Entregável | Responsável |
|------------|-------------|
| Deploy funcional via Docker (multi-stage) | Dev 1 + Dev 8 |
| Todos os bugs conhecidos resolvidos | Devs 2–7 |
| Testes de carga e stress aprovados | Dev 8 |
| Testes de segurança (pen test básico) aplicados | Dev 2 |
| Validação de cada domínio em produção | Devs 3–7 |
| Smoke tests passando em produção | Dev 8 + Thales |
| Documentação de API finalizada | Dev 8 + Thales |
| Checklist de produção e backup configurados | Dev 1 + Karla |
| Kanban e milestones encerrados | Karla |

---

## Cronograma

```
Semana 7 ──────────────────────────────────
  Dev 1:  Docker multi-stage + otimização  (7 dias)
  Dev 2:  Pen test + correções de segurança (7 dias)
  Dev 3:  Bug fixes Pessoa/Proprietário    (7 dias)
  Dev 4:  Bug fixes Comunidade/Propriedade (7 dias)
  Dev 5:  Bug fixes Produto/Estoque        (7 dias)
  Dev 6:  Bug fixes Pedido/Itens           (7 dias)
  Dev 7:  Bug fixes Notificação/Relatório  (7 dias)
  Dev 8:  Testes de carga e stress         (7 dias)
  Karla:  Triagem de bugs + review         (7 dias)
  Thales: Homologação + regressão web      (7 dias)

Semana 8 ──────────────────────────────────
  Dev 1:  Deploy final + variáveis         (7 dias)
  Dev 2:  Validação JWT em produção        (7 dias)
  Dev 3:  Validação cadastros em produção  (7 dias)
  Dev 4:  Validação comunidades produção   (7 dias)
  Dev 5:  Validação estoque em produção    (7 dias)
  Dev 6:  Validação pedidos em produção    (7 dias)
  Dev 7:  Validação relatórios produção    (7 dias)
  Dev 8:  Deploy completo + smoke tests    (7 dias)
  Karla:  Checklist + fechamento Kanban    (7 dias)
  Thales: Smoke tests + docs de API        (7 dias)
───────────────────────────────────────────
```

---

## Dependências (da Fase 3 para a Fase 4)

- **Dev 1 (Docker multi-stage)** depende do Docker Compose da Fase 1 e das otimizações da Fase 3
- **Devs 3–7 (Bug fixes)** dependem da lista de bugs consolidada na Fase 3 (testes web de Thales + feedback dos fluxos integrados)
- **Dev 2 (Segurança)** depende da autenticação/roles da Fase 2 e do pen test rodado após a integração da Fase 3
- **Dev 8 (Testes de carga)** depende da imagem otimizada de Dev 1 e do ambiente de homologação de Thales
- **Validação em produção (Semana 8)** depende de todos os bug fixes e otimizações da Semana 7

---

## DoD aplicado nesta fase

- [x] Código implementado e commitado na branch correta (`feat/nome-da-feature`)
- [x] Todos os bugs conhecidos resolvidos e revisados por Karla
- [x] Testes de carga e stress aprovados
- [x] Testes de segurança aplicados e corrigidos
- [x] Deploy final funcional via Docker multi-stage
- [x] Domínios validados em produção (Devs 3–7)
- [x] Smoke tests passando em produção
- [x] Documentação de API finalizada
- [x] Code review aprovado por Karla
- [x] Card do GitHub Projects atualizado para "Concluído"

---

*Documento de entrega — Projeto Semente Livre*  
*Versão 1.0 — Agosto 2026*