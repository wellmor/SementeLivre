# 11. Gestão e Qualidade

## 11.1 Gestão de Projetos

- Painel **GitHub Projects** (Kanban) com milestones e cards.
- Guia de configuração em [`gestao/Guia-GitHub-Projects.md`](../gestao/Guia-GitHub-Projects.md).
- Template de issue para **tasks** em `.github/ISSUE_TEMPLATE/task.md`.
- Template de **pull request** em `.github/pull_request_template.md`.

---

## 11.2 Cronograma de Sprints

| Fase | Semanas | Escopo |
|---|---|---|
| **Fase 0 — Preparação** | Semana 0 | Estudos técnicos, setup de ambiente, infraestrutura de testes |
| **Fase 1 — Fundação** | 1–2 | Infraestrutura do projeto, mapeamento JPA, CRUDs básicos |
| **Fase 2 — Negócio** | 3–4 | Lógica de negócio, validações, autenticação JWT |
| **Fase 3 — Integração** | 5–6 | Integração frontend-backend, relatórios, testes cross-domain |
| **Fase 4 — Qualidade e Deploy** | 7–8 | Qualidade, correção de bugs, testes de carga, deploy final |

> Detalhamento completo em [`cronograma/`](../cronograma/).

---

## 11.3 Qualidade e Testes

### Backend

| Tipo | Tecnologia | Escopo |
|---|---|---|
| Testes unitários | JUnit 5 + Mockito | Services (ex.: `NotificacaoServiceTest`, `RelatorioServiceTest`) |
| Testes de integração/persistência | Spring Boot Test + H2/Testcontainers | `*PersistenciaTest` |
| Migrações | Flyway | Validado na inicialização (`ddl-auto=validate`) |

### Comandos de teste

```bash
cd backend
./mvnw clean verify          # roda toda a suíte
./mvnw test -Dtest=NomeTest  # roda teste específico
```

### Frontends

- `npm run lint` (ESLint) em `front-app/` e `front-site/`.
- Ajustes de qualidade e testes funcionais previstos na Fase 4.

---

## 11.4 Documentação Complementar

| Documento | Local |
|---|---|
| Documento geral de requisitos/CDU | `docs/requisitos/DocumentoGeral-Req-CDU.md` |
| Modelo conceitual do banco | `docs/modelo-dados/Modelo-Conceitual-Banco-Dados.md` |
| Diagramas UML | `docs/diagramas/` |
| Cronograma e fases | `docs/cronograma/` |
| Justificativa tecnológica | `docs/Justificativa-Escolha-Tecnologias.md` |

---

*[Voltar à Wiki](README.md) · [10. Infraestrutura e Deploy](10-infraestrutura-deploy.md) · [12. Equipe e Contato](12-equipe-contato.md)*