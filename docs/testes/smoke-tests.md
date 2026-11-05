# Smoke tests de produção (#138)

Suíte curta que confirma que um ambiente **já no ar** está funcionando de ponta a ponta. Ela passa pelos fluxos críticos (auth, catálogo, propriedade, pedido, relatório e notificação), pelo health check, pelo HTTPS e pelos dois frontends. Use depois de cada deploy ou redeploy.

- Código: `e2e/smoke/producao.spec.ts`, com a configuração em `e2e/playwright.smoke.config.ts`.
- Ferramenta: Playwright, a mesma da suíte e2e. Diferente da e2e, **não sobe frontend nenhum**: aponta para as URLs informadas.

## Como rodar

```bash
cd e2e
npm ci
npx playwright install chromium        # só na primeira vez

API_URL=https://<api>  APP_URL=https://<app>  SITE_URL=https://<site> \
SMOKE_EMAIL_COMPRADOR=<caixa-da-equipe> \
npm run test:smoke
```

| Variável | Para que serve | Padrão |
|---|---|---|
| `API_URL` | backend | `http://localhost:8080` |
| `APP_URL` | front-app (proprietário) | `http://localhost:3000` |
| `SITE_URL` | front-site (catálogo público) | `http://localhost:3001` |
| `SMOKE_EMAIL_COMPRADOR` | recebe a cópia do pedido de teste | `smoke-comprador@example.com` |

O relatório HTML fica em `e2e/smoke-report/` (`npx playwright show-report smoke-report`).

## O que é verificado

A suíte roda em série, nesta ordem:

| # | Fluxo | Verificação |
|---|---|---|
| 1 | Health check | `/actuator/health`, `/liveness` e `/readiness` respondem `UP` |
| 2 | HTTPS | API, app e site respondem em https, e o http redireciona para https (pulado quando as URLs são http, como no ambiente local) |
| 3 | Auth | cadastro, login, `/auth/me` com o papel de proprietário, refresh do token e 401 sem token |
| 4 | Propriedade | cadastro numa comunidade ativa, que aparece na listagem do produtor |
| 5 | Catálogo | produto e oferta aparecem no catálogo público, e o perfil público do produtor não traz e-mail, documento, RG nem telefone |
| 6 | Pedido e notificação | pedido pelo site público nasce `PENDENTE`, o produtor recebe a notificação, e o aceite deixa o pedido `CONFIRMADO` com a baixa de estoque (20 → 17 kg) |
| 7 | Relatórios | estoque em PDF (`%PDF`) e pedidos em CSV, com o comprador presente |
| 8 | front-app | login pela tela abre o dashboard; isso confirma a comunicação app → API, CORS e o cookie de sessão |
| 9 | front-site | o catálogo da página inicial encontra a oferta pela busca |

## Dados criados e limpeza

A suíte cria uma conta **"Smoke Test"** (`smoke-<id>@example.com`) com uma propriedade, um produto, uma oferta e um pedido. Ao final, **mesmo se algum teste falhar**, ela apaga o pedido, a oferta e o produto e depois exclui a conta (`DELETE /api/pessoas/{id}/conta`, a exclusão LGPD, que remove também propriedade, notificações e relatórios). Por fim confirma que o login dessa conta passa a responder 401.

> **Até o PR #228 entrar**, excluir o pedido deixava o registro do comprador órfão; com ele, a limpeza não deixa nada.

Enquanto a suíte roda (menos de 1 minuto), a oferta "Feijão Smoke …" fica visível no catálogo público.

A cópia do pedido de teste é enviada para `SMOKE_EMAIL_COMPRADOR`. Em produção, informe uma caixa da equipe para não gerar e-mail para endereço inexistente.

## Checklist de deploy

- [ ] Containers `db`, `app`, `caddy` e `db-backup` em `healthy`/`running` (`docker compose -f docker-compose.prod.yml ps`)
- [ ] `JWT_SECRET`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `DOMAIN_NAME` e `MAIL_*` definidos no `.env` do servidor
- [ ] `npm run test:smoke` contra as URLs de produção: todos os testes passando
- [ ] Resultado registrado na tabela abaixo

## Registro de execuções

| Data | Ambiente | Commit | Resultado |
|---|---|---|---|
| 04/11/2026 | local: backend em perfil `prod` (Docker) com os limites reais e frontends em build de produção (`next start`) | `developer` @ `f0cbad8` | ✅ 8 passaram, 1 pulado (HTTPS, URLs locais em http) em 15 s; limpeza confirmada no banco |
