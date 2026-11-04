# Relatório de testes de carga e stress — Fase 4 (#130)

**Data:** 04/11/2026
**Base testada:** `developer` @ `f0cbad8`, e depois a mesma base com a correção do PR #226 (login simultâneo)
**Ferramenta:** k6 (script em [`testes-carga/api.js`](../../testes-carga/api.js); como rodar em [`testes-carga/README.md`](../../testes-carga/README.md))
**Ambiente:** backend em **perfil `prod`** (imagem Docker multi-stage do `docker-compose.prod.yml`) + PostgreSQL 15 em container, numa máquina Intel i5-13450HX (16 threads, 15,7 GB). A VM do Docker tinha 16 CPUs e 7,6 GB. Limites por IP afrouxados só para o teste (`testes-carga/docker-compose.carga.yml`), porque todo o tráfego sai de um IP.

## Resumo

| Critério de aceite | Resultado |
|---|---|
| Endpoints críticos dentro do tempo de resposta sob carga nominal | ✅ p95 de **60 ms** no geral, e todos os endpoints abaixo de 80 ms (meta: 500 ms) |
| Taxa de erro sob stress documentada, com limites identificados | ✅ degradação **entre 300 e 400 usuários simultâneos** (~600 req/s); **0% de erro** até 600 usuários com a correção |
| Comportamento estável sob concorrência (sem deadlock ou estouro de conexão) | ✅ 60 aceites simultâneos sobre o mesmo estoque, 3 rodadas: sem deadlock, sem 500, estoque nunca negativo |
| Cobertura igual ou acima da meta | ✅ 94,2% de linhas (meta de 70%), no PR #225 e em [`relatorio-cobertura-fase4.md`](relatorio-cobertura-fase4.md) |
| Resultados e recomendações registrados | ✅ este documento |

**Bug encontrado pelo teste:** logins simultâneos da mesma conta respondiam 500 (0,04% das requisições na carga nominal). A correção está no **PR #226**. Os números abaixo vêm da rodada **com** a correção, salvo onde indicado.

## Tempo de resposta definido (SLO)

A documentação da fase não fixava um número, então ele foi definido para este teste e está aplicado como meta no script: **p95 abaixo de 500 ms em cada endpoint crítico e menos de 1% de erro sob carga nominal**.

**Carga nominal** é 50 usuários simultâneos, cada um fazendo uma requisição por segundo (~49 req/s). É bem acima do uso esperado de um banco comunitário de sementes, o que deixa margem.

## Mistura de tráfego

Cada iteração escolhe um produtor e um endpoint ao acaso:

| Peso | Endpoint | Tipo |
|---|---|---|
| 30% | `GET /catalogo/produtos` | público |
| 15% | `GET /catalogo/produtos/{id}` | público |
| 15% | `GET /estoques` | autenticado |
| 12% | `GET /pedidos?proprietarioId=` | autenticado |
| 8% | `POST /auth/login` | público (BCrypt) |
| 5% | `GET /relatorios/estoque/csv` | autenticado |
| 10% | `POST /catalogo/pedidos` (grava pedido, notificação e evento de e-mail) | público |
| 5% | `GET /auth/me` | autenticado |

## 1. Carga nominal — 50 usuários, 3 minutos

8.960 requisições (48,9 req/s), **0 erros**, todas as metas cumpridas.

| Endpoint | Mediana | p95 | p99 |
|---|---|---|---|
| `POST /auth/login` | 60,6 ms | 78,6 ms | 102,8 ms |
| `GET /catalogo/produtos` | 8,5 ms | 17,1 ms | 48,3 ms |
| `POST /catalogo/pedidos` | 6,9 ms | 14,0 ms | 29,1 ms |
| `GET /pedidos` | 4,2 ms | 8,7 ms | 19,1 ms |
| `GET /auth/me` | 3,4 ms | 8,1 ms | 25,9 ms |
| `GET /relatorios/estoque/csv` | 3,0 ms | 7,5 ms | 18,7 ms |
| `GET /estoques` | 2,6 ms | 6,0 ms | 15,3 ms |
| `GET /catalogo/produtos/{id}` | 2,5 ms | 5,9 ms | 13,7 ms |
| **Geral** | — | **60,1 ms** | 74,7 ms |

O login é o endpoint mais lento de propósito: o BCrypt custa ~60 ms de CPU por tentativa, e isso também freia força bruta.

## 2. Stress em degraus — até achar a degradação

Cada degrau dura 1 minuto, com 0,5 s de pausa por usuário. Total de 144.924 requisições e **0 erros**.

