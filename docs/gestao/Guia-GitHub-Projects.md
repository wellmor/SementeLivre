# Guia: Configuração do GitHub Projects — Semente Livre

**Projeto:** https://github.com/users/wellmor/projects/1  
**Repositório:** wellmor/SementeLivre  
**Objetivo:** Configurar milestones, labels e cards do cronograma no GitHub Projects

---

## 1. Criar Milestones

Acesse: `https://github.com/wellmor/SementeLivre/milestones/new`

Crie os 5 milestones abaixo com as descrições e datas:

| # | Title | Description | Due Date |
|---|-------|-------------|----------|
| 1 | **Fase 0 — Preparação & Estudos** | Semana 0: configuração do ambiente, estudos individuais, protótipos, setup do repo e Kanban | *(1 semana após início)* |
| 2 | **Fase 1 — Fundação** | Semanas 1–2: setup Spring Boot, Docker, mapeamento JPA de 10 entidades, CRUD básico em 7 controllers, exceptions globais | *(2 semanas após início)* |
| 3 | **Fase 2 — Lógica de Negócio & Segurança** | Semanas 3–4: JWT completo, roles, validações de negócio, Flyway migrations, testes unitários | *(4 semanas após início)* |
| 4 | **Fase 3 — Integração & Funcionalidades Avançadas** | Semanas 5–6: integração frontend-backend, relatórios PDF/CSV, notificações automáticas, testes cross-domain | *(6 semanas após início)* |
| 5 | **Fase 4 — Qualidade & Deploy** | Semanas 7–8: bug fixes, otimização, testes de carga/segurança, deploy Docker, smoke tests | *(8 semanas após início)* |

---

## 2. Criar Labels de Domínio *(opcional)*

> Pode pular esta etapa por enquanto. Crie as labels depois se necessário.

---

## 3. Como Criar Cards no GitHub Projects

### Criando um card novo

1. Abra o projeto: `https://github.com/users/wellmor/projects/1`
2. Clique em **"+ Add item"** na parte inferior de qualquer coluna
3. Digite o título do card e pressione Enter
4. Para editar os campos, clique no card → painel lateral abre à direita

### Configurando cada card

Ao clicar no card, no painel lateral você configura:

| Campo | Como fazer |
|-------|------------|
| **Status** | Clique no campo "Status" → selecione "Backlog" (início) |
| **Priority** | Clique no campo "Priority" → selecione P0, P1 ou P2 |
| **Size** | Clique no campo "Size" → selecione XS, S, M, L ou XL |
| **Milestone** | Clique em "Milestone" → selecione a fase correspondente |
| **Assignee** | Clique em "Assignees" → selecione o dev responsável |

### Dica: Criar múltiplos cards rápido

Use o **"+ Add item"** na coluna "Backlog" e adicione vários títulos de uma vez (um por linha). Depois, clique em cada um para configurar os campos.

---

## 4. Cards da Fase 0 — Com Descrições Detalhadas

> Cada card abaixo tem uma descrição pronta para colar no campo "Description" do GitHub.
> Ajuste o **Assignee** para o dev correspondente ao criar cada card.

---

### Dev 1 — Docker, Spring Boot & Flyway

**Título do card:**
`Estudo: Docker + Docker Compose + Spring Boot + Flyway`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição (copie e cole):**

```
## Objetivo
Criar um repositório de estudo pessoal demonstrando domínio de Docker, Docker Compose, Spring Boot (IOC, DI, profiles) e Flyway.

## O que estudar
1. **Docker:** o que é, como funciona (containers vs VMs), Dockerfile, comandos básicos (build, run, exec, logs, down)
2. **Docker Compose:** como orstrar múltiplos containers, arquivo docker-compose.yml, depends_on, volumes, networks, port mapping
3. **Spring Boot:** criar projeto com Spring Initializr, conceitos de IOC (Inversion of Control) e DI (Dependency Injection), como o Spring gerencia beans, profiles (dev, test, prod), application.yml com profile-specific configs
4. **Flyway:** o que é, como funciona (convenção de nomes V1__, V2__), como integrar com Spring Boot, como criar uma migration simples

## O que entregar
- Repositório GitHub pessoal (ou pasta no repo do projeto) com:
  - Spring Boot app mínima que conecta no PostgreSQL via Docker Compose
  - Dockerfile funcional
  - docker-compose.yml com PostgreSQL + app
  - Pelo menos 2 migrations Flyway (criar tabela + alterar tabela)
  - README.md com cheatsheet dos comandos Docker e Flyway mais usados

## Critérios de aceite
- [ ] `docker compose up` sobe PostgreSQL + app sem erro
- [ ] App conecta no banco e responde via HTTP
- [ ] Flyway roda as migrations automaticamente ao iniciar
- [ ] README documenta os comandos principais

## Referências
- https://docs.docker.com/get-started/
- https://docs.docker.com/compose/
- https://spring.io/guides/gs/relational-data-access
- https://flywaydb.org/documentation/
```

