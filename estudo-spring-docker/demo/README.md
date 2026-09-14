# Projeto de Estudo: Spring Boot + Docker + Flyway

Esta pasta demonstra a criação de uma API em Spring Boot containerizada e integrada com PostgreSQL, utilizando Flyway para controle de versão do banco de dados.

---

## Documentação da API (Swagger / OpenAPI)

Com a aplicação rodando, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **JSON da especificação OpenAPI:** http://localhost:8080/v3/api-docs

## Testes

### Testes unitários e de repositório (H2, rápidos)

```bash
./mvnw test
```

### Teste de integração com TestContainers (PostgreSQL real em Docker)

Exige Docker instalado e rodando na máquina.

```bash
./mvnw test -Dtest=PostgresIntegrationTest
```

Esse teste sobe um container `postgres:15-alpine`, roda as migrations do Flyway
normalmente e valida a conexão com uma query simples.

### Rodar tudo (o que o CI executa)

```bash
./mvnw clean verify
```

## CI/CD

Todo push ou Pull Request para `main` ou `developer` que altere a pasta
`estudo-spring-docker/demo/` dispara automaticamente o workflow
`.github/workflows/ci.yml`, que builda o projeto com Java 17 e roda toda a
suíte de testes (unitários + integração com TestContainers).

## Como rodar localmente

```bash
# 1. Subir o Postgres (usuário/senha/porta definidos no docker-compose.yml)
docker compose up -d db

# 2. Definir as credenciais do datasource (Windows PowerShell)
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/estudodb"
$env:SPRING_DATASOURCE_USERNAME="admin"
$env:SPRING_DATASOURCE_PASSWORD="adminpassword"

# Linux/macOS (bash)
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/estudodb"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="adminpassword"

# 3. Rodar a aplicação
./mvnw spring-boot:run

# 4. Abrir no navegador
http://localhost:8080/swagger-ui.html
```

> **Nota:** essas variáveis precisam estar setadas na mesma sessão de terminal antes
do `spring-boot:run`. Se você já rodou testes com TestContainers na mesma sessão,
use um terminal novo ou limpe as variáveis antes (`Remove-Item Env:\SPRING_DATASOURCE_URL`
etc.) para não interferir nos testes que usam H2.

## Cheatsheet de Estudo

### Docker: Containers vs VMs
*   **VMs (Virtual Machines):** Virtualizam o hardware completo, rodando um Sistema Operacional inteiro para cada aplicação. São pesadas e lentas.
*   **Containers (Docker):** Virtualizam apenas a nível de Sistema Operacional, compartilhando o Kernel da máquina host. São leves, rápidos e resolvem o problema do "na minha máquina funciona".

### Comandos Básicos do Docker
*   `docker build -t nome-imagem .` : Constrói uma imagem a partir de um Dockerfile na pasta atual.
*   `docker run -p 8080:8080 nome-imagem` : Roda um container baseado em uma imagem.
*   `docker exec -it <id_container> /bin/sh` : Abre um terminal interativo dentro de um container rodando.
*   `docker logs -f <id_container>` : Mostra os logs do container em tempo real.
*   `docker stop <id_container>` : Para a execução de um container.
*   `docker rm <id_container>` : Remove um container que está parado (use `-f` para forçar a parada e remoção de uma vez).

### Comandos Essenciais do Docker Compose
*   **O que é:** Ferramenta para orquestrar múltiplos containers de uma vez (ex: API + Banco) através de um arquivo `.yml`.
*   `docker compose up` : Sobe todos os serviços configurados (use `-d` para rodar em background).
*   `docker compose up --build` : Força a recompilação (build) da imagem antes de subir os serviços.
*   `docker compose down` : Para e remove todos os containers e redes criadas pelo compose.
*   `docker compose down -v` : Remove os containers e também apaga os volumes (reseta o banco de dados).

### Flyway
*   **O que é:** Ferramenta de controle de versão (migrations) para o esquema do banco de dados.
*   **Convenção:** Os arquivos devem ser nomeados como `V<Versão>__<Descricao>.sql` (ex: `V1__create_table.sql`). Lembre-se: são **dois underlines**.
*   **Integração com Spring Boot:** Basta adicionar a dependência, configurar o banco no `application.yml` e colocar os scripts na pasta `src/main/resources/db/migration`. O Spring roda tudo automaticamente ao iniciar.
*   **Comandos Manuais (CLI/Maven):**
    *   `flyway migrate` : Executa as migrations pendentes.
    *   `flyway info` : Mostra o status de todas as migrations (quais já rodaram e quais estão pendentes).
    *   `flyway clean` : Apaga todos os objetos, tabelas e dados do banco (ótimo para ambiente de dev, perigoso em produção).
    *   `flyway repair` : Corrige a tabela de histórico caso alguma migration tenha falhado no meio do caminho.