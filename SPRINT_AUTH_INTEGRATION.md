# Sprint — Integração de Autenticação e Perfil (Front-App ↔ Backend REST)

> Issues: #90 (auth no front-app) e #99 (pessoa/proprietário no front-app)
> Escopo: `front-app` consumindo o backend Spring Boot para cadastro, login, sessão, recuperação de senha, perfil e troca de senha.

---

## Objetivo

O front-app (PWA Next.js) deixa a autenticação mock em `localStorage` e passa a usar o backend REST para:

- cadastro de proprietário, login, refresh de token e recuperação de senha (#90);
- perfil do proprietário (ver e editar dados pessoais e endereço) e troca de senha (#99).

---

## Configuração

`front-app/.env.local` (não commitado):

```
NEXT_PUBLIC_API_URL=http://localhost:8080
BACKEND_URL=http://localhost:8080
```

- `NEXT_PUBLIC_API_URL`: usado pelo navegador para chamar o backend direto (auth e perfil). Sem ele, o padrão é `http://localhost:8080`.
- `BACKEND_URL`: usado só pelos route handlers do catálogo (`/api/catalogo`).
- O backend precisa liberar a origem do front no CORS (`app.cors.allowed-origins`; em dev já libera `http://localhost:3000`).

---

## Arquivos do front

| Arquivo | Papel |
|---|---|
| `src/lib/api.ts` | Cliente HTTP: tokens no `localStorage`, `Authorization: Bearer`, refresh silencioso no 401, erros tipados (`ApiError` com `fieldErrors`), toasts de "sem conexão" e "sessão expirada". Exporta `apiGet/apiPost/apiPut/apiDelete`, `renovarToken`, `isApiError`. |
| `src/lib/authApi.ts` | Funções por endpoint: `loginApi`, `cadastrarApi`, `meApi`, `recuperarSenhaApi`, `redefinirSenhaApi`, `atualizarProprietarioApi`, `alterarSenhaApi`, `logoutApi`. |
| `src/context/AuthContext.tsx` | Sessão: revalida com `GET /auth/me` na montagem; expõe `user` (uid, email, nome), `perfil` (resposta completa do `/auth/me`), `loading`, `semConexao`, `recarregarPerfil`, `logout`. Logout após 30 min sem interação. |
| `src/app/(app)/layout.tsx` | Guard: sem sessão, manda para `/entrar`. Sem conexão com sessão salva, mostra "tentar novamente" em vez de deslogar. |
| `src/types/user.ts` | Tipos alinhados com os DTOs: `Logradouro`, `Pessoa`, `Proprietario`, `PerfilUsuario` (`/auth/me`), `ProprietarioCadastroRequest`, `PessoaUpdateRequest`. |
| `src/lib/validators.ts` | Schemas Zod que espelham as regras do backend, usados no cadastro, no perfil e nas telas de senha. |
| `src/lib/forms.ts` | `aplicarErrosDaApi` (fieldErrors da API → `setError` do react-hook-form) e `comMascara` (máscara em campo registrado). |
| `src/lib/toast.ts` + `components/feedback/Toast.tsx` | `emitirToast()` para disparar toast fora do React; o `ToastProvider` fica no layout raiz, então as telas de login/cadastro também têm toast. |

Telas: `(auth)/entrar`, `(auth)/cadastrar`, `(auth)/recuperar-senha`, `(app)/perfil`.

---

## Contrato — padrões gerais

### Autenticação

- Rotas autenticadas recebem `Authorization: Bearer {accessToken}`.
- `accessToken` (JWT) vale **30 min**; `refreshToken` (UUID opaco) vale **7 dias** e é trocado a cada refresh (o anterior deixa de valer).
- O `subject` do JWT é o **e-mail**. Se o e-mail da conta mudar, o access token atual deixa de valer; o refresh token continua valendo e gera um token novo com o e-mail novo.

### Status de erro

| Status | Quando | Corpo |
|---|---|---|
| `400` | Validação do DTO, documento inválido, senha atual incorreta, token de refresh/recuperação inválido | `ErrorResponse` |
| `401` | Sem token, token inválido/expirado, **ou** credencial errada no `/auth/login` | vazio |
| `403` | Autenticado, mas sem permissão (ex.: editar dados de outro proprietário) | JSON padrão do Spring |
| `404` | Recurso não encontrado | `ErrorResponse` |
| `409` | E-mail, documento ou RG já cadastrado | `ErrorResponse` com `fieldErrors` |

### `ErrorResponse`

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

`fieldErrors` é uma lista de `"campo: mensagem"`, com o nome do campo do DTO (`email`, `documento`, `rg`, `endereco.uf`, `senhaAtual`, `novaSenha`...). Vem nos 400 de validação, nos 409 de duplicidade e no 400 de senha atual incorreta; é `null` nos demais. O front separa no primeiro `": "` e aplica no campo do formulário.

---

## Endpoints

### `POST /auth/cadastrar` — público

Cria pessoa + proprietário + conta de login (`ROLE_PROPRIETARIO`, ativa). Não faz login: o front redireciona para `/entrar`.

**Request:**
```json
{
  "nome": "Maria da Silva",
  "tipoDocumento": "CPF",
  "documento": "52998224725",
  "rg": "MG-12.345.678",
  "telefone": "32999998888",
  "email": "maria@example.com",
  "senha": "Senha1234",
  "exibirNoSitePublico": false,
  "endereco": {
    "logradouro": "Rua A", "numero": "10", "complemento": null, "bairro": "Centro",
    "municipio": "Rio Pomba", "uf": "MG", "cep": "36180000"
  }
}
```

Regras: `nome`, `tipoDocumento`, `documento`, `rg`, `email`, `senha` e `endereco` obrigatórios; `documento` só dígitos, CPF com 11 dígitos e dígito verificador válido; `rg` até 20; `telefone` até 15; `endereco.logradouro` e `endereco.municipio` obrigatórios; `endereco.uf` = 2 letras maiúsculas. `exibirNoSitePublico` é opcional (padrão `false`).

**Response 201:** mesmo formato do `GET /auth/me`.
**Erros:** `400` (validação, `fieldErrors`), `409` (`email`, `documento` ou `rg` em `fieldErrors`).

---

### `POST /auth/login` — público

**Request:** `{ "email": "maria@example.com", "senha": "Senha1234" }`

**Response 200:**
```json
{ "accessToken": "eyJ...", "refreshToken": "458afefa-...", "tokenType": "Bearer", "expiresInSeconds": 1800 }
```

**Erros:** `401` sem corpo (e-mail ou senha incorretos, ou conta inativa), `400` (validação).
O front-app só aceita conta de proprietário: se o `/auth/me` vier com `tipoPessoa` diferente de `PROPRIETARIO`, os tokens são descartados.

---

### `POST /auth/refresh` — público

**Request:** `{ "refreshToken": "458afefa-..." }`
**Response 200:** igual ao login (novo par de tokens).
**Erros:** `400` (refresh token inexistente, expirado ou revogado).

---

### `GET /auth/me` — autenticado

Perfil completo da conta logada.

**Response 200:**
```json
{
  "id": "ed5c82e4-...",
  "tipoDocumento": "CPF",
  "documento": "52998224725",
  "nome": "Maria da Silva",
  "telefone": "32999998888",
  "email": "maria@example.com",
  "endereco": {
    "logradouro": "Rua A", "numero": "10", "complemento": null, "bairro": "Centro",
    "municipio": "Rio Pomba", "uf": "MG", "cep": "36180000"
  },
  "dataCadastro": "2026-09-26T18:59:15.840857",
  "dataUltimaAlteracao": "2026-09-26T18:59:15.840857",
  "tipoPessoa": "PROPRIETARIO",
  "roles": ["ROLE_PROPRIETARIO"],
  "rg": "MG-12.345.678",
  "exibirNoSitePublico": false
}
```

`rg` e `exibirNoSitePublico` vêm `null` para admin (`tipoPessoa: "ADMIN"`).
**Erros:** `401`.

---

### `PUT /api/proprietarios/{id}` — autenticado (o próprio proprietário ou admin)

Atualiza dados pessoais e endereço. CPF e RG não são editáveis por aqui.

**Request:**
```json
{
  "nome": "Maria da Silva",
  "telefone": "32988887777",
  "email": "maria@example.com",
  "endereco": { "logradouro": "Rua B", "numero": "20", "municipio": "Rio Pomba", "uf": "MG", "cep": "36180000" }
}
```

**Response 200:** `ProprietarioResponseDTO` (mesmos dados do perfil, sem `roles`).
**Erros:** `400` (validação), `403` (id de outra pessoa), `404`, `409` (`email` em `fieldErrors`).
Se o e-mail mudar, o front chama `renovarToken()` (refresh) antes de recarregar o `/auth/me`; se o refresh falhar, faz logout e manda para o login com aviso.

---

### `POST /auth/alterar-senha` — autenticado

Troca a senha da conta logada conferindo a senha atual.

**Request:** `{ "senhaAtual": "Senha1234", "novaSenha": "NovaSenha9" }`
**Response 200:** `{ "mensagem": "Senha alterada com sucesso." }`
**Erros:**
- `400` com `fieldErrors: ["senhaAtual: Senha atual incorreta."]`;
- `400` com `fieldErrors: ["novaSenha: A nova senha deve ter entre 8 e 72 caracteres"]`;
- `401` (sem token).

---

### `POST /auth/recuperar-senha` — público

**Request:** `{ "email": "maria@example.com" }`
**Response 200:** `{ "mensagem": "Se o e-mail estiver cadastrado, as instruções foram enviadas." }`

Sempre 200, para não revelar se o e-mail existe. Se existir, o backend manda por e-mail um **código** (token) válido por 15 minutos. Em dev, sem SMTP configurado, o envio falha só no log e o código fica em `token_recuperacao_senha_t`.

### `POST /auth/redefinir-senha` — público

**Request:** `{ "token": "código-do-e-mail", "novaSenha": "NovaSenha9" }`
**Response 200:** `{ "mensagem": "Senha redefinida com sucesso." }`
**Erros:** `400` (código inválido, expirado ou já usado; `novaSenha` com menos de 6 caracteres).

No front, a tela `/recuperar-senha` tem as duas etapas: pedir o código e, com ele, definir a nova senha.

---

## Fluxo de sessão no front

```
[Montagem da área logada — (app)/layout + AuthContext]
  Tem sl_access_token?
    não → /entrar
    sim → GET /auth/me
            200 → user + perfil
            401 → refresh (abaixo)
            sem conexão → mantém tokens, tela "tentar novamente" + toast

[Qualquer requisição autenticada recebe 401]
  (exceto /auth/login, /auth/refresh e /auth/cadastrar, para não entrar em loop)
  POST /auth/refresh
    ok    → salva o novo par de tokens → repete a requisição
    falha → limpa tokens → evento sl:session-expired → toast "Sua sessão expirou" → /entrar

[Login]
  POST /auth/login → salva tokens → GET /auth/me (só PROPRIETARIO) → /dashboard

[Cadastro]
  POST /auth/cadastrar → toast "Conta criada" → /entrar (sem auto-login)
```

### Onde cada erro aparece

| Erro | Onde |
|---|---|
| Credencial inválida (401 no login) | Toast |
| Sessão expirada (refresh falhou) | Toast + redireciona para `/entrar` |
| Sem conexão (fetch falhou) | Toast |
| Validação e duplicidade (`fieldErrors`) | No campo do formulário (`setError`) |
| Outros erros da API | Alerta no formulário |

---

## Validações no front (espelham o backend)

| Campo | Regra |
|---|---|
| CPF | obrigatório, 11 dígitos, não todos iguais, dígito verificador (mesmo algoritmo do `DocumentoValidator`); enviado só com dígitos |
| RG | obrigatório, até 20 caracteres |
| E-mail | obrigatório, formato de e-mail, até 255 |
| Telefone | opcional; se preenchido, 10 ou 11 dígitos (enviado só com dígitos, cabe nos 15 do backend) |
| Nome | obrigatório, até 150 |
| Logradouro / Município | obrigatórios, até 255 / 100 |
| Número / Complemento / Bairro | opcionais, até 10 / 100 / 100 |
| UF | uma das 27 UFs (o backend exige 2 letras maiúsculas) |
| CEP | opcional; se preenchido, 8 dígitos (enviado só com dígitos) |
| Senha | 8 a 72 caracteres, uma maiúscula e um número (o backend exige no mínimo 8 na troca de senha e não valida força no cadastro) |

---

## Critérios de aceite

### #90 — Auth no front-app

| Critério | Status |
|---|---|
| Login válido retorna JWT e inicia a sessão | ✅ `loginApi` + `AuthContext` |
| Cadastro cria o usuário e permite login em seguida | ✅ cadastro → `/entrar` → login |
| Refresh token renova o access token sem novo login | ✅ `silentRefresh` em `api.ts` |
| Erros de credencial e sessão em toast, no padrão do app | ✅ `Toast` do app + `emitirToast` |

### #99 — Pessoa/proprietário no front-app

| Critério | Status |
|---|---|
| Cadastro cria a conta via `/auth/cadastrar` e redireciona para o login | ✅ |
| Perfil exibe os dados de `/auth/me` | ✅ |
| Editar perfil persiste no backend | ✅ `PUT /api/proprietarios/{id}` |
| CPF e e-mail duplicados mostram o erro da API no campo | ✅ `fieldErrors` → `setError` |
| Alteração de senha valida a senha atual | ✅ `POST /auth/alterar-senha` |

---

## Pendências

- `GET /api/proprietarios`, `GET /api/proprietarios/{id}` e `DELETE` continuam liberados para qualquer usuário autenticado (expõem CPF/RG de outros proprietários).
- Unicidade de e-mail é sensível a maiúsculas/minúsculas.
- O cadastro no backend não valida força de senha (só o front exige maiúscula e número); `redefinir-senha` aceita a partir de 6 caracteres.
- Sementes, pedidos e propriedades ainda usam `localStorage` (`lib/db.ts`).
