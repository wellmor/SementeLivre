# Justificativa da Escolha Tecnológica — Semente Livre

**Contexto:** Projeto de extensão para a comunidade **Quilombola dos Coelhos** (Rio Pomba/MG) — gestão de bancos de sementes crioulas.  
**Data:** setembro de 2026  
**Uso:** subsídio para o relatório final e para o artigo científico.  

---

## 1. Justificativa da Adoção de PWA (Progressive Web App) e Java Spring Boot

A decisão de adotar **PWA (Progressive Web App) para o frontend** e **Java com Spring Boot para o backend** foi orientada pela realidade do público-alvo e pelos requisitos não funcionais do sistema. A PWA reúne o melhor dos dois mundos: a facilidade de uso de um aplicativo, sem depender de publicação em lojas de aplicativos — fator crítico para uma comunidade rural em que o acesso a dados móveis é intermitente. Graças ao Service Worker e ao cache local, o aplicativo interno Semente Livre continua operando de forma funcional em áreas de baixa ou nenhuma conectividade e sincroniza os dados assim que a conexão é restabelecida, atendendo diretamente o requisito RNF-04 (suporte offline com sincronização). Além disso, a PWA é um aplicativo web progressivo instalável, multiplataforma, atualizável sem processos de homologação e com custo zero de distribuição — características que se alinham tanto à limitação de infraestrutura da comunidade quanto ao caráter acadêmico e extensionista do projeto. No backend, a adoção de **Java 21 com Spring Boot** traz um framework robusto, maduro e amplamente documentado, com injeção de dependências, persistência por meio do Spring Data JPA, migrações versionadas com Flyway e uma camada consolidada de segurança com Spring Security e autenticação JWT. Essa combinação garante forte tipagem, integridade transacional, manutenibilidade e conformidade com boas práticas empresariais — requisitos essenciais para um sistema que manipula dados pessoais (LGPD), movimenta estoque agrícola e precisa evoluir com segurança ao longo do tempo. A opção pelo ecossistema Java também se justifica pela ampla disponibilidade de desenvolvedores no mercado, pela maturidade das bibliotecas (PDF/CSV, validação de documentos, envio de e-mails) e pela facilidade de testes automatizados, o que reduz o risco tecnológico e facilita a continuidade do projeto por equipes futuras.

---

## 2. Quadro Comparativo — Tecnologias de Frontend

Critérios: disponibilidade para a equipe, adequação ao contexto rural (offline/móvel), maturidade, custo de distribuição e manutenibilidade.

| Critério | **PWA — Next.js (escolhida)** | React (SPA) | Vue.js / Angular | Flutter | App nativo (Android) |
|---|---|---|---|---|---|
| **Adequação offline** | Alta (Service Worker + cache) | Média (requer libs extras) | Média | Média (suporte nativo parcial) | Alta |
| **Instalação/distribuição** | Sem lojas, link HTTPS | Sem lojas | Sem lojas | Exige loja (APK/AAB) | Exige loja |
| **Curva de aprendizado da equipe** | Baixa (time já usa TypeScript/React) | Baixa | Média a alta | Média | Média a alta |
| **Maturidade e comunidade** | Alta (Vercel, ecossistema React) | Alta | Alta (Vue) / Alta (Angular) | Média/alta | Alta |
| **Custo de desenvolvimento** | Baixo (um único código web) | Baixo | Baixo/Médio | Médio (recompila p/ cada plataforma) | Alto (dois códigos) |
| **SEO/catálogo público** (site) | Excelente (SSR/SSG) | Fraco (client-side) | Variável | Ruim | Ruim |
| **Manutenibilidade** | Alta (um só código) | Alta | Alta | Média | Baixa (manter 2 apps) |

**Veredito:** A PWA com **Next.js** vence por ser a única opção que combina funcionamento offline, instalação sem loja e facilidade de manutenção, além de reutilizar o mesmo ecossistema React/TypeScript já dominado pela equipe — permitindo inclusive o reuso de componentes e conhecimentos entre o app (`front-app`) e o site público (`front-site`).