| Usuários simultâneos | Vazão (req/s) | Mediana | p95 | p99 | Máx. | Erro |
|---|---|---|---|---|---|---|
| 50 | 98 | 4,1 ms | 59 ms | 65 ms | 103 ms | 0% |
| 100 | 197 | 4,6 ms | 60 ms | 72 ms | 124 ms | 0% |
| 200 | 389 | 8,4 ms | 77 ms | 106 ms | 292 ms | 0% |
| 300 | 569 | 15,9 ms | 91 ms | 147 ms | 399 ms | 0% |
| **400** | **602** | 130 ms | **462 ms** | 750 ms | 1,7 s | 0% |
| 600 | 561 | 517 ms | 1.042 ms | 1.331 ms | 2,5 s | 0% |

**Limite identificado:**
- Até **300 usuários** a vazão cresce linearmente com a carga e o p95 fica abaixo de 100 ms.
- Em **400 usuários** a vazão para de crescer (**~600 req/s é o teto**) e as requisições começam a esperar na fila: o p95 pula para 462 ms, ainda dentro da meta.
- Em **600 usuários** a vazão cai e o p95 passa de 1 s. A API **não derruba requisições**: segue respondendo tudo, só mais devagar.

**Uso de recursos no pico** (amostrado a cada 4 s): a API usou até 8,1 núcleos e 872 MiB de memória; o PostgreSQL usou até 4 núcleos e 134 MiB. Nenhum dos dois esgotou a máquina (16 núcleos). O teto de vazão vem antes da CPU acabar, o que indica fila no pool de conexões do banco: o Hikari usa o padrão de 10 conexões, e o Tomcat atende até 200 threads. Ver recomendações.

### Erros observados antes da correção

Na primeira rodada, sobre `developer` sem o PR #226 e com o k6 rodando fora do Docker:

- **500 em logins simultâneos da mesma conta:** 4 na carga nominal (0,04%) e 141 no stress. Todos eram `ObjectOptimisticLockingFailureException` no delete do refresh token. Corrigido no **PR #226** (delete em massa); com a correção, zero.
- **`dial: connection refused` acima de ~400 usuários:** recusas de conexão TCP antes de a requisição chegar à API. Rodando o mesmo degrau com o k6 **dentro da rede do Docker**, elas somem por completo. A causa é o port-forward do Docker Desktop no Windows, não a aplicação; por isso as rodadas finais rodaram de dentro da rede.

## 3. Concorrência — aceites simultâneos sobre o mesmo estoque

Uma oferta de **100 kg** recebe uma fila de **60 pedidos de 5 kg** (300 kg pedidos), e os 60 aceites são disparados **no mesmo instante**. Repetido 3 vezes:

| Verificação | Rodada 1 | Rodada 2 | Rodada 3 |
|---|---|---|---|
| Aceites sem erro de servidor (só 200, 409 ou 422) | ✅ | ✅ | ✅ |
| Estoque final | 0 kg | 0 kg | 0 kg |
| Pedidos confirmados (máximo que cabe: 20) | 20 | 20 | 20 |
| Pedidos que ficaram pendentes | 0 | 0 | 0 |
| Baixa = 100 − 5 × confirmados | ✅ | ✅ | ✅ |

Os 40 pedidos que não couberam foram recusados automaticamente (`CANCELADO`). Não houve deadlock, erro de conexão nem estoque negativo: o lock pessimista nas linhas de estoque, sempre na mesma ordem, cumpre o que promete.

## Recomendações

1. **Mesclar o PR #226** (login simultâneo). É o único erro real encontrado sob carga.
2. **Pool de conexões:** o teto de ~600 req/s aparece com a CPU ainda sobrando, o que é consistente com o pool padrão do Hikari (10 conexões). Se um dia for preciso mais vazão, vale testar `spring.datasource.hikari.maximum-pool-size` entre 20 e 30 e repetir o degrau de 400 usuários. Para o uso previsto do sistema, a capacidade atual sobra.
3. **`GET /pedidos` filtra em memória** (`PedidoService.listarTodos`). Hoje é rápido (p95 de 9 ms), mas o tempo cresce com o número de pedidos de cada produtor. Se algum produtor passar de milhares de pedidos, vale mover o filtro e a paginação para a consulta.
4. **Rate limit em produção:** os limites por IP (login 30, cadastro 20, pedido público 10 a cada 10 min) seguram o abuso, mas contam por IP. Atrás de um NAT de escola ou de operadora, vários usuários reais podem dividir o mesmo IP. Os valores podem ser ajustados por variável de ambiente sem novo deploy.
5. **Repetir o teste no servidor de produção ou de homologação** antes de eventos com muito acesso. O script aceita `BASE_URL` e roda igual.