---

### Dev 2 — Spring Security, JWT & BCrypt

**Título do card:**
`Estudo: Spring Security + JWT + filtros HTTP + BCrypt`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição:**

```
## Objetivo
Criar protótipo de autenticação funcional com Spring Security + JWT, demonstrando conceitos de segurança HTTP, filtros e hash de senhas.

## O que estudar
1. **Spring Security:** o que é, como funciona a filter chain, como configurar SecurityFilterChain, @EnableWebSecurity
2. **JWT (JSON Web Token):** o que é, como funciona (header, payload, signature), como gerar e validar tokens, onde armazenar no cliente
3. **Filtros HTTP:** como criar um filtro customizado (JwtAuthenticationFilter), como interceptar requests antes do controller
4. **BCrypt:** o que é salting, como usar BCryptPasswordEncoder para hash de senhas, por que não usar MD5/SHA

## O que entregar
- Protótipo "hello world JWT" com:
  - Endpoint POST /login que recebe email + senha e retorna JWT
  - Endpoint GET /me que retorna dados do usuário autenticado (protegido por JWT)
  - Endpoint POST /cadastrar que cria usuário com senha em BCrypt
  - Filtro JWT que valida o token em requests protegidos
  - Roles básicas (PROPRIETARIO, ADMIN) via JWT claims

## Critérios de aceite
- [ ] POST /cadastrar retorna 201 e salva senha com BCrypt
- [ ] POST /login retorna JWT válido com credenciais corretas
- [ ] GET /me sem token retorna 401
- [ ] GET /me com token válido retorna dados do usuário
- [ ] Token expira após tempo configurado
- [ ] README documenta a estrutura do JWT e como testar

## Referências
- https://spring.io/guides/gs/securing-web/
- https://jwt.io/introduction
- https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/bcrypt.html
```

---

### Dev 3 — JPA/Hibernate, Herança TPT, DTOs

**Título do card:**
`Estudo: JPA/Hibernate + herança TPT + DTOs — entidade Pessoa mapeada`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição:**

```
## Objetivo
Mapear a entidade Pessoa do modelo conceitual usando JPA/Hibernate, aplicando herança Table Per Type (TPT) e DTOs para transferência de dados.

## O que estudar
1. **JPA/Hibernate:** o que é ORM, anotações principais (@Entity, @Table, @Id, @Column, @GeneratedValue), como o Hibernate gera SQL
2. **Herança TPT (Table Per Type):** como mapear Pessoa → Usuario/Proprietario/Admin com tabelas separadas, @Inheritance(strategy = InheritanceType.JOINED)
3. **DTOs (Data Transfer Objects):** por que usar DTOs vs retornar entidades diretamente, como criar DTOs de request e response
4. **Validações Bean Validation:** @NotNull, @Size, @Email, @Pattern para CPF, como validar no DTO

## O que entregar
- Entidade Pessoa mapeada com JPA:
  - Campos: id (UUID), tipoDocumento (CPF/CNPJ), documento, nome, telefone, email, senhaHash, logradouro, dataCadastro, dataUltimaAlteracao
  - Constraints: UNIQUE em (tipoDocumento, documento), UNIQUE em email
  - Herança TPT: Usuario, Proprietario, Admin como subclasses
- DTOs de request/response para Pessoa
- Validações Bean Validation nos DTOs
- Teste básico de persistência (insert + select)

## Modelo de referência (ver docs/modelo-dados/Modelo-Conceitual-Banco-Dados.md)
- Tabela pessoa_t: tipo_documento, documento, nome, telefone, email, senha_hash, logradouro_id
- Tabela usuario_t: id, pessoa_id (1:1)
- Tabela proprietario_t: id, pessoa_id, rg, exibir_no_site_publico (1:1)
- Tabela admin_t: id, pessoa_id, nivel_acesso (1:1)

## Critérios de aceite
- [ ] Entidade Pessoa mapeada com todas as anotações JPA corretas
- [ ] Herança TPT funciona (criar Pessoa + Proprietario em tabelas separadas)
- [ ] DTOs criados para create/update/response
- [ ] Validações Bean Validation funcionando (email único, documento único)
- [ ] Pelo menos 1 teste de persistência passando

## Referências
- https://docs.jboss.org/hibernate/orm/6.4/guide/html_single/Hibernate_Guide.html
- https://docs.oracle.com/javaee/7/api/javax/persistence/InheritanceType.html
```