---

## 3. Quadro Comparativo — Tecnologias de Backend

Critérios: maturidade, robustez/segurança, performance, disponibilidade da equipe, ecossistema de bibliotecas e testes.

| Critério | **Java — Spring Boot (escolhido)** | Node.js (Express/NestJS) | Python (Django/FastAPI) | ASP.NET Core (C#) | PHP (Laravel) |
|---|---|---|---|---|---|
| **Maturidade e documentação** | Altíssima | Alta | Alta | Alta | Alta |
| **Segurança embutida** | Excelente (Spring Security + JWT, BCrypt) | Boa (middlewares manuais) | Boa (Django Security) | Excelente | Boa |
| **Persistência/integridade** | Excelente (Spring Data JPA, transações, migrations Flyway) | Média (ORM opcional, mais manual) | Boa (Django ORM) | Excelente (EF Core) | Boa (Eloquent) |
| **Performance** | Alta (JVM, JIT) | Alta (I/O assíncrono) | Média | Alta | Média |
| **Tipagem e segurança de código** | Excelente (linguagem fortemente tipada) | Fraca a média (JS) / média (TS) | Média (tipagem dinâmica; FastAPI adiciona tipagem) | Excelente | Média |
| **Testes automatizados** | Excelente (JUnit, Mockito, H2) | Boa (Jest/Vitest) | Boa (pytest) | Boa (xUnit) | Regular |
| **Ecossistema p/ relatórios (PDF/CSV)** | Excelente (iText/JasperReports, OpenCSV) | Bom (pdfkit, csv) | Bom (ReportLab, pandas) | Bom | Bom |
| **Curva do time** | Média (Java tradicional em graduação) | Baixa | Baixa | Média | Média |
| **Banco relacional (PostgreSQL)** | Excelente (suporte nativo, H2 p/ testes) | Bom | Excelente (Django/PG) | Excelente | Bom |
| **Longevidade/mercado** | Altíssima | Alta | Alta | Alta | Alta |

**Veredito:** O **Spring Boot** foi escolhido pela combinação de segurança embutida, integridade transacional, forte tipagem e maturidade do ecossistema Java, elementos que reduzem riscos em um sistema com dados sensíveis (LGPD) e regras de negócio complexas (estoque, pedidos, notificações). Embora Node.js e Python tenham curvas mais suaves, o Spring Boot oferece a robustez e a longevidade desejadas para um sistema institucional que deverá ser mantido e evoluído por equipes acadêmicas e pela comunidade ao longo dos anos.

---

## 4. Comparativo Final e Conclusão

| Camada | Escolha | Principais alternativas avaliadas | Motivação central |
|---|---|---|---|
| Frontend (app interno) | **PWA — Next.js + TypeScript** | React SPA, Vue, Flutter, nativo Android | Offline + instalação sem loja + reuso de ecossistema |
| Frontend (site público) | **Next.js + TypeScript + shadcn/ui** | React SPA, HTML/CSS/JS puro | SEO, catálogo público, design system |
| Backend | **Java 21 + Spring Boot** | Node.js (Express/NestJS), Python (Django/FastAPI), ASP.NET, PHP (Laravel) | Segurança, tipagem, integridade transacional, maturidade |
| Banco de dados | **PostgreSQL (Supabase)** + Firestore (app) | MySQL, Firebase-only, MongoDB | Integridade relacional e backups automatizados |

A escolha atende simultaneamente aos requisitos essenciais (RNF-01, RNF-02, RNF-04, RNF-06, RNF-08 e RNF-09 do documento de requisitos), restrições do público rural e limitações do contexto acadêmico, mantendo o sistema simples de manter, seguro e preparado para crescer.

---

*Documento elaborado para o projeto Semente Livre — IF Sudeste MG Campus Rio Pomba*
