# Sprint — Integração de Autenticação (Front-App ↔ Backend REST)

> Data de execução: 2026-09-26  
> Escopo: `front-app` — integração das telas de auth com os endpoints REST do backend Spring Boot

---

## Objetivo

Garantir que o front-app (PWA Next.js) consuma o backend REST para login, cadastro, refresh de token e recuperação de senha — substituindo a autenticação mock local baseada em `localStorage`.

---

## Contexto — Estado anterior

O front-app possuía uma camada de autenticação **totalmente local** em `src/lib/auth.ts`:

- Usuários armazenados em `localStorage` como JSON.
- Senha "hasheada" com `btoa(encodeURIComponent(password))` (inseguro).
- Login, cadastro e logout operavam apenas no browser, sem servidor.
- `AuthContext` carregava dados do "proprietário" via `dbGet()` do `lib/db.ts` (também localStorage).

---

## Arquivos Criados (novos — sem remoção)

### 1. `front-app/.env.local`
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```
Variável de ambiente para a URL base do backend. Não commitada no git.

---

### 2. `front-app/src/lib/api.ts` *(novo)*

**Cliente HTTP centralizado** para todas as requisições ao backend.

| Responsabilidade | Detalhe |
|---|---|
| Gerenciamento de tokens | Salva/lê `sl_access_token` e `sl_refresh_token` do localStorage |
| Refresh automático | Intercepta resposta `401` e tenta `POST /auth/refresh` silenciosamente |
| Evento de sessão expirada | Dispara `window.dispatchEvent(new Event('sl:session-expired'))` quando refresh falha |
| Helpers tipados | Exporta `apiGet`, `apiPost`, `apiPut`, `apiDelete` |

**Exportações:**
```typescript
getAccessToken(): string | null
getRefreshToken(): string | null
saveTokens(accessToken, refreshToken): void
clearTokens(): void
apiGet<T>(path, options?): Promise<T>
apiPost<T>(path, body?, options?): Promise<T>
apiPut<T>(path, body?, options?): Promise<T>
apiDelete<T>(path, options?): Promise<T>
```

---

### 3. `front-app/src/lib/authApi.ts` *(novo)*

**Camada de domínio de autenticação** — funções que chamam os endpoints do backend.

| Função | Endpoint | Descrição |
|---|---|---|
| `loginApi(email, senha)` | `POST /auth/login` + `GET /auth/me` | Faz login, salva tokens, retorna `AuthSession` |
| `cadastrarApi(payload)` | `POST /auth/cadastrar` | Cria usuário no backend |
| `recuperarSenhaApi(email)` | `POST /auth/recuperar-senha` | Solicita link de recuperação |
| `logoutApi()` | — (apenas limpa tokens locais) | Limpa `localStorage` |

**Tipo `AuthSession`:**
```typescript
interface AuthSession {
  uid: string;   // UUID do backend
  email: string;
  nome: string;
}
```

---

## Arquivos Modificados

### 4. `front-app/src/context/AuthContext.tsx`

**Antes (versão mock local):**
```typescript
// REMOVIDAS:
import { onAuthStateChanged, signOutAndNotify, Session } from '@/lib/auth';
import { dbGet } from '@/lib/db';
import { Proprietario } from '@/types/user';

// Dentro do Provider:
const unsubscribe = onAuthStateChanged(async (session) => {
  setUser(session);
  if (session) {
    const prop = dbGet<Proprietario & { id: string }>('proprietarios', session.uid);
    if (prop) {
      setProprietario({ ...prop, idProprietario: prop.id }); // ← REMOVIDA
    }
    // ...
  }
});

// Contexto expunha:
interface AuthContextType {
  user: Session | null;
  proprietario: Proprietario | null;  // ← REMOVIDO
  loading: boolean;
  logout: () => Promise<void>;
}
```

**Depois (integrado ao backend):**
```typescript
// ADICIONADAS:
import { AuthSession, logoutApi, UsuarioResponse } from '@/lib/authApi';
import { apiGet, getAccessToken } from '@/lib/api';

// Inicialização via GET /auth/me (revalida token existente):
const me = await apiGet<UsuarioResponse>('/auth/me');
setUser({ uid: me.id, email: me.email, nome: me.nome });

