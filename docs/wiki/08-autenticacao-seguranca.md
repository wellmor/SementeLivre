# 08. Autenticação e Segurança

## 8.1 Modelo de Autenticação (Backend)

O backend utiliza **JWT (JSON Web Token)** com **Spring Security**:

1. `POST /auth/login` valida credenciais e retorna **access token** + **refresh token**.
2. O **SecurityFilter** intercepta as requisições e valida o token JWT.
3. O `JwtService` assina e valida os tokens.
4. O `UserDetailsServiceImpl` carrega o usuário autenticado.
5. o `SecurityConfig` define as regras de autorização por rota.

### Fluxo de Refresh

- O access token tem validade curta.
- Sempre que expira, o cliente chama `POST /auth/refresh` enviando o refresh token.
- Tokens revogados/rotacionados são controlados pela entidade `RefreshToken`.

## 8.2 Recuperação de Senha

1. `POST /auth/recuperar-senha` — solicita redefinição pelo e-mail cadastrado.
2. Sistema gera token de recuperação (`TokenRecuperacaoSenha`).
3. `EmailService` envia link/código ao usuário.
4. `POST /auth/redefinir-senha` — aplica a nova senha.

## 8.3 Proteção de Senhas

- Senhas armazenadas exclusivamente com hash **BCrypt** (`spring-security-crypto`).
- Nunca em texto plano; nunca no banco sem hash.

## 8.4 Perfis e Níveis de Acesso

| Entidade | Papel no sistema |
|---|---|
| `usuario_t` | Realiza pedidos no sistema |
| `proprietario_t` | Produtor rural dono de sementes/propriedades |
| `admin_t` | Administração (níveis SUPER_ADMIN, ADMIN, MODERADOR) |

## 8.5 LGPD

- Dados pessoais (CPF, e-mail, telefone) protegidos.
- Senhas criptografadas com BCrypt.
- Logs de acesso por no mínimo 6 meses.
- Direito ao esquecimento previsto (exclusão de dados).

## 8.6 Boas Práticas Aplicadas

- `RequestLoggingFilter` para auditoria de requisições (config do backend).
- Tratamento padronizado de erros com `GlobalExceptionHandler` e `ErrorResponse`.
- Validação de entradas com `spring-boot-starter-validation` (Bean Validation).
- Não exposição de entidades — comunicação via DTOs.

---

*[Voltar à Wiki](README.md) · [07. Frontend — Site Público](07-frontend-site.md) · [09. Guia de Desenvolvimento](09-guia-desenvolvimento.md)*