---

### Dev 4 — JPA Relationships, Lombok & Bean Validation

**Título do card:**
`Estudo: JPA relationships + Lombok + Bean Validation — protótipo Comunidade + Propriedade`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição:**

```
## Objetivo
Mapear as entidades Comunidade e Propriedade com relacionamentos JPA (OneToMany, ManyToOne), usar Lombok para reduzir boilerplate e aplicar Bean Validation.

## O que estudar
1. **Relacionamentos JPA:** @OneToMany, @ManyToOne, @JoinColumn, cascade types (ALL, PERSIST, MERGE), fetch types (LAZY vs EAGER), mappedBy
2. **Lombok:** @Data, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder — como reduzir código repetitivo
3. **Bean Validation:** @NotBlank, @Positive, @Size, @Valid em controllers, como validar objetos aninhados

## O que entregar
- Entidade Comunidade mapeada:
  - Campos: id, nome, logradouro, status (ATIVA/PENDENTE_APROVACAO/REJEITADA), dataSolicitacao, dataAprovacao
  - Relacionamento: Comunidade → Logradouro (ManyToOne)
- Entidade Propriedade mapeada:
  - Campos: id, nome, tamanhoHectares, logradouro, dataCadastro, dataUltimaAlteracao
  - Relacionamentos: Propriedade → Proprietario (ManyToOne), Propriedade → Comunidade (ManyToOne)
  - Regra: exclusão bloqueada se tiver estoque ou pedidos vinculados
- Entidade Logradouro mapeada:
  - Campos: id, logradouro, numero, complemento, bairro, municipio, uf, cep
- DTOs para Comunidade e Propriedade
- Lombok aplicado em todas as entidades

## Modelo de referência
- comunidade_t: nome, logradouro_id, status, data_solicitacao, data_aprovacao
- propriedade_t: nome, tamanho_hectares, logradouro_id, proprietario_id, comunidade_id
- logradouro_t: logradouro, numero, complemento, bairro, municipio, uf, cep

## Critérios de aceite
- [ ] Comunidade mapeada com relacionamento ManyToOne → Logradouro
- [ ] Propriedade mapeada com ManyToOne → Proprietario e ManyToOne → Comunidade
- [ ] Lombok @Data/@Builder aplicado em todas as entidades
- [ ] Validações Bean Validation funcionando (@NotBlank no nome, @Positive no tamanho)
- [ ] Teste de persistência: criar Comunidade + Propriedade com relacionamentos

## Referências
- https://www.baeldung.com/jpa-one-to-many
- https://projectlombok.org/
```

---

### Dev 5 — Spring Data JPA, Enums PostgreSQL & Upload

**Título do card:**
`Estudo: Spring Data JPA + enums PostgreSQL + upload — protótipo Produto + Estoque`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição:**

