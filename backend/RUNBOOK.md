# Runbook de Deploy — Backend (Semente Livre)

Este documento é o guia oficial de deploy, configuração de ambiente, monitoramento, backup e contingência do backend do projeto **Semente Livre** para o ambiente de produção.

---

## 1. Pré-requisitos do Servidor

Para rodar este ambiente em produção (servidor da universidade ou VPS), certifique-se de que a máquina possui:
- Sistema Operacional Linux (Ubuntu 22.04 LTS ou superior recomendado)
- **Docker Engine** (v24.0+) e **Docker Compose** (v2.20+) instalados
- Portas de rede liberadas no firewall:
  - `80` (HTTP)
  - `443` (HTTPS)
- Domínio ou IP do servidor apontado e acessível
- A rede Docker compartilhada `docker-net`, padrão do servidor do TI. O compose a usa como `external: true`, então ela precisa existir antes do deploy; sem ela, o `up` para com `network docker-net declared as external, but could not be found`. Se o servidor ainda não tiver a rede:
  ```bash
  docker network create docker-net
  ```

### 1.1. Onde ficam os dados no servidor

Seguindo o padrão do TI, todos os dados persistentes ficam em pastas do host, em `/docker/volumes/sementelivre/` (não há volumes nomeados do Docker). O Docker cria as pastas no primeiro `up`.

| Pasta no host | O que guarda |
|---|---|
| `/docker/volumes/sementelivre/postgres` | Dados do PostgreSQL |
| `/docker/volumes/sementelivre/uploads` | Fotos cadastradas pelos produtores |
| `/docker/volumes/sementelivre/caddy/data` e `/caddy/config` | Certificados HTTPS e configuração do Caddy |
| `/docker/volumes/sementelivre/backups` | Backups automáticos do banco (ver seção 5.1) |

O backend roda sem root (usuário `spring`), e a pasta de fotos nasce do root. O serviço `uploads-permissao` resolve isso sozinho: a cada `up`, antes do backend, ele entrega a pasta ao `spring` e termina. Por isso ele aparece como `Exited (0)` em `docker compose ps -a`, o que é normal.

Só o `caddy` entra na `docker-net`; banco, backend e backup ficam só na rede interna do projeto. O Caddy chama o backend pelo nome do container (`semente_livre_app_prod`), porque na `docker-net` o nome `app` pode ser o serviço de outro sistema do servidor.

---

## 2. Preparação das Variáveis de Ambiente

Todas as credenciais e segredos são gerenciados exclusivamente por variáveis de ambiente via arquivo `.env` (em conformidade com o 12-Factor App).

1. No servidor, navegue até a pasta do backend:
   ```bash
   cd backend
   ```

2. Crie o arquivo `.env` a partir do modelo `.env.example`:
   ```bash
   cp .env.example .env
   ```

3. Abra o arquivo `.env` para edição:
   ```bash
   nano .env
   ```

4. Preencha os valores obrigatórios de produção:
   - `DB_USER`: Usuário do PostgreSQL (ex: `semente_user`).
   - `DB_PASSWORD`: Senha forte exclusiva de produção.
   - `DB_NAME`: Nome do banco de produção (ex: `semente_prod`).
   - `JWT_SECRET`: Chave criptográfica aleatória com mais de 32 caracteres (256 bits). Para gerar uma chave forte no terminal, execute:
     ```bash
     openssl rand -base64 32
     ```
   - `JWT_EXPIRATION`: Tempo de expiração do token em milissegundos (padrão: `1800000` = 30 minutos).
   - `DOMAIN_NAME`: Domínio registrado fornecido pelo instituto (ex: `semente.riopomba.ifsudestemg.edu.br`) ou IP da máquina.
   - `CORS_ALLOWED_ORIGINS`: URLs oficiais de onde o front-site e front-app acessarão a API, separadas por vírgula.
   - `MAIL_*`: Configurações do servidor SMTP para envio de e-mails do sistema.
   - `ADMIN_INICIAL_EMAIL` e `ADMIN_INICIAL_SENHA`: primeiro acesso do admin principal (ver abaixo).

### 2.1. Admin principal (primeiro acesso)

O banco já nasce com um admin principal (`SUPER_ADMIN`, e-mail `admin@sementelivre.com.br`), mas **sem senha**: o hash que vinha no seed está no repositório público e foi invalidado (migration V14).

1. Antes do primeiro deploy (ou do deploy desta versão), preencha no `.env`:
   - `ADMIN_INICIAL_SENHA`: senha forte, de 8 a 72 caracteres.
   - `ADMIN_INICIAL_EMAIL` (opcional): um e-mail que a equipe lê. Ele passa a ser o login do admin e recebe o link do "esqueci a senha". Vazio mantém `admin@sementelivre.com.br`.
