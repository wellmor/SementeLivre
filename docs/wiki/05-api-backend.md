# 05. API Backend

**Stack:** Java 21 + Spring Boot 4.x · Spring Data JPA · Spring Security + JWT  
**URL base (dev):** `http://localhost:8080`

---

## 5.1 Autenticação (`/auth`)

| Método | Endpoint | Descrição | Autenticação |
|---|---|---|---|
| `POST` | `/auth/cadastrar` | Cadastro de proprietário (cria Pessoa + Proprietário + conta de login) | Não |
| `POST` | `/auth/login` | Login (retorna access + refresh token; `401` se a credencial estiver errada) | Não |
| `POST` | `/auth/refresh` | Renovar access token (refresh token no corpo) | Não |
| `POST` | `/auth/recuperar-senha` | Enviar por e-mail um código de redefinição (responde `200` mesmo se o e-mail não existir) | Não |
| `POST` | `/auth/redefinir-senha` | Definir nova senha com o código recebido por e-mail | Não |
| `GET` | `/auth/me` | Perfil completo do usuário logado (pessoa, endereço, `rg`, `exibirNoSitePublico`, `roles`) | Sim (JWT) |
| `POST` | `/auth/alterar-senha` | Trocar a senha conferindo a senha atual | Sim (JWT) |

Payloads, respostas e status de cada endpoint: [`SPRINT_AUTH_INTEGRATION.md`](../../SPRINT_AUTH_INTEGRATION.md).

---

## 5.2 Proprietários (`/api/proprietarios`)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/proprietarios` | Criar proprietário (Pessoa + Proprietário + conta de login) |
| `GET` | `/api/proprietarios` | Listar todos |
| `GET` | `/api/proprietarios/{id}` | Buscar por ID |
| `PUT` | `/api/proprietarios/{id}` | Atualizar (só o próprio proprietário ou admin; senão `403`) |
| `DELETE` | `/api/proprietarios/{id}` | Excluir (cascata; só admin) |

---

## 5.3 Usuários (`/api/usuarios`)

Contas de login de admins e proprietários. A conta nasce junto com o proprietário (`/auth/cadastrar` ou `POST /api/proprietarios`); nome, e-mail e endereço são editados em `/api/pessoas` ou `/api/proprietarios`.

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/usuarios` | Listar contas |
| `GET` | `/api/usuarios/{id}` | Buscar conta por ID (mesmo ID da pessoa) |
| `DELETE` | `/api/usuarios/{id}` | Excluir só a conta de login (a pessoa continua; só admin) |

---

## 5.4 Pessoas (`/api/pessoas`)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/pessoas` | Listar todas |
| `GET` | `/api/pessoas/{id}` | Buscar por ID |
| `PUT` | `/api/pessoas/{id}` | Atualizar dados (só a própria pessoa ou admin; senão `403`) |
| `DELETE` | `/api/pessoas/{id}` | Excluir (só admin) |

---

## 5.5 Produtos/Sementes (`/produtos`)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/produtos` | Criar produto |
| `GET` | `/produtos` | Listar todos |
| `GET` | `/produtos/{id}` | Buscar por ID |
| `PUT` | `/produtos/{id}` | Atualizar |
| `DELETE` | `/produtos/{id}` | Excluir |
| `POST` | `/produtos/upload-foto` | Upload de foto do produto |

---

## 5.6 Estoque (`/estoques`)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/estoques` | Criar/atualizar registro de estoque |
| `GET` | `/estoques` | Listar todos |
| `GET` | `/estoques/{id}` | Buscar por ID |
| `PUT` | `/estoques/{id}` | Atualizar |
| `DELETE` | `/estoques/{id}` | Excluir |

**Regra:** Um proprietário só pode ter 1 registro de estoque por produto. Quantidade nunca negativa.

---