```
## Objetivo
Mapear as entidades Produto e Estoque usando Spring Data JPA, praticar enums no PostgreSQL e implementar upload de arquivos (fotos).

## O que estudar
1. **Spring Data JPA:** repositories (JpaRepository), queries derivadas, @Query com JPQL, Pageable para paginação
2. **Enums no PostgreSQL:** como criar tipos enum no PostgreSQL, como mapear com @Enumerated(EnumType.STRING), migrar enums com Flyway
3. **Upload de arquivos:** como receber arquivo multipart, salvar localmente ou em nuvem (Firebase Storage, Supabase Storage), retornar URL

## O que entregar
- Entidade Produto mapeada:
  - Campos: id, nomePopular, nomeCientifico, historico, urlFoto, tipo (enum), especie (enum), formato (enum), comunidadeOrigem, dataInclusao, dataUltimaAlteracao
  - Enums: TipoProduto (HORTALICA, FRUTIFERA, FORRAGEIRA, CEREAL, LEGUMINOSA, VERDURA, MEDICINAL, OUTRAS), EspecieGeral, FormatoProduto
- Entidade Estoque mapeada:
  - Campos: id, proprietario, produto, descricao, preco, quantidade, tipoPesagem (enum), disponibilidade (enum), tipoMovimentacao (enum), dataMovimentacao, dataUltimaAtualizacao
  - Constraint: UNIQUE (proprietario_id, produto_id)
  - Regra: quantidade >= 0
- Repositórios Spring Data JPA para ambos
- Endpoint básico de upload de foto (retorna URL)
- Testes de queries derivadas (buscar por tipo, por disponibilidade)

## Modelo de referência
- produto_t: nome_popular, nome_cientifico, historico, url_foto, tipo, especie, formato, comunidade_origem_id
- estoque_t: proprietario_id, produto_id, descricao, preco, quantidade, tipo_pesagem, disponibilidade, tipo_movimentacao

## Critérios de aceite
- [ ] Produto mapeado com todos os enums como @Enumerated(STRING)
- [ ] Estoque mapeado com constraint UNIQUE (proprietario, produto)
- [ ] Repositórios com pelo menos 2 queries customizadas cada
- [ ] Upload de foto funciona (arquivo salvo, URL retornada)
- [ ] Testes de queries passando

## Referências
- https://spring.io/guides/gs/accessing-data-jpa/
- https://docs.jboss.org/hibernate/orm/6.4/guide/html_single/Hibernate_Guide.html#mapping-enum
```

---

### Dev 6 — Transações JPA, Services & JUnit 5

**Título do card:**
`Estudo: Transações JPA + lógica em services + JUnit 5 — protótipo Pedido + Itens`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição:**

```
## Objetivo
Mapear as entidades Pedido e Itens, praticar transações JPA (@Transactional), lógica de negócio em services e testes unitários com JUnit 5.

## O que estudar
1. **Transações JPA:** @Transactional (propagation, isolation, rollbackFor), quando usar REQUIRED vs REQUIRES_NEW, como o Spring gerencia transações
2. **Lógica em services:** separação de responsabilidades (Controller → Service → Repository), como implementar regras de negócio (ex: verificar estoque antes de criar pedido)
3. **JUnit 5:** @Test, @BeforeEach, @ExtendWith(MockitoExtension.class), @Mock, @InjectMocks, assertThrows, assertEquals

## O que entregar
- Entidade Pedido mapeada:
  - Campos: id, tipoPedido (VENDA/TROCA/DOACAO), mensagemOpcional, dataPedido, status (PENDENTE/CONFIRMADO/CANCELADO)
  - Relacionamentos: Pedido → Usuario (ManyToOne), Pedido → Proprietario (ManyToOne)
- Entidade Itens mapeada:
  - Campos: id, quantidade, precoUnitario
  - Relacionamento: Itens → Pedido (ManyToOne), Itens → Produto (ManyToOne)
  - Regra: Pedido 1:N Itens (composition)
- PedidoService com lógica:
  - Criar pedido (validar estoque disponível)
  - Confirmar pedido (subtrair estoque)
  - Cancelar pedido (restaurar estoque)
- Pelo menos 3 testes unitários com JUnit 5 + Mockito

## Modelo de referência
- pedido_t: tipo_pedido, mensagem_opcional, data_pedido, status, usuario_solicitante_id, proprietario_recebedor_id
- itens_pedido_t: pedido_id, produto_id, quantidade, preco_unitario

## Critérios de aceite
- [ ] Pedido e Itens mapeados com relacionamentos corretos
- [ ] PedidoService implementa criar/confirmar/cancelar com @Transactional
- [ ] Teste: criar pedido com estoque suficiente → sucesso
- [ ] Teste: criar pedido com estoque insuficiente → exceção
- [ ] Teste: cancelar pedido restaura estoque
- [ ] JUnit 5 + Mockito configurados e rodando

## Referências
- https://spring.io/guides/gs/testing-web/
- https://junit.org/junit5/docs/current/user-guide/
```

---

### Dev 7 — Geração de PDF & CSV

