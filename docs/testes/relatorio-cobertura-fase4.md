# Relatório de cobertura de código — Fase 4 (#130)

**Ferramenta:** JaCoCo 0.8.13 (`jacoco-maven-plugin`)
**Base medida:** branch `developer` @ `f0cbad8`, suíte completa do backend (`./mvnw verify`)
**Data:** 04/11/2026
**Meta (Fase 0):** 70% de cobertura de linhas, com o build falhando abaixo disso

## Resultado

| Contador | Cobertos / total | Cobertura |
|---|---|---|
| **Linhas** | 2769 / 2938 | **94,2%** ✅ |
| Instruções | — | 94,3% |
| Branches | 595 / 732 | 81,3% |
| Métodos | — | 93,4% |

**Testes executados:** 391, com 0 falhas, 0 erros e 0 ignorados (unitários e integração com PostgreSQL via Testcontainers).

A meta de 70% está cumprida com folga de 24 pontos percentuais.

## Por pacote

| Pacote | Linhas | Branches |
|---|---|---|
| `service` (regras de negócio) | 95,9% | 82,1% |
| `security` (JWT, filtro, hash de tokens) | 98,1% | 83,3% |
| `controller` | 82,5% | 66,7% |
| `repository` | 100% | — |
| `exception` | 91,7% | 78,6% |
| `util` (PDF, CSV, validação de documento) | 93,3% | 83,3% |
| `config` | 95,2% | 66,7% |
| `entity` | 86,3% | 62,5% |
| `entity.enums` | 100% | 100% |
| `dto` | 99,3% | 75,0% |
| raiz (`BackendApplication.main`) | 33,3% | — |

`entity` e `dto` incluem getters e setters gerados pelo Lombok, o que puxa esses pacotes para cima. Mesmo descontando essas camadas, as camadas com lógica (`service`, `security`, `controller`) ficam todas acima da meta. A única linha não coberta na raiz é o `main` da aplicação, que os testes não chamam porque sobem o contexto pelo `@SpringBootTest`.

## Pontos com menor cobertura de branches

- **`controller` (66,7%):** os desvios menos exercitados são os de autorização por papel dentro do próprio controller, como a listagem de estoque que separa admin de proprietário. Os casos negativos de autorização estão cobertos pelos testes de integração de autorização e pelo DAST.
- **`entity` (62,5%):** `equals`/`hashCode` e métodos utilitários das entidades.

## Como é aplicado

- `./mvnw test` gera o relatório HTML em `backend/target/site/jacoco/index.html`.
- `./mvnw verify` roda o `jacoco:check`, e o build **falha** se a cobertura de linhas ficar abaixo de 70%. O CI (`.github/workflows/ci.yml`) já roda `verify`, então um PR que derrube a cobertura abaixo da meta fica vermelho.
- O CI publica o relatório HTML como artefato `relatorio-cobertura` em todas as execuções.