// Contexto expõe:
interface AuthContextType {
  user: AuthSession | null;  // uid + email + nome
  loading: boolean;
  logout: () => Promise<void>;
}
```

> **Atenção:** O campo `proprietario: Proprietario | null` foi **removido** do contexto.
> As telas que o usavam foram adaptadas conforme descrito abaixo.

---

### 5. `front-app/src/app/(auth)/entrar/page.tsx`

**Linha removida:**
```typescript
- import { signInAndNotify } from '@/lib/auth';
- signInAndNotify(email, senha);
```

**Substituída por:**
```typescript
+ import { loginApi } from '@/lib/authApi';
+ import { ApiError } from '@/lib/api';
+ await loginApi(email, senha);
```

**Mapeamento de erros HTTP:**
| Status HTTP | Mensagem exibida |
|---|---|
| `401` / `403` | "E-mail ou senha incorretos." |
| `0` / sem status | "Verifique sua conexão com a internet." |
| Outros | "Ocorreu um erro. Tente novamente." |

---

### 6. `front-app/src/app/(auth)/cadastrar/page.tsx`

**Linhas removidas:**
```typescript
- import { createUserAndNotify } from '@/lib/auth';
- import { dbSet } from '@/lib/db';

- const { user: newUser } = createUserAndNotify(form.email, form.senha);
- dbSet('proprietarios', {
-   id: newUser.uid,
-   idProprietario: newUser.uid,
-   nome: form.nome, rg: form.rg,
-   documento: form.cpf.replace(/\D/g, ''), tipoDocumento: 'CPF',
-   telefone: form.telefone, email: form.email,
-   exibirNoSitePublico: false,
-   logradouro: { logradouro: form.logradouro, numero: form.numero, ... },
-   dataCadastro: new Date(), dataUltimaAlteracao: new Date(),
- });
- router.push('/entrar');
```

**Substituídas por:**
```typescript
+ import { cadastrarApi, loginApi } from '@/lib/authApi';

+ await cadastrarApi({ nome: form.nome, email: form.email, senha: form.senha });
+ await loginApi(form.email, form.senha);   // auto-login após cadastro
+ router.push('/dashboard');
```

> **Nota:** Os campos de endereço (CEP, logradouro, etc.) são mantidos no formulário para coleta de dados, mas o `POST /auth/cadastrar` do backend recebe apenas `{ nome, email, senha }`. Os dados de endereço serão enviados via `PATCH /usuarios/{id}` na próxima sprint quando o endpoint for expandido.

---

### 7. `front-app/src/app/(auth)/recuperar-senha/page.tsx`

**Linha removida:**
```typescript
- import { sendPasswordResetEmail } from '@/lib/auth';
- await sendPasswordResetEmail(email);   // era no-op — não fazia nada
```

**Substituída por:**
```typescript
+ import { recuperarSenhaApi } from '@/lib/authApi';
+ await recuperarSenhaApi(email);   // POST /auth/recuperar-senha
```

> O backend retorna `200` com `{ mensagem: "..." }` independente de o e-mail existir (por segurança). O comportamento de `setSent(true)` no `catch` é mantido para nunca revelar se o e-mail está cadastrado.

**Correção ortográfica aproveitada:**
```
- "voce recebera um link"
+ "você receberá um link"
```

---

### 8. `front-app/src/app/(app)/dashboard/page.tsx`

**Linha removida:**
```typescript
- const { proprietario } = useAuth();
- const firstName = proprietario?.nome?.split(' ')[0] || 'Produtor';
```

**Substituída por:**
```typescript
+ const { user } = useAuth();
+ const firstName = user?.nome?.split(' ')[0] || user?.email?.split('@')[0] || 'Produtor';
```

---

### 9. `front-app/src/app/(app)/perfil/page.tsx`

**Linhas removidas:**
```typescript
- import { reauthenticateWithCredential, updatePassword as localUpdatePassword } from '@/lib/auth';

- const { proprietario, user, logout } = useAuth();
// → const { user, logout } = useAuth();

- reauthenticateWithCredential(user.uid, { email: user.email!, password: pwdForm.atual });
- localUpdatePassword(user.uid, pwdForm.nova);

- const name = proprietario?.nome || user?.email || 'Usuário';
// → const name = user?.nome || user?.email || 'Usuário';

- {proprietario?.nome || 'Usuário'}
// → {user?.nome || 'Usuário'}

- {proprietario && ( ... )}   // bloco de dados pessoais com telefone/endereço do localStorage
// → Bloco simplificado mostrando "—" (dados virão via GET /usuarios/me na próxima sprint)
```

**Adicionado:**
```typescript
+ import { apiPost } from '@/lib/api';

// handleChangePwd agora chama:
+ await apiPost('/auth/redefinir-senha', {
+   email: user.email,
+   senhaAtual: pwdForm.atual,
+   novaSenha: pwdForm.nova,
+ });
```

---

## Contrato de Auth — Endpoints Consumidos

### `POST /auth/login`
**Request:**
```json
{ "email": "user@example.com", "senha": "MinhaS3nha!" }
```
**Response 200:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600
}
```
**Erros:** `401 Unauthorized` (credenciais inválidas)

---

### `POST /auth/cadastrar`
**Request:**
```json
{ "nome": "Maria da Silva", "email": "maria@example.com", "senha": "MinhaS3nha!" }
```
**Response 201:**
```json
{ "id": "uuid", "nome": "Maria da Silva", "email": "maria@example.com", "roles": ["ROLE_USER"] }
```
**Erros:** `409 Conflict` (e-mail já cadastrado), `400 Bad Request` (validação)

---

### `POST /auth/refresh`
**Request:**
```json
{ "refreshToken": "eyJ..." }
```
**Response 200:**
```json
{ "accessToken": "eyJ...", "refreshToken": "eyJ...", "tokenType": "Bearer", "expiresInSeconds": 3600 }
```

---

### `POST /auth/recuperar-senha`
**Request:**
```json
{ "email": "user@example.com" }
```
**Response 200:**
```json
{ "mensagem": "Se o e-mail estiver cadastrado, as instruções foram enviadas." }
```
> Sempre retorna 200 para não revelar se o e-mail existe.

---

### `GET /auth/me`
**Header:** `Authorization: Bearer {accessToken}`  
**Response 200:** `UsuarioResponseDTO` (id, nome, email, roles, ...)

---

## Fluxo de Sessão

```
[Montagem do App]
       ↓
  Tem sl_access_token?
       ↓ sim
  GET /auth/me
       ↓ 200          ↓ 401
  setUser(...)    silentRefresh()
                       ↓ ok        ↓ falha
                  GET /auth/me   clearTokens()
                                 setUser(null)

[Login]
  POST /auth/login → saveTokens() → GET /auth/me → setUser() → redirect /dashboard

[Refresh automático — transparente]
  Qualquer apiGet/apiPost/... recebe 401
       ↓
  POST /auth/refresh → saveTokens() → retry original request

[Sessão expirada sem refresh]
  clearTokens() → window.dispatchEvent('sl:session-expired') → AuthContext → setUser(null)
```

---

## Critérios de Aceite — Verificação

| Critério | Status |
|---|---|
| Login com credenciais válidas retorna JWT e inicia sessão | ✅ Implementado via `loginApi` |
| Cadastro cria usuário no backend e faz login em seguida | ✅ `cadastrarApi` + `loginApi` |
| Refresh token renova o access token sem novo login | ✅ `silentRefresh` em `api.ts` |
| Erros exibem mensagens em padrão do app | ✅ Mapeamento de status HTTP para mensagens |
| Sessão expirada faz logout automático | ✅ Evento `sl:session-expired` |

---

## Arquivos NÃO Alterados

| Arquivo | Motivo |
|---|---|
| `src/lib/auth.ts` | Mantido como está — pode ser removido em sprint futura após testes |
| `src/lib/db.ts` | Usado pelos hooks de sementes/pedidos/propriedades (ainda em localStorage) |
| `src/lib/storage.ts` | Não relacionado à autenticação |
| `src/lib/validators.ts` | Não relacionado |
| `src/context/NotificationContext.tsx` | Compatível — usa `user.uid` que persiste no `AuthSession` |
| `src/app/(app)/layout.tsx` | Sem referências a `proprietario` |
| Hooks (`useSeeds`, `useOrders`, `useProperties`) | Usam `user.uid` — compatível com `AuthSession.uid` |
| `src/types/*` | Sem alterações |
| `*.module.css` | Sem alterações |

---

## Dependências de Ambiente

Para executar a integração localmente:

1. Backend Spring Boot rodando em `http://localhost:8080`
2. PostgreSQL rodando em `localhost:5433` (conforme `application-dev.yml`)
3. Arquivo `front-app/.env.local` com `NEXT_PUBLIC_API_URL=http://localhost:8080`
4. CORS configurado no backend para `http://localhost:3000` (já configurado em `application-dev.yml`)

---

## Próximas Tarefas Sugeridas

- [ ] Expandir `POST /auth/cadastrar` para aceitar dados de endereço, ou criar `PATCH /usuarios/{id}` para atualização pós-cadastro
- [ ] Integrar tela de perfil com `GET /usuarios/me` para exibir telefone e endereço completos
- [ ] Remover `src/lib/auth.ts` após validação completa em ambiente de desenvolvimento
- [ ] Adicionar testes E2E para os fluxos de login e cadastro