**Título do card:**
`Estudo: Geração de PDF (iText) + CSV (OpenCSV) — relatório simples`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição:**

```
## Objetivo
Implementar geração de relatórios em PDF e CSV, estudando bibliotecas iText/OpenPDF e OpenCSV.

## O que estudar
1. **PDF com iText/OpenPDF:** como criar documento PDF programaticamente, adicionar texto, tabelas, cabeçalho, formatação, salvar como byte[]
2. **CSV com OpenCSV:** como escrever dados em CSV, usar annotations @CsvBindByName, gerar String CSV a partir de lista de objetos
3. **Geração de relatório:** como combinar dados do banco com template de relatório, retornar arquivo para download

## O que entregar
- Serviço GeraRelatorioService com:
  - gerarPdf(List<Produto> produtos, String titulo) → byte[]
  - gerarCsv(List<Produto> produtos) → String
- O PDF deve conter:
  - Título do relatório
  - Data de geração
  - Tabela com colunas: Nome Popular, Tipo, Espécie, Formato, Data Inclusão
- O CSV deve conter:
  - Header row com os mesmos campos
  - Uma linha por produto
- Teste unitário que gera PDF e valida que não é nulo
- Teste unitário que gera CSV e valida o conteúdo

## Critérios de aceite
- [ ] iText/OpenPDF adicionado como dependência
- [ ] OpenCSV adicionado como dependência
- [ ] gerarPdf() retorna byte[] válido (PDF legível)
- [ ] gerarCsv() retorna String CSV com header + dados
- [ ] Testes unitários passando para ambos os métodos
- [ ] README documenta como usar o serviço

## Referências
- https://itextpdf.com/resources/free-tutorials
- https://opencsv.sourceforge.net/
```

---

### Dev 8 — Swagger, TestContainers & CI/CD

**Título do card:**
`Estudo: Swagger/OpenAPI + TestContainers + CI/CD básico`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição:**

```
## Objetivo
Configurar documentação de API com Swagger/OpenAPI, testes de integração com TestContainers e pipeline básico de CI/CD.

## O que estudar
1. **Swagger/OpenAPI:** o que é, como integrar com Spring Boot (springdoc-openapi), como documentar endpoints com @Operation, @Schema, como acessar a UI (swagger-ui.html)
2. **TestContainers:** o que é, como subir PostgreSQL em container para testes, como configurar @Container, @DynamicPropertySource, como limpar containers após testes
3. **CI/CD básico:** o que é pipeline de integração contínua, como configurar GitHub Actions para rodar testes automaticamente ao fazer push

## O que entregar
- Spring Boot app com springdoc-openapi configurado:
  - Endpoint /swagger-ui.html acessível
  - Pelo menos 2 endpoints documentados com @Operation
- Teste de integração com TestContainers:
  - Sobe PostgreSQL em container Docker
  - Roda migrate (Flyway ou JDBC)
  - Executa query simples no banco
  - Valida que a conexão funciona
- GitHub Actions workflow (.github/workflows/ci.yml):
  - Roda build + testes ao fazer push para main
  - Usa Java 17+ e Docker

## Critérios de aceite
- [ ] /swagger-ui.html abre e mostra a documentação da API
- [ ] Teste de integração com TestContainers passa (PostgreSQL em container)
- [ ] GitHub Actions workflow existe e está funcional (build passa no push)
- [ ] README documenta como acessar o Swagger e rodar os testes

## Referências
- https://springdoc.org/
- https://www.testcontainers.org/
- https://docs.github.com/en/actions
```

---

### Thales — Infraestrutura de Testes

Thales tem 6 tasks na Fase 0. Crie **um card para cada** ou **um card único** com as 6 subtarefas na descrição. Segue a sugestão com card único (mais prático):

**Título do card:**
`Thales: Infraestrutura de testes — convenções, fixtures, templates e API contracts`

**Priority:** P0 | **Size:** L | **Milestone:** Fase 0

**Descrição:**

