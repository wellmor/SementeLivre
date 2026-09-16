# 03. Arquitetura e Tecnologias

## 3.1 Stack Tecnológica

| Camada | Tecnologia | Detalhes |
|---|---|---|
| **Backend** | Java 21 + Spring Boot | API REST, Spring Data JPA, Flyway migrations |
| **Segurança** | Spring Security + JWT | Autenticação stateless, BCrypt, refresh token |
| **Banco de dados** | PostgreSQL 15 | Via Supabase (prod) / Docker (dev) |
| **Banco de testes** | H2 | Testes de persistência/serviço |
| **front-app (PWA)** | Next.js + TypeScript | `next-pwa`, Service Worker e cache offline |
| **front-site** | Next.js + TypeScript | shadcn/ui, Tailwind CSS, API Routes |
| **Firebase** | Auth, Firestore, Storage | Persistência do app (PWA) |
| **Infraestrutura** | Docker + Docker Compose | Subida do PostgreSQL local |
| **CI/CD** | GitHub Actions | Build e testes do backend |

## 3.2 Arquitetura em Camadas (Backend)

O backend segue o padrão **MVC (Model-View-Controller)** com organização em camadas:

```
Controller (REST) → Service (regras de negócio) → Repository (JPA) → PostgreSQL
```

- **Controller:** recebe requisições HTTP, valida entrada e responde `DTOs`.
- **Service:** implementa regras de negócio (estoque, status de pedido, notificações).
- **Repository:** interfaces Spring Data JPA para persistência.
- **Entity:** classes JPA anotadas, espelhando o modelo relacional.
- **Security:** `JwtService`, `SecurityFilter`, `UserDetailsServiceImpl`, `SecurityConfig`.
- **Exception:** `GlobalExceptionHandler` centraliza erros em `ErrorResponse`.

## 3.3 Padrões de Projeto

- **DTOs** de requisição/resposta em todos os módulos (não expõe entidades diretamente).
- **Herança Table Per Type (TPT)** no domínio de pessoas (`pessoa_t` → `usuario_t`, `proprietario_t`, `admin_t`).
- **DAO + Singleton** documentado nos diagramas (página de diagramas de `docs/`).
- **Repository pattern** via Spring Data JPA.
- **Service layer** desacoplada dos controllers.

## 3.4 Justificativa das Escolhas

### Por que PWA?
- Funciona **offline** (Service Worker + cache) — essencial para área rural com conectividade instável.
- Instalação **sem loja de aplicativos** e com custo zero de distribuição.
- Multiplataforma e atualizável instantaneamente.
- Reutiliza o ecossistema React/TypeScript já dominado pela equipe.

### Por que Java Spring Boot?
- Segurança embutida (Spring Security + JWT).
- Integridade transacional e persistência madura (Spring Data JPA + Flyway).
- Forte tipagem e maturidade empresarial (ideal para dados sensíveis/LGPD).
- Ecossistema amplo para relatórios PDF/CSV, e-mails e testes.

> Quadro comparativo detalhado entre PWA/React/Flutter/nativo e Spring/Node/Python/ASP.NET/PHP está em [`Justificativa-Escolha-Tecnologias.md`](../Justificativa-Escolha-Tecnologias.md).

## 3.5 Diagramas

Os diagramas formais (classes, sequência, implantação, DAO) estão em [`diagramas/`](../diagramas/):

| Diagrama | Formato |
|---|---|
| Diagrama de Classes (análise) | Mermaid |
| Diagrama de Classes MVC + Observer (projeto) | Mermaid |
| Diagrama DAO + Singleton | draw.io |
| Diagrama de Sequência CDU-09 | Mermaid |
| Diagrama de Implantação | PlantUML |

---

*[Voltar à Wiki](README.md) · [02. Requisitos e Casos de Uso](02-requisitos-casos-de-uso.md) · [04. Modelo de Dados](04-modelo-de-dados.md)*