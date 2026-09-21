# Convergência da entidade `Usuario` para o modelo TPT

**Branch:** `dev-igor` · **Status:** aplicado, aguardando revisão · **Data:** 2026-09-20
**Referência normativa:** [Modelo-Conceitual-Banco-Dados.md](Modelo-Conceitual-Banco-Dados.md) — §2 (MER) e §8 (Estratégia de Herança)

---

## 1. Resumo

A entidade `Usuario` era uma classe **flat**, com `@Id` próprio e colunas `nome`, `email`, `senha`, `ativo` e `data_criacao`. O restante do domínio de pessoas (`Pessoa`, `Proprietario`, `Admin`) já seguia **Table Per Type (TPT)**. Essa divergência mantinha a branch `developer` sem compilar.

`Usuario` passou a ser:

```java
@Entity
@Table(name = "usuario_t")
@PrimaryKeyJoinColumn(name = "pessoa_id")
public class Usuario extends Pessoa implements UserDetails { ... }
```

O fluxo de autenticação (login, roles, tokens, recuperação de senha) foi preservado. **Dois arquivos do módulo de autenticação foram alterados**, um deles com três linhas trocadas.

---

## 2. Por que TPT, e não o contrário

Três fatos sustentam a decisão:

**A especificação manda TPT.** O Modelo Conceitual, §8, define a estratégia de herança Table Per Type para `usuario_t`, `proprietario_t` e `admin_t`, com `pessoa_id` atuando simultaneamente como PK e FK (equivalente a `@PrimaryKeyJoinColumn` no JPA). O MER da §2 modela `USUARIO_T` com **`uuid pessoa_id PK, FK`** e nenhuma outra coluna.

**O schema já era TPT dos dois lados.** Nenhuma migration jamais criou `usuario_t.id`, `nome`, `email`, `senha` ou `data_criacao`. A `V1_1__criar_schema_tabelas.sql` cria:

```sql
CREATE TABLE usuario_t(
    pessoa_id UUID PRIMARY KEY REFERENCES pessoa_t(id) ON DELETE CASCADE
);
```

E as três tabelas da `V2__criar_tabelas_autenticacao.sql` já referenciam essa PK:

```sql
usuario_id UUID NOT NULL REFERENCES usuario_t(pessoa_id) ON DELETE RESTRICT,  -- usuario_role_t
usuario_id UUID NOT NULL REFERENCES usuario_t(pessoa_id) ON DELETE RESTRICT,  -- refresh_token_t
usuario_id UUID NOT NULL REFERENCES usuario_t(pessoa_id) ON DELETE RESTRICT,  -- token_recuperacao_senha_t
```

Ou seja: a modelagem de autenticação já pressupunha TPT. O único artefato flat no repositório era a classe Java.

**A alternativa custaria mais.** Manter a entidade flat exigiria uma migration para adicionar seis colunas a `usuario_t` e **trocar a PK** de `pessoa_id` para `id`, recriando as três FKs acima — além de reescrever `UsuarioController`, `UsuarioService`, `UsuarioResponseDTO` e `PessoaController`, que já estavam escritos para TPT.

---

## 3. O que mudou no módulo de autenticação

### 3.1 `entity/Usuario.java` — reescrito

| Antes (flat) | Depois (TPT) |
|---|---|
| `@Id @GeneratedValue private UUID id` | herdado de `Pessoa` → coluna `pessoa_id` |
| `private String nome` | herdado de `Pessoa` |
| `private String email` | herdado de `Pessoa` |
| `private String senha` | `Pessoa.senhaHash` |
| `private LocalDateTime dataCriacao` | `Pessoa.dataCadastro` (mesma semântica, mesmo `@PrePersist`) |
| `private Boolean ativo` | **mantido**, agora `boolean` primitivo |
| `@ManyToMany Set<Role> roles` | **mantido, mapeamento idêntico** |
| `implements UserDetails` | **mantido** |
| Lombok `@Getter/@Setter/@Builder` | getters/setters manuais |