```
## Objetivo
Configurar toda a infraestrutura de testes do projeto backend antes do início do desenvolvimento, garantindo que os devs tenham um padrão claro e ferramentas prontas para usar.

## Tasks desta Fase

### 1. Padrão de testes (convenções)
- Definir nomenclatura: testes em classe separada do service, sufixo Test, método descritivo
- Estrutura de pastas: src/test/java/.../mesmo.pacote.do.service/
- Organização: testes unitários (sem banco) vs integração (com banco via TestContainers)
- Documentar convenções em um arquivo TESTING.md na raiz do repo

### 2. TestContainers (configuração base)
- Criar classe base de teste que sobe PostgreSQL em container Docker
- Configurar @DynamicPropertySource para injetar URL do banco
- Garantir que o container é limpo após cada teste
- Criar profile Spring para testes (application-test.yml)

### 3. Fixture de dados (testdata builder)
- Criar classe PessoaFixture com métodos para gerar Pessoa de teste (com dados válidos)
- Criar classe ProdutoFixture com métodos para gerar Produto de teste
- Criar classe PedidoFixture com métodos para gerar Pedido de teste
- Cada fixture deve ter método criar() e criarComTodosOsCampos()

### 4. Template de teste de integração
- Criar classe base BaseIntegrationTest com:
  - Configuração do TestContainers
  - @BeforeEach: limpa tabelas ou reseta sequences
  - @AfterEach: rollback de transações
  - Métodos utilitários: salvar(), buscar(), deletar()
- Criar 1 exemplo de teste de integração completo (ex: PessoaRepositoryIntegrationTest)

### 5. Cobertura de código (JaCoCo)
- Configurar plugin JaCoCo no pom.xml/build.gradle
- Definir meta mínima: 70% de cobertura de código
- Configurar para gerar relatório HTML em target/site/jacoco
- Adicionar verificação de cobertura no build (fail se < 70%)

### 6. API Contracts
- Documentar contratos das APIs principais que os devs vão implementar:
  - POST /api/auth/login (request/response)
  - POST /api/pessoas (request/response)
  - GET /api/produtos (request/response com paginação)
  - POST /api/pedidos (request/response)
- Usar formato simples: Método + URL + Request Body + Response Body + Status Codes
- Salvar em docs/api-contracts.md

## Entregáveis
- [ ] Arquivo TESTING.md com convenções documentadas
- [ ] Classe BaseIntegrationTest com TestContainers configurado
- [ ] 3 classes Fixture (Pessoa, Produto, Pedido)
- [ ] 1 teste de integração exemplo passando
- [ ] JaCoCo configurado no build
- [ ] docs/api-contracts.md com contratos das APIs principais

## Referências
- https://www.testcontainers.org/guide/junit5/
- https://www.jacoco.org/jacoco/trunk/doc/
```

---

## 5. Resumo da Fase 0 para o Kanban

| Card | Assignee | Priority | Size |
|------|----------|----------|------|
| Estudo: Docker + Spring Boot + Flyway | Dev 1 | P0 | L |
| Estudo: Spring Security + JWT + BCrypt | Dev 2 | P0 | L |
| Estudo: JPA/Hibernate + TPT + DTOs | Dev 3 | P0 | L |
| Estudo: JPA Relationships + Lombok | Dev 4 | P0 | L |
| Estudo: Spring Data JPA + Enums + Upload | Dev 5 | P0 | L |
| Estudo: Transações JPA + Services + JUnit 5 | Dev 6 | P0 | L |
| Estudo: PDF (iText) + CSV (OpenCSV) | Dev 7 | P0 | L |
| Estudo: Swagger + TestContainers + CI/CD | Dev 8 | P0 | L |
| Infraestrutura de testes | Thales | P0 | L |

**Total: 9 cards na Fase 0** (só devs + Thales, sem Karla)

---

## Resumo: Fases 1 a 4

As fases 1 a 4 seguem a mesma estrutura da tabela do cronograma. Cada card tem título, priority, size e milestone.

> Para ver a lista completa dos cards das fases 1-4, consulte o arquivo `docs/cronograma/Cronograma-Backend-Sprint.md`.

---

## 5. Ordem Recomendada de Configuração

1. **Criar os 5 milestones** (2 minutos)
2. **Criar os cards da Fase 0** (10 minutos) — com as descrições acima
3. **Criar os cards das Fases 1-4** (30 minutos) — consulte o cronograma
4. **Atribuir assignees** aos cards conforme a equipe for definida
5. **Mover cards para "Ready"** quando a fase iniciar

**Tempo estimado total:** ~1 hora

---

*Guia gerado para o projeto Semente Livre — IF Sudeste MG Campus Rio Pomba*  
*Agosto 2026*
