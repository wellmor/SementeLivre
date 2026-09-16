# Protótipo de Autenticação Spring Security + JWT

## Visão Geral

Protótipo de autenticação com Spring Boot, Spring Security e JWT (JSON Web Token).  
Banco de dados H2 em memória — os dados são perdidos ao reiniciar a aplicação.

---

## Endpoints

| Método | Rota         | Acesso     | Descrição                            |
|--------|--------------|------------|--------------------------------------|
| POST   | `/cadastrar` | Público    | Cria usuário com senha em BCrypt     |
| POST   | `/login`     | Público    | Autentica e retorna JWT              |
| GET    | `/me`        | Protegido  | Retorna dados do usuário autenticado |

---

## Estrutura do JWT

O token JWT possui três partes separadas por ponto (`.`):

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsInJvbGUiOiJQUk9QUklFVEFSSU8iLCJpYXQiOjE3MjAwMDAwMDAsImV4cCI6MTcyMDAwMzYwMH0.ASSINATURA
   HEADER                          PAYLOAD                                                                                                      SIGNATURE
```

### Header
```json
{
  "alg": "HS256"
}
```
Indica o algoritmo de assinatura (HMAC SHA-256).

### Payload (Claims)
```json
{
  "sub": "user@email.com",
  "role": "PROPRIETARIO",
  "iat": 1720000000,
  "exp": 1720003600
}
```

| Campo | Significado                               |
|-------|-------------------------------------------|
| `sub` | Subject — email do usuário                |
| `role`| Papel/permissão do usuário               |
| `iat` | Issued At — timestamp de emissão          |
| `exp` | Expiration — timestamp de expiração       |

### Signature
Gerada com HMAC-SHA256 usando a chave secreta definida em `application.properties`.  
Garante que o token não foi adulterado.

---

## Roles Disponíveis

| Role          | Descrição          |
|---------------|--------------------|
| `PROPRIETARIO`| Proprietário       |
| `ADMIN`       | Administrador      |

---

## Como Executar

```bash
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

---

## Como Testar

### 1. Cadastrar usuário

```bash
curl -X POST http://localhost:8080/cadastrar \
  -H "Content-Type: application/json" \
  -d '{"email":"user@email.com","senha":"123456","role":"PROPRIETARIO"}'
```

**Resposta esperada (201):**
```json
{"mensagem": "Usuário cadastrado com sucesso"}
```

---

### 2. Fazer login e obter token

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@email.com","senha":"123456"}'
```

**Resposta esperada (200):**
```json
{"token": "eyJhbGciOi..."}
```

---

### 3. Acessar rota protegida com token

```bash
curl -X GET http://localhost:8080/me \
  -H "Authorization: Bearer eyJhbGciOi..."
```

**Resposta esperada (200):**
```json
{"email": "user@email.com", "role": "PROPRIETARIO"}
```

---

### 4. Acessar rota protegida sem token

```bash
curl -X GET http://localhost:8080/me
```

**Resposta esperada (401):** acesso negado.

---

## Console H2 (Banco de Dados)

Acesse `http://localhost:8080/h2-console` com:
- **JDBC URL:** `jdbc:h2:mem:authdb`
- **User:** `sa`
- **Password:** *(vazio)*

---

## Configurações (application.properties)

| Propriedade       | Valor padrão                        | Descrição                    |
|-------------------|-------------------------------------|------------------------------|
| `jwt.secret`      | chave de 256 bits                   | Chave de assinatura do JWT   |
| `jwt.expiration`  | `3600000` (1 hora em milissegundos) | Tempo de expiração do token  |
