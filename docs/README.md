# Documentação — Semente Livre

Este diretório reúne toda a documentação técnica e de gestão do projeto **Semente Livre**, organizada por categoria para facilitar a navegação.

---

## Estrutura da pasta `docs/`

```
docs/
├── requisitos/       # Documento geral de requisitos e casos de uso (CDU)
├── modelo-dados/      # Modelo conceitual do banco de dados (MER)
├── diagramas/         # Diagramas UML (classes, sequência, implantação, DAO)
├── cronograma/        # Cronograma de sprints e plano da Fase 0
├── gestao/            # Guias de configuração e gestão (GitHub Projects)
└── README.md          # Este índice
```

---

## 1. Requisitos (`requisitos/`)

| Arquivo | Descrição |
|---|---|
| [DocumentoGeral-Req-CDU.md](requisitos/DocumentoGeral-Req-CDU.md) | Documento geral do projeto: visão geral, requisitos funcionais/não funcionais e casos de uso do App e do Site Semente Livre |

---

## 2. Modelo de Dados (`modelo-dados/`)

| Arquivo | Descrição |
|---|---|
| [Modelo-Conceitual-Banco-Dados.md](modelo-dados/Modelo-Conceitual-Banco-Dados.md) | Modelo Entidade-Relacionamento (MER), script DDL PostgreSQL, regras de integridade e diagrama de relacionamentos |

---

## 3. Diagramas (`diagramas/`)

| Arquivo | Descrição |
|---|---|
| [DiagramaDeClasses.md](diagramas/DiagramaDeClasses.md) | Diagrama de classes de domínio (nível de análise) — Mermaid |
| [DiagramaDeClassesMVC.md](diagramas/DiagramaDeClassesMVC.md) | Diagrama de classes com padrão MVC + Observer (nível de projeto) — Mermaid |
| [DiagramaDAOSingleton.drawio.xml](diagramas/DiagramaDAOSingleton.drawio.xml) | Diagrama de arquitetura DAO + Singleton (abrir em [draw.io](https://app.diagrams.net/)) |
| [DSeq-cdu9.md](diagramas/DSeq-cdu9.md) | Diagrama de sequência do CDU-09 (alteração de produto) — Mermaid |
| [deployment_diagram.puml](diagramas/deployment_diagram.puml) | Diagrama de implantação (PlantUML) — arquitetura Supabase/PostgreSQL |

---

## 4. Cronograma (`cronograma/`)

| Arquivo | Descrição |
|---|---|
| [Cronograma-Backend-Sprint.md](cronograma/Cronograma-Backend-Sprint.md) | Cronograma completo de 9 semanas: estrutura da equipe, fases, riscos, DoD e Gantt simplificado |
| [Fase0-Preparacao.md](cronograma/Fase0-Preparacao.md) | Detalhamento da Fase 0 (Semana 0) — estudos técnicos, setup de ambiente e infraestrutura de testes |
| [Fase1-Fundacao.md](cronograma/Fase1-Fundacao.md) | Detalhamento da Fase 1 (Semanas 1–2) — infraestrutura do projeto, mapeamento JPA e CRUDs básicos |

---

## 5. Gestão (`gestao/`)

| Arquivo | Descrição |
|---|---|
| [Guia-GitHub-Projects.md](gestao/Guia-GitHub-Projects.md) | Guia passo a passo de configuração do GitHub Projects: milestones, cards e organização do Kanban |

---

*Documentação organizada para o projeto Semente Livre — IF Sudeste MG Campus Rio Pomba*
