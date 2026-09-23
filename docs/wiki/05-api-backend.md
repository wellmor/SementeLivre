# 05. API Backend

**Stack:** Java 21 + Spring Boot 4.x · Spring Data JPA · Spring Security + JWT  
**URL base (dev):** `http://localhost:8080`

---

## 5.1 Autenticação (`/auth`)

| Método | Endpoint | Descrição | Autenticação |
|---|---|---|---|
| `POST` | `/auth/cadastrar` | Cadastrar novo usuário | Não |
| `POST` | `/auth/login` | Login (retorna access + refresh token) | Não |
| `POST` | `/auth/refresh` | Renovar access token | Refresh token |
| `POST` | `/auth/recuperar-senha` | Solicitar link de redefinição de senha | Não |
| `POST` | `/auth/redefinir-senha` | Definir nova senha | Token de recuperação |
| `GET` | `/auth/me` | Retornar dados do usuário logado | Sim (JWT) |

---

## 5.2 Proprietários (`/api/proprietarios`)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/proprietarios` | Criar proprietário (Pessoa + Proprietário) |
| `GET` | `/api/proprietarios` | Listar todos |
| `GET` | `/api/proprietarios/{id}` | Buscar por ID |
| `PUT` | `/api/proprietarios/{id}` | Atualizar |
| `DELETE` | `/api/proprietarios/{id}` | Excluir (cascata) |

---

## 5.3 Usuários (`/api/usuarios`)

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/usuarios` | Criar usuário |
| `GET` | `/api/usuarios` | Listar todos |
| `GET` | `/api/usuarios/{id}` | Buscar por ID |
| `PUT` | `/api/usuarios/{id}` | Atualizar |
| `DELETE` | `/api/usuarios/{id}` | Excluir |

---

## 5.4 Pessoas (`/api/pessoas`)

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/pessoas` | Listar todas |
| `GET` | `/api/pessoas/{id}` | Buscar por ID |
| `PUT` | `/api/pessoas/{id}` | Atualizar dados |
| `DELETE` | `/api/pessoas/{id}` | Excluir |

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
| `LoginRequestDTO` / `TokenResponseDTO` | Autenticação |
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

Todos os erros retornam `ErrorResponse` padronizado via `GlobalExceptionHandler`:

```json
{
  "erro": "Mensagem amigável ao usuário",
  "detalhes": "Detalhes técnicos (apenas em dev)"
}
```

---

*[Voltar à Wiki](README.md) · [04. Modelo de Dados](04-modelo-de-dados.md) · [06. Frontend — PWA](06-frontend-pwa.md)*