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
├── wiki/              # Documentação completa e contínua do sistema
├── Justificativa-Escolha-Tecnologias.md  # Justificativas (PWA/Spring) e quadro comparativo
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
| [Fase2-Negocio.md](cronograma/Fase2-Negocio.md) | Detalhamento da Fase 2 (Semanas 3–4) — lógica de negócio, validações e autenticação JWT |
| [Fase3-Integracao.md](cronograma/Fase3-Integracao.md) | Detalhamento da Fase 3 (Semanas 5–6) — integração frontend-backend, relatórios e testes cross-domain |
| [Fase4-Qualidade-Deploy.md](cronograma/Fase4-Qualidade-Deploy.md) | Detalhamento da Fase 4 (Semanas 7–8) — qualidade, correção de bugs, testes de carga e deploy final |

---

## 5. Gestão (`gestao/`)

| Arquivo | Descrição |
|---|---|
| [Guia-GitHub-Projects.md](gestao/Guia-GitHub-Projects.md) | Guia passo a passo de configuração do GitHub Projects: milestones, cards e organização do Kanban |

---

## 6. Justificativa Tecnológica

| Arquivo | Descrição |
|---|---|
| [Justificativa-Escolha-Tecnologias.md](Justificativa-Escolha-Tecnologias.md) | Parágrafo de justificativa da adoção de PWA e Java Spring Boot + quadros comparativos de frontend e backend (React, Node, ASP.NET, Python, Next.js, etc.) |

---

## 7. Wiki do Sistema

A documentação completa e contínua do sistema está na pasta [`wiki/`](wiki/README.md), com visão geral, requisitos, arquitetura, modelo de dados, API, frontends, segurança, guia de desenvolvimento, infraestrutura, gestão e equipe.

---

*Documentação organizada para o projeto Semente Livre — IF Sudeste MG Campus Rio Pomba*