Os seis métodos de `UserDetails` continuam existindo. Só dois mudaram de origem:

```java
@JsonIgnore
@Override
public String getPassword() { return getSenhaHash(); }   // antes: this.senha

@Override
public String getUsername() { return getEmail(); }       // antes: this.email
```

`getAuthorities()`, `isEnabled()`, `isAccountNonExpired()`, `isAccountNonLocked()` e `isCredentialsNonExpired()` ficaram inalterados.

**Duas escolhas que valem registro:**

- **Lombok removido.** `@Builder` numa subclasse de classe abstrata só funciona com `@SuperBuilder` no pai *e* no filho, o que mudaria a API de construção de `Pessoa`, `Proprietario` e `Admin` junto. Optou-se por getters/setters manuais, igual às outras três entidades do domínio. Consequência: `Usuario.builder()` deixou de existir (dois call sites, ambos ajustados).
- **`@JsonIgnore` adicionado ao `getPassword()`.** `Pessoa.senhaHash` já tinha `@JsonIgnore`, mas `getPassword()` é um getter público que o Jackson serializaria como propriedade `password`. A versão flat tinha a mesma exposição; a anotação fecha isso.

`dataCriacao` foi removida sem substituto porque **nenhum código fora da própria entidade lia ou escrevia esse campo** (verificado por varredura em `src/`).

### 3.2 `service/AuthService.java` — 3 linhas

Ambas as edições são consequência direta da remoção do Lombok e do rename `senha` → `senhaHash`. **Nenhuma regra de negócio mudou.**

```diff
@@ cadastrar(CadastroRequestDTO) @@
-        Usuario usuario = Usuario.builder()
-                .nome(dto.nome())
-                .email(dto.email())
-                .senha(passwordEncoder.encode(dto.senha())) // Criptografia segura com BCrypt
-                .ativo(true)
-                .build();
+        Usuario usuario = new Usuario();
+        usuario.setNome(dto.nome());
+        usuario.setEmail(dto.email());
+        usuario.setSenhaHash(passwordEncoder.encode(dto.senha())); // Criptografia segura com BCrypt
+        usuario.setAtivo(true);
         usuario.getRoles().add(roleUsuario);
```

```diff
@@ redefinirSenha(RedefinirSenhaDTO) @@
-        usuario.setSenha(passwordEncoder.encode(dto.novaSenha())); // Atualiza a senha no banco com BCrypt
+        usuario.setSenhaHash(passwordEncoder.encode(dto.novaSenha())); // Atualiza a senha no banco com BCrypt
```

O cast `(Usuario) authentication.getPrincipal()` e o `usuario.getRoles().add(...)` **não precisaram de mudança**.

### 3.3 O que NÃO foi tocado

Nenhuma linha alterada em:

`UserDetailsServiceImpl` · `SecurityFilter` · `SecurityConfig` · `AuthController` · `JwtService` · `Role` · `RefreshToken` · `TokenRecuperacaoSenha` · `RoleRepository` · `RefreshTokenRepository` · `TokenRecuperacaoSenhaRepository` · `UsuarioRepository`

Motivo de cada um continuar válido:

- **`UserDetailsServiceImpl`** — `usuarioRepository.findByEmail()` devolve `Optional<Usuario>`, e `Usuario` continua sendo `UserDetails`. `email` agora vem de `Pessoa`, e o Spring Data resolve pela hierarquia.
- **`SecurityFilter`** — usa `isEnabled()` e `getAuthorities()` pela interface `UserDetails`.
- **`SecurityConfig`** — `hasRole("ADMIN")` casa com a authority `ROLE_ADMIN` produzida por `getAuthorities()` a partir de `PerfilEnum`. Nada na cadeia depende da forma da entidade.
- **`JwtService`** — usa `getRoles()`, `getEmail()`, `getId()` e `getNome()`; os quatro existem (roles em `Usuario`, o resto em `Pessoa`).
- **`usuario_role_t` / `Role`** — o `@JoinTable` aponta `usuario_id` para o identificador de `Usuario`, que no TPT é `pessoa_id`. Mesma coluna, mesma FK, mesmo valor.
- **`RefreshToken` / `TokenRecuperacaoSenha`** — `@ManyToOne Usuario` com FK já em `usuario_t(pessoa_id)`.

