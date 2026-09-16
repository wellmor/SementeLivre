# 09. Guia de Desenvolvimento

> Regras completas no [`README.md`](../../README.md) da raiz do repositório.

---

## 9.1 Clonar e preparar o ambiente

```bash
git clone git@github.com:wellmor/SementeLivre.git
cd SementeLivre
```

### Backend (Spring Boot)

```bash
# Subir o PostgreSQL local (porta 5433)
cd backend
docker compose up -d

# Rodar a API em modo dev
./mvnw spring-boot:run   # profile dev ativo por padrão
# URL: http://localhost:8080
```

> Banco dev: `jdbc:postgresql://localhost:5433/devdb` (`devuser`/`devpassword`). Flyway aplica as migrations automaticamente.

### front-app (PWA)

```bash
cd front-app
npm install
npm run dev   # http://localhost:3000
```

### front-site (Site Público)

```bash
cd front-site
npm install
npm run dev   # http://localhost:3001
```

---

## 9.2 Profile de ambiente (backend)

| Profile | Arquivo | Observação |
|---|---|---|
| `dev` (padrão) | `application-dev.yml` | PostgreSQL local via Docker, `ddl-auto=validate`, logs DEBUG |
| `prod` | `application-prod.yml` | Conecta em `${DB_URL}`, logs WARN/INFO |

---

## 9.3 Modelo de Branches

```
main
 └── developer
      ├── dev1 ... devN
```

| Regra | Descrição |
|---|---|
| `main` | Produção — **nunca** PR direto para `main` |
| `developer` | Integração — todos os PRs vão para ela |
| `dev{N}` | Branch individual de cada dev, criada a partir de `developer` |

### Comandos

```bash
# Atualizar sua base
git checkout developer
git pull origin developer
git checkout -b dev1

# Atualizar sua branch a partir da developer
git checkout dev1
git pull origin developer
```

---

## 9.4 Padrão de Commits

Formato: `tipo(descrição) - #número_da_task`

```bash
git commit -m "feat: adicionar endpoint de cadastro de pessoa - #12"
git commit -m "fix: corrigir validação de CPF no cadastro - #15"
git commit -m "docs: atualizar diagrama de classes - #20"
git commit -m "refactor: extrair lógica de validação para service - #18"
git commit -m "test: adicionar teste unitário para ProdutoService - #22"
git commit -m "chore: configurar Flyway migrations - #8"
```

| Tipo | Uso |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Documentação |
| `refactor` | Refatoração sem mudar comportamento |
| `test` | Testes |
| `chore` | Configuração, dependências, CI/CD |
| `style` | Formatação sem mudança lógica |

---

## 9.5 Fluxo de Pull Request

1. Atualizar `developer`.
2. Rebase da sua branch:
   ```bash
   git checkout dev1
   git rebase developer
   ```
3. Push: `git push origin dev1`.
4. Abrir PR `dev1 → developer` com título e body conforme padrão (referenciando a issue).
5. Aguardar revisão (regras abaixo) e merge.
6. Após merge, remover branch local e remota.

### Revisores obrigatórios

| Área | Revisor(es) |
|---|---|
| `*` | @kfrural |
| `backend/` | @ThalesTHM + @kfrural |
| `front-app/` | @kfrural |
| `front-site/` | @ThalesTHM |
| `docs/` | @ThalesTHM + @kfrural |

---

## 9.6 O que nunca fazer

- ~~`git push origin main`~~ — branch protegida.
- ~~PR direto para `main`~~ — sempre para `developer`.
- ~~Trabalhar direto na `developer`~~ — sempre na sua `dev{N}`.
- ~~Commitar arquivos sensíveis~~ (`.env`, senhas, tokens).

---

*[Voltar à Wiki](README.md) · [08. Autenticação e Segurança](08-autenticacao-seguranca.md) · [10. Infraestrutura e Deploy](10-infraestrutura-deploy.md)*