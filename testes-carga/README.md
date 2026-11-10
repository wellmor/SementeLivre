# Testes de carga, stress e concorrência (#130)

Script [k6](https://k6.io/docs/) que exercita a API com o tráfego misto dos fluxos críticos: auth, catálogo, estoque, pedidos e relatórios. Os resultados e as recomendações estão em [`docs/testes/relatorio-carga-stress-fase4.md`](../docs/testes/relatorio-carga-stress-fase4.md).

## Cenários

| `CENARIO` | O que faz | Critério |
|---|---|---|
| `carga` | carga nominal: `VUS` usuários (padrão 50) com 1 s de pausa, durante `DURACAO` (padrão 3m) | **falha** se o p95 de qualquer endpoint passar de 500 ms ou se mais de 1% das requisições der erro |
| `stress` | degraus de usuários simultâneos (`DEGRAUS`, padrão `50,100,200,300,400,600`), cada um por `DURACAO_DEGRAU` (padrão 1m), com 0,5 s de pausa | sem meta: mostra latência, vazão e erro por degrau, para achar o ponto de degradação |
| `concorrencia` | um estoque de 100 kg e uma fila de `PEDIDOS_FILA` (padrão 60) pedidos de 5 kg, todos aceitos ao mesmo tempo | **falha** se algum aceite der erro de servidor, se o estoque ficar negativo ou se mais de 20 pedidos forem aceitos |

O `setup` cria `USUARIOS` produtores (padrão 10), cada um com produto e oferta, pela própria API. Os dados de cada rodada são novos, então dá para repetir sobre o mesmo banco.

## Como rodar

**1. Backend em perfil `prod`, com os limites por IP afrouxados.** Todo o tráfego do k6 sai de um IP só, e sem isso o teste mediria o rate limit, não a API.

```bash
cd backend
export DB_PASSWORD=qualquer_senha_local JWT_SECRET=$(openssl rand -base64 48)
docker compose -p sl-carga -f docker-compose.prod.yml -f ../testes-carga/docker-compose.carga.yml up -d --build db app
```

**2. k6 de dentro da rede do Docker** (recomendado). No Windows e no macOS o port-forward do Docker Desktop recusa conexões acima de ~400 usuários simultâneos (`dial: connection refused`), o que não é problema da API.

```bash
docker run --rm --network sl-carga_app-network -v "$PWD/testes-carga:/scripts" \
  -e BASE_URL=http://app:8080 -e CENARIO=carga grafana/k6:1.3.0 run /scripts/api.js
```

Ou com o k6 instalado na máquina, contra a porta publicada:

```bash
k6 run -e CENARIO=carga testes-carga/api.js          # BASE_URL padrão: http://localhost:18080
k6 run -e CENARIO=stress testes-carga/api.js
k6 run -e CENARIO=concorrencia testes-carga/api.js
```

**3. Desligar:** `docker compose -p sl-carga -f docker-compose.prod.yml -f ../testes-carga/docker-compose.carga.yml down -v`

> `docker-compose.carga.yml` serve **só** para teste local. Ele publica a API sem o Caddy e desliga na prática os limites por IP. Nunca use esse arquivo em produção.
