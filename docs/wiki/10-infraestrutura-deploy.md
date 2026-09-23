# 10. Infraestrutura e Deploy

## 10.1 Ambiente de Desenvolvimento

| Componente | Tecnologia | Detalhes |
|---|---|---|
| Banco de dados | PostgreSQL 15 (Docker) | Container `semente_livre_db`, porta 5433 |
| Docker Compose | `backend/docker-compose.yml` | Sube apenas o PostgreSQL |

### Comandos

```bash
cd backend
docker compose up -d      # Subir banco dev
docker compose down       # Parar e remover
```

### Estrutura de volumes

- Volume persistente `postgres_data` evita perda de dados ao reiniciar o container.

---

## 10.2 Docker Compose (dev)

```yaml
version: '3.8'
services:
  db:
    image: postgres:15
    container_name: semente_livre_db
    environment:
      POSTGRES_USER: devuser
      POSTGRES_PASSWORD: devpassword
      POSTGRES_DB: devdb
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
volumes:
  postgres_data:
```

---

## 10.3 Perfiles de Ambiente (Backend)

| Profile | Arquivo | Banco | Logs |
|---|---|---|---|
| `dev` (padrão) | `application-dev.yml` | `jdbc:postgresql://localhost:5433/devdb` | DEBUG/INFO |
| `prod` | `application-prod.yml` | `${DB_URL}` (variável de ambiente) | WARN/INFO |

---

## 10.4 CI/CD

### GitHub Actions

| Workflow | Trigger | O que faz |
|---|---|---|
| `CI - Backend` (`.github/workflows/ci.yml`) | Push/PR para `main` ou `developer` | Build e testes do backend com Maven (JDK 17 no CI; JDK 21 local) |
| Artifacts | Sempre | Publica `surefire-reports` como artifact do GitHub |

> O CI hoje cobre apenas o diretório `estudo-spring-docker/demo/`. Extensão para o `backend/` principal está em andamento.

---

## 10.5 Supabase (Produção)

| Recurso | Serviço |
|---|---|
| PostgreSQL gerenciado | Supabase (produção e staging) |
| Storage | Supabase Storage (fotos de produtos) |
| Backups | Automáticos diários (configurável no painel Supabase) |

---

## 10.6 Variáveis de Ambiente Relevantes

| Variável | Uso (prod) | Uso (dev) |
|---|---|---|
| `DB_URL` | URL de conexão do PostgreSQL em produção | `jdbc:postgresql://localhost:5433/devdb` |
| `DB_USERNAME` | Usuário do banco prod | `devuser` |
| `DB_PASSWORD` | Senha do banco prod | `devpassword` |
| `SPRING_PROFILES_ACTIVE` | `prod` | `dev` (padrão) |

---

## 10.7 Backups e Recomendações

- Supabase: backups automáticos diários.
- Recomendação para ambientes manuais: `pg_dump` semanal completo + contínuo via WAL.
- Em produção: `spring.profiles.active=prod` com variáveis de ambiente seguras (nunca commitar `.env`).

---

*[Voltar à Wiki](README.md) · [09. Guia de Desenvolvimento](09-guia-desenvolvimento.md) · [11. Gestão e Qualidade](11-gestao-qualidade.md)*