## 5.7 Pedidos (`/pedidos`)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/pedidos` | Criar pedido (venda/troca/doação) |
| `GET` | `/pedidos` | Listar todos |
| `GET` | `/pedidos/{id}` | Buscar por ID |
| `PUT` | `/pedidos/{id}` | Atualizar pedido |
| `DELETE` | `/pedidos/{id}` | Excluir (restaura estoque) |
| `PATCH` | `/pedidos/{id}/confirmar` | Confirmar pedido |
| `PATCH` | `/pedidos/{id}/cancelar` | Cancelar pedido |

---

## 5.8 Notificações (`/notificacoes`)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/notificacoes` | Criar notificação manual |
| `GET` | `/notificacoes` | Listar todas |
| `GET` | `/notificacoes/{id}` | Buscar por ID |
| `PUT` | `/notificacoes/{id}` | Atualizar |
| `PATCH` | `/notificacoes/{id}/lida` | Marcar como lida |
| `DELETE` | `/notificacoes/{id}` | Excluir |

---

## 5.9 Relatórios (`/relatorios`)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/relatorios` | Gerar relatório (estoque ou pedidos) |
| `GET` | `/relatorios` | Listar todos |
| `GET` | `/relatorios/{id}` | Buscar por ID |
| `PUT` | `/relatorios/{id}` | Atualizar metadados |
| `DELETE` | `/relatorios/{id}` | Excluir |

---

## 5.10 DTOs Principais

| DTO | Módulo |
|---|---|
| `LoginRequestDTO` / `TokenResponseDTO` / `RefreshTokenRequestDTO` | Autenticação |
| `SolicitarRecuperacaoDTO` / `RedefinirSenhaDTO` / `AlterarSenhaDTO` | Senha |
| `ProprietarioCreateRequestDTO` / `ProprietarioResponseDTO` | Proprietários |
| `ProdutoRequestDTO` / `ProdutoResponseDTO` | Sementes |
| `EstoqueRequestDTO` / `EstoqueResponseDTO` | Estoque |
| `PedidoRequestDTO` / `PedidoResponseDTO` / `PedidoUpdateDTO` | Pedidos |
| `ItemPedidoRequestDTO` / `ItemPedidoResponseDTO` | Itens do pedido |
| `NotificacaoRequestDTO` / `NotificacaoResponseDTO` | Notificações |
| `RelatorioRequestDTO` / `RelatorioResponseDTO` | Relatórios |
| `UsuarioCreateRequestDTO` / `UsuarioResponseDTO` | Usuários |
| `ComunidadeDTO` / `LogradouroDTO` | Complementares |

---

## 5.11 Tratamento de Erros

Erros de validação e de negócio retornam `ErrorResponse`, montado pelo `GlobalExceptionHandler`:

```json
{
  "error": "Conflict",
  "message": "E-mail já cadastrado no sistema.",
  "timestamp": "2026-09-26T21:59:15.985Z",
  "status": 409,
  "path": "/auth/cadastrar",
  "fieldErrors": ["email: E-mail já cadastrado no sistema."]
}
```

`fieldErrors` é uma lista de `"campo: mensagem"`, com o nome do campo do DTO (`email`, `documento`, `rg`, `endereco.uf`, `senhaAtual`...). Vem preenchido nos erros de validação, de duplicidade e de senha atual incorreta; nos demais é `null`.

| Status | Quando | Corpo |
|---|---|---|
| `400` | Validação do DTO, documento inválido, senha atual incorreta, token de refresh/recuperação inválido | `ErrorResponse` |
| `401` | Sem token, token inválido/expirado, ou credencial errada no `/auth/login` | vazio |
| `403` | Autenticado, mas sem permissão (ex.: editar dados de outra pessoa, excluir sem ser admin) | JSON padrão do Spring (`timestamp`, `status`, `error`, `path`) |
| `404` | Recurso não encontrado | `ErrorResponse` |
| `409` | E-mail, documento ou RG já cadastrado | `ErrorResponse` com `fieldErrors` |

---

*[Voltar à Wiki](README.md) · [04. Modelo de Dados](04-modelo-de-dados.md) · [06. Frontend — PWA](06-frontend-pwa.md)*