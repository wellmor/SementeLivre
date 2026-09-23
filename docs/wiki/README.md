# Wiki — Semente Livre

Documentação oficial do sistema **Semente Livre**, um projeto de extensão do **IF Sudeste MG — Campus Rio Pomba** voltado à gestão de bancos de sementes crioulas da comunidade **Quilombola dos Coelhos** (Rio Pomba/MG).

---

## Sumário

| # | Página | Conteúdo |
|---|---|---|
| 01 | [Visão Geral](01-visao-geral.md) | O que é o projeto, objetivos, público-alvo e sistemas desenvolvidos |
| 02 | [Requisitos e Casos de Uso](02-requisitos-casos-de-uso.md) | Requisitos funcionais/não funcionais e casos de uso do app e do site |
| 03 | [Arquitetura e Tecnologias](03-arquitetura-tecnologias.md) | Stack tecnológica, arquitetura, justificativa das escolhas |
| 04 | [Modelo de Dados](04-modelo-de-dados.md) | MER, dicionário de dados, enums, regras de integridade |
| 05 | [API Backend](05-api-backend.md) | Endpoints REST do Spring Boot, DTOs, autenticação |
| 06 | [Frontend — PWA (front-app)](06-frontend-pwa.md) | Aplicativo interno: telas, hooks, off-line, Firebase |
| 07 | [Frontend — Site Público (front-site)](07-frontend-site.md) | Site público: catálogo, dashboard, área administrativa |
| 08 | [Autenticação e Segurança](08-autenticacao-seguranca.md) | JWT, Spring Security, BCrypt, LGPD, refresh token |
| 09 | [Guia de Desenvolvimento](09-guia-desenvolvimento.md) | Setup do ambiente, branches, padrão de commits, PRs |
| 10 | [Infraestrutura e Deploy](10-infraestrutura-deploy.md) | Docker, PostgreSQL/Supabase, CI/CD, profiles |
| 11 | [Gestão e Qualidade](11-gestao-qualidade.md) | GitHub Projects, testes, CI, cronograma |
| 12 | [Equipe e Contato](12-equipe-contato.md) | Membros, papéis, responsabilidades |

---

## Estrutura do Repositório

```
SementeLivre/
├── backend/          # API REST (Java 21 + Spring Boot + PostgreSQL)
├── front-app/        # PWA interna (Next.js + TypeScript + Firebase)
├── front-site/       # Site público (Next.js + shadcn/ui + Tailwind CSS)
├── docs/             # Documentos formais do projeto (requisitos, diagramas, cronograma)
├── wiki/             # Esta documentação contínua do sistema
├── estudo-spring-docker/  # PoC de estudos de Spring + Docker
└── README.md
```

---

*Wiki do projeto Semente Livre — IF Sudeste MG Campus Rio Pomba*