---

## 4. Migration nova

`backend/src/main/resources/db/migration/V4__adicionar_ativo_usuario.sql`

```sql
ALTER TABLE usuario_t ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
```

Aditiva: não altera coluna existente, não mexe em PK nem em FK, e linhas existentes assumem `TRUE` pelo `DEFAULT`.

A coluna `ativo` existia apenas na entidade e **nunca havia sido criada por migration nenhuma** — funcionava em dev só porque o profile usa `ddl-auto: update`. A migration regulariza isso.

Cadeia resultante, sem colisão de versão: `V1 → V1.1 → V1.2 → V1.3 → V2 → V3 → V4`.

---

## 5. Demais arquivos alterados

| Arquivo | Mudança |
|---|---|
| `dto/UsuarioResponseDTO.java` | `+ fromEntity(Usuario)` (o método já era chamado por `AuthController` e `AuthService`, mas não existia); `+ Set<String> roles` mapeado de `role.getNome().name()` |
| `controller/UsuarioController.java` | 3 linhas preenchendo `roles` no `mapToResponse` |
| `test/.../PedidoServiceTest.java` | `Usuario.builder()` → `new Usuario()` + setters (teste Mockito, não persiste) |

---

## 6. Estado da build

### `mvn clean compile` — **BUILD SUCCESS**

Os 29 erros de compilação que bloqueavam a branch foram a zero. Destes, 27 (em `UsuarioController`, `UsuarioService` e `PessoaController`) desapareceram **sem edição nesses arquivos** — já estavam escritos para TPT. Os outros 2 eram o `fromEntity` ausente.

### `mvn clean test` — 113 testes, 0 falhas, 14 erros, 1 pulado

**98 passaram.** Nenhuma falha de asserção. Verde integral em `PedidoServiceTest` (20), `NotificacaoServiceTest` (9), `RelatorioServiceTest` (8), `DocumentoValidatorTest` (8), `EstoqueServiceTest` (7), `ProdutoServiceTest` (7), `ProdutoValidacaoServiceTest` (7), `PessoaServiceTest` (5), `ProprietarioServiceTest` (5), `UsuarioServiceTest` (5), `StatusPedidoTest` (5), `UsuarioControllerTest` (3) e `ProprietarioControllerTest` (3).

Os dois sinais mais relevantes para esta mudança: **`UsuarioControllerTest` e `UsuarioServiceTest` passam integralmente**, e o `NotificacaoRelatorioPersistenciaTest` grava entidades TPT em H2 com sucesso em 6 dos 7 testes.

Os 14 erros têm três causas raiz, **nenhuma relacionada à convergência TPT**:

| Causa | Erros | Detalhe |
|---|---|---|
| `JavaMailSender` ausente no contexto de teste | 5 | Nenhum arquivo de `src/test/resources/` define `spring.mail.*`, então o bean não é autoconfigurado. `EmailService` → `AuthService` → `AuthController` não instanciam. Atinge `BackendApplicationTests` e `GlobalExceptionHandlerIntegrationTest`. |
| Flyway executa `CREATE EXTENSION` contra H2 | 6 | `application-test.yaml` tem `flyway.enabled: true` sobre H2, e a `V1_1` começa com `CREATE EXTENSION IF NOT EXISTS "uuid-ossp"`, sintaxe exclusiva do PostgreSQL. Quebra na primeira migration. Atinge os quatro testes de repositório do profile `test`. |
| `Detached entity passed to persist: Pedido` | 1 | `NotificacaoRelatorioPersistenciaTest` linha 82 faz `Pedido.builder().id(UUID.randomUUID()).build()`; como `Pedido.id` tem `@GeneratedValue`, o Hibernate trata a instância como *detached*. Correção de uma linha: remover o `.id(...)`. |
| Sem causa raiz própria (cascata) | 2 | `PostgresIntegrationTest`. |

