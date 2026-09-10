# Semente Livre

Sistema de gestão de comunidades rurais para o IF Sudeste MG Campus Rio Pomba.

---

## Sumário

- [Estrutura do Repositório](#estrutura-do-repositório)
- [Modelo de Branches](#modelo-de-branches)
- [Padrão de Commits](#padrão-de-commits)
- [Fluxo de Pull Request](#fluxo-de-pull-request)
- [Regras de Revisão](#regras-de-revisão)
- [Equipe](#equipe)
- [Tecnologias](#tecnologias)

---

## Estrutura do Repositório

```
SementeLivre/
├── backend/          # API REST (Spring Boot + PostgreSQL)
├── front-app/        # PWA interna (Next.js 15 + TypeScript + Firebase)
├── front-site/       # Site público (Next.js + shadcn/ui + Tailwind CSS)
├── docs/             # Documentação do projeto
└── README.md
```

---

## Modelo de Branches

```
main
 └── developer
      ├── dev1
      ├── dev2
      ├── dev3
      ├── dev4
      ├── dev5
      ├── dev6
      ├── dev7
      └── dev8
```

### Regras

| Regra | Descrição |
|-------|-----------|
| **`main`** | Branch de produção. **Nunca** fazer pull request direto para `main`. |
| **`developer`** | Branch de integração. Todos os PRs devem ser feitos **para esta branch**. |
| **`dev{N}`** | Branch individual de cada dev (ex: `dev1`, `dev2`, ...). Criada a partir de `developer`. |

### Como criar sua branch

```bash
# Sempre atualizar a developer antes de criar sua branch
git checkout developer
git pull origin developer

# Criar sua branch individual
git checkout -b dev1
```

### Nunca faça

- ~~`git push origin main`~~ — branch protegida
- ~~Pull request direto para `main`~~ — sempre para `developer`
- ~~Trabalhar direto na `developer`~~ — sempre na sua `dev{N}`

---

## Padrão de Commits

### Formato

```
tipo(descrição) - #número_da_task
```

### Tipos permitidos

| Tipo | Quando usar |
|------|-------------|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Alteração apenas em documentação |
| `refactor` | Refatoração sem mudar comportamento |
| `test` | Adição ou correção de testes |
| `chore` | Configuração, dependências, CI/CD |
| `style` | Formatação, indentação (sem mudança lógica) |

### Exemplos

```bash
git commit -m "feat: adicionar endpoint de cadastro de pessoa - #12"
git commit -m "fix: corrigir validação de CPF no cadastro - #15"
git commit -m "docs: atualizar diagrama de classes - #20"
git commit -m "refactor: extrair lógica de validação para service - #18"
git commit -m "test: adicionar teste unitário para ProdutoService - #22"
git commit -m "chore: configurar Flyway migrations - #8"
```

### Regras de commit

- **Sempre** incluir o número da task/issue do GitHub no final: `- #número`
- Mensagem em **português**, clara e curta
- **Um commit por tarefa** (ou múltiplos commits se a tarefa for grande, todos com o mesmo `#número`)
- **Não commitar** arquivos sensíveis (senhas, tokens, `.env`)

---

## Fluxo de Pull Request

### Passo a passo

```bash
# 1. Atualizar a developer
git checkout developer
git pull origin developer

# 2. Rebase da sua branch (manter atualizada)
git checkout dev1
git rebase developer

# 3. Push da sua branch
git push origin dev1

# 4. Abrir Pull Request no GitHub
#    - De: dev1 → developer
#    - Título: feat: descrição da tarefa - #número
#    - Body: o que foi feito, como testar, issue vinculada

# 5. Aguardar revisão e aprovação

# 6. Após merge, deletar sua branch local e remota
git checkout developer
git pull origin developer
git branch -d dev1
git push origin --delete dev1
```

### Regras do Pull Request

| Regra | Detalhe |
|-------|---------|
| **Direção** | `dev{N}` → `developer` (nunca para `main`) |
| **Revisão obrigatória** | @kfrural precisa aprovar **todo** PR |
| **Revisão técnica** | @ThalesTHM precisa aprovar PRs que alterem `backend/` ou `front-site/` |
| **Descrição** | Todo PR deve descrever o que foi feito e como testar |
| **Issue vinculada** | Todo PR deve referenciar a task/issue do GitHub |

---

## Regras de Revisão

### Quem revisa o quê

| Área | Revisor(es) obrigatórios |
|------|--------------------------|
| `*` (qualquer coisa) | @kfrural |
| `backend/` | @ThalesTHM + @kfrural |
| `front-app/` | @kfrural |
| `front-site/` | @ThalesTHM |
| `docs/` | @ThalesTHM + @kfrural |

### Antes de pedir revisão

- [ ] Código compila/roda sem erros
- [ ] Testes passam (`npm test` ou equivalente)
- [ ] Não há arquivos sensíveis commitados (`.env`, senhas, tokens)
- [ ] Commit segue o padrão: `tipo(descrição) - #número`
- [ ] PR aponta para `developer` (não `main`)

### Após aprovação

- O PR será mergeado na branch `developer`
- Periodicamente, `developer` é mergeada em `main` (apenas pela Karla)

---

## Equipe

| Nome | GitHub | Papel |
|------|--------|-------|
| **Karla** | [@kfrural](https://github.com/kfrural) | Líder de Gestão — revisa tudo |
| **Thales** | [@ThalesTHM](https://github.com/ThalesTHM) | Líder de Integração — revisa backend e front-site |
| Dev 1 | [@mylenaantonelli](https://github.com/mylenaantonelli)  | Desenvolvimento |
| Dev 2 | [@victorlak](https://github.com/victorlak)  | Desenvolvimento |
| Dev 3 | [@IgorXF](https://github.com/IgorXF)  | Desenvolvimento |
| Dev 4 | [@PhenriqET](https://github.com/PhenriqET)  | Desenvolvimento |
| Dev 5 | [@wes23dev](https://github.com/wes23dev)  | Desenvolvimento |
| Dev 6 | [@joaocarlosms](https://github.com/joaocarlosms)  | Desenvolvimento |
| Dev 7 | [@WlBrito](https://github.com/WlBrito)  | Desenvolvimento |
| Dev 8 | [@otFaria](https://github.com/otFaria)  | Desenvolvimento |

> Preencher os usernames do GitHub de cada dev conforme forem sendo mapeados.

---

## Tecnologias

### Backend

- **Java 21** + **Spring Boot 3**
- **PostgreSQL** (banco de dados)
- **Spring Security** + **JWT** (autenticação)
- **Flyway** (migrations)
- **Docker** + **Docker Compose**

### Frontend — PWA Interna (`front-app`)

- **Next.js 15** + **TypeScript**
- **Firebase** (Auth, Firestore, Storage)
- **PWA** (Progressive Web App)

### Frontend — Site Público (`front-site`)

- **Next.js** + **TypeScript**
- **shadcn/ui** + **Tailwind CSS**
- **API Routes**

### Infraestrutura

- **GitHub Actions** (CI/CD)
- **Docker** (deploy)
- **GitHub Projects** (gestão de tasks)