2. Suba a aplicação. No log aparece `Senha do admin principal definida a partir de ADMIN_INICIAL_SENHA. Login: <e-mail>`.
3. Entre no front-app com esse e-mail e senha. Depois disso as duas variáveis são ignoradas; pode apagá-las do `.env`. Trocar a senha pelo app não é desfeito em deploys seguintes.

Se o backend subir sem `ADMIN_INICIAL_SENHA` enquanto o admin estiver sem senha, ele avisa no log e ninguém consegue entrar como admin até a variável ser definida e o backend reiniciado.

> **Atenção:** O arquivo `.env` jamais deve ser commitado no Git. Ele está explicitamente ignorado no `.gitignore`.

---

## 3. Inicialização e Deploy da Aplicação

1. Certifique-se de dar permissão de execução aos scripts utilitários:
   ```bash
   chmod +x scripts/backup.sh scripts/restore.sh
   ```

2. Construa a imagem multi-stage otimizada e inicialize os containers em segundo plano:
   ```bash
   docker compose -f docker-compose.prod.yml up --build -d
   ```

3. Verifique o status de execução de todos os serviços:
   ```bash
   docker compose -f docker-compose.prod.yml ps
   ```
   **Serviços esperados no estado `healthy` ou `running`:**
   - `semente_livre_db_prod` (PostgreSQL 15 Alpine)
   - `semente_livre_app_prod` (Spring Boot API)
   - `semente_livre_caddy_prod` (Proxy Reverso com SSL/HTTPS automático)
   - `semente_livre_backup_prod` (Serviço de backup automático diário)

---

## 4. Validação e Health Checks

### 4.1. Verificação de Saúde da API (Liveness / Readiness)
Execute o comando curl no servidor ou abra pelo navegador:
```bash
curl -i http://localhost:8080/actuator/health
```
Ou através do domínio com HTTPS:
```bash
curl -i https://<SEU_DOMINIO>/actuator/health
```
**Resposta esperada (HTTP 200 OK):**
```json
{"status":"UP"}
```

### 4.2. Acesso aos Logs em Tempo Real
Os logs da aplicação em produção são formatados como JSON estruturado via Logstash para facilitar a análise:
```bash
docker compose -f docker-compose.prod.yml logs -f app
```
Para ver os logs do banco ou do proxy:
```bash
docker compose -f docker-compose.prod.yml logs -f db
docker compose -f docker-compose.prod.yml logs -f caddy
```

---

## 5. Rotina de Backup e Restauração de Dados

### 5.1. Backup Automático
O container `semente_livre_backup_prod` executa automaticamente um `pg_dump` todos os dias à meia-noite (`@daily`).
- Os arquivos compactados são salvos em `/docker/volumes/sementelivre/backups` (subpastas `daily`, `weekly` e `monthly`; o mais recente de cada uma é o `*-latest.sql.gz`).
- A política de retenção mantém os backups dos últimos **7 dias**, removendo arquivos antigos automaticamente.

### 5.2. Backup Manual / Sob Demanda
Antes de realizar qualquer manutenção crítica ou atualização, gere um backup imediato:
```bash
./scripts/backup.sh
```

### 5.3. Procedimento de Restauração (Disaster Recovery)
Caso seja necessário restaurar os dados a partir de um backup existente:
```bash
./scripts/restore.sh ./backups/backup_semente_prod_<DATA_HORA>.sql.gz
```
O script solicitará confirmação antes de aplicar a restauração.

---

## 6. Atualização de Versão (Redeploy)

Quando houver novas alterações mescladas na branch principal:

1. Atualize o código no servidor:
   ```bash
   git pull origin main
   ```

2. Reconstrua e reinicie apenas o container da aplicação sem derrubar o banco de dados:
   ```bash
   docker compose -f docker-compose.prod.yml build app
   docker compose -f docker-compose.prod.yml up -d --no-deps app
   ```

3. Acompanhe os logs para validar se o Spring Boot subiu com sucesso e executou as migrações do Flyway:
   ```bash
   docker compose -f docker-compose.prod.yml logs -f app
   ```

---

## 7. Procedimento de Rollback

Em caso de falha crítica na inicialização de uma nova versão:

1. Retorne o código para a tag ou commit estável anterior:
   ```bash
   git checkout <HASH_OU_TAG_ESTAVEL>
   ```

2. Reconstrua a imagem estável:
   ```bash
   docker compose -f docker-compose.prod.yml up --build -d --no-deps app
   ```

3. Se a falha tiver corrompido o banco ou aplicado migrações incompatíveis, restaure o último backup íntegro:
   ```bash
   ./scripts/restore.sh ./backups/<ULTIMO_BACKUP_VALIDO>.sql.gz
   ```
   