**Ressalva de leitura do censo:** o Spring usa `spring.test.context.failure.threshold = 1` por padrão. Depois da primeira falha de contexto, as cargas seguintes são puladas e marcadas como erro. Parte dos 14 são tentativas puladas, não falhas independentes.

**Ressalva de baseline:** não há um "antes" mensurável. A branch não compilava, então `mvn test` nunca havia executado. A atribuição acima é por causa raiz, não por comparação.

---

## 7. Pendências

### 7.1 `POST /auth/cadastrar` — decisão de produto

O `CadastroRequestDTO` coleta apenas `nome`, `email` e `senha`. Com TPT, o `save()` grava em `pessoa_t`, que exige:

```sql
tipo_documento VARCHAR(10) NOT NULL,
documento      VARCHAR(14) NOT NULL,
```

O `INSERT` chega ao banco com ambos nulos e é rejeitado. A exceção sobe como `DataIntegrityViolationException`, que o `GlobalExceptionHandler` já trata — a resposta é **409 Conflict** com `"Violação de integridade nos dados."`, não 500.

**Escopo do impacto: apenas esse endpoint.** `POST /auth/login`, `/auth/refresh`, `/auth/recuperar-senha`, `/auth/redefinir-senha`, `GET /auth/me`, roles, tokens e os endpoints protegidos seguem funcionais — nenhum deles cria `Pessoa`. Usuários criados por `POST /api/usuarios` (que usa `UsuarioCreateRequestDTO`, com `tipoDocumento`, `documento`, `telefone` e `endereco`) nascem válidos e autenticam normalmente.

Não há teste automatizado cobrindo `/auth/cadastrar`, então isso não aparece no censo da seção 6.

Duas saídas possíveis, **ambas dependem de decisão da liderança** — nenhuma foi aplicada:

- **(a)** `CadastroRequestDTO` passa a coletar CPF/CNPJ, alinhando o cadastro público ao modelo de dados.
- **(b)** `/auth/cadastrar` é descontinuado em favor de `POST /api/usuarios`.

Tornar as colunas *nullable* foi descartado por contrariar o Modelo Conceitual (§3.1) e as regras de integridade R2.

### 7.2 Itens fora do escopo desta mudança

| Item | Dono | Observação |
|---|---|---|
| `spring.mail.*` no config de teste | autenticação | Destrava 5 erros de contexto |
| `CREATE EXTENSION` da `V1_1` sob H2 | modelo de dados | Destrava 6 erros; exige decidir entre desligar Flyway no profile `test` ou tornar a V1_1 compatível com H2 |
| `.id(...)` no `Pedido.builder()` do teste de persistência | domínio Pedido | Correção de uma linha |
| `existsByEmail` do `AuthService` | autenticação | Hoje consulta só `Usuario`, mas `pessoa_t.email` é `UNIQUE` global. Um e-mail já usado por `Proprietario`/`Admin` passa na checagem e só falha no INSERT — com a mensagem correta (409 "E-mail já cadastrado no sistema."), via `uk_pessoa_email`. **Deliberadamente não alterado.** |

---

## 8. Checklist de revisão sugerido

- [ ] `Usuario` TPT não perdeu nenhum comportamento de `UserDetails` usado em produção
- [ ] `roles` continua populando corretamente as authorities no JWT e no `hasRole("ADMIN")`
- [ ] Fluxo completo de login/refresh/recuperação testado manualmente contra PostgreSQL
- [ ] Decisão sobre `/auth/cadastrar` (seção 7.1)
- [ ] `@JsonIgnore` em `getPassword()` aprovado ou revertido
- [ ] Campo `roles` no `UsuarioResponseDTO` atende ao que o frontend consome em `GET /auth/me`
