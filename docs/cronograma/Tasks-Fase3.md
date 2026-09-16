# Fase 3 — Cards de Atividades

**Projeto:** Semente Livre  
**Disciplina:** AAIFE3 — IF Sudeste MG Campus Rio Pomba  
**Fase:** 3 — Integração & Funcionalidades Avançadas (Semanas 5–6)  
**Equipe:** 8 desenvolvedores + 2 líderes

---

# Semana 5

---

## Dev 1 · S5 — Health checks, monitoramento e métricas

**Objetivo**
Tornar a aplicação observável em ambiente integrado: expor endpoints de health check, métricas e logs estruturados para acompanhamento do estado do backend.

**O que fazer**
- Adicionar e configurar Spring Boot Actuator nos profiles dev e prod
- Expor `/actuator/health` (liveness/readiness) e `/actuator/metrics`
- Integrar Micrometer com Prometheus (`/actuator/prometheus`)
- Configurar logs estruturados (SLF4J/Logback) com contexto de requisição
- Restringir acesso aos endpoints sensíveis do Actuator (não expor detalhes em prod)

**O que entregar**
- Endpoints de health checks e métricas funcionando no ambiente integrado
- Painel básico (ou exportação) de métricas via Prometheus
- Logs estruturados em produção

**Critérios de aceite**
- `GET /actuator/health` retorna 200 com status `UP`
- `/actuator/prometheus` expõe métricas da JVM e de HTTP
- Métricas não expõem dados sensíveis
- Endpoints de Actuator protegidos/limitados no profile de produção

**Referências**
- https://docs.spring.io/spring-boot/reference/actuator/index.html
- https://micrometer.io/docs
- https://prometheus.io/docs/introduction/overview/

---

## Dev 2 · S5 — Integração dos endpoints de auth com o front-app

**Objetivo**
Garantir que a autenticação do front-app (PWA) consuma o backend REST: login, cadastro, refresh e recuperação de senha.

**O que fazer**
- Revisar e estabilizar os endpoints de auth do backend (`/auth/cadastrar`, `/auth/login`, `/auth/refresh`, `/auth/recuperar-senha`)
- Produzir o contrato do fluxo de auth para o frontend (payloads de request/response e status codes)
- Integrar as telas de entrar/cadastrar/recuperar-senha do front-app para consumir a API
- Tratar erros de autenticação no frontend (credenciais inválidas, sessão expirada, sem conexão)

**O que entregar**
- Contrato de auth documentado para os frontends
- Login, cadastro, refresh e recuperação de senha do front-app funcionando via backend

**Critérios de aceite**
- Login com credenciais válidas retorna JWT e inicia sessão no front-app
- Cadastro cria usuário no backend e permite login em seguida
- Refresh token renova o access token sem novo login
- Erros de credenciais/sessão exibem mensagens no padrão do app (toast)

**Referências**
- https://nextjs.org/docs/app
- https://jwt.io/introduction
- https://docs.spring.io/spring-security/reference/servlet/authentication/index.html

---

## Dev 3 · S5 — Endpoints de perfil do site público

**Objetivo**
Expor, via REST público, o perfil do produtor para o front-site (listagem do produtor, dados básicos e sementes cultivadas).

**O que fazer**
- Criar endpoint público de perfil do proprietário (dados públicos: nome, município, comunidades)
- Criar endpoint público das sementes/produtos vinculados ao proprietário (apenas disponíveis publicamente)
- Respeitar LGPD: não expor CPF, RG, telefone, e-mail ou dados sensíveis
- Estruturar DTOs de resposta pública separados dos DTOs internos

**O que entregar**
- Endpoints públicos de perfil de produtor integrados ao front-site

**Critérios de aceite**
- Perfil do produtor carregado na página pública do front-site
- Lista de sementes cultivadas do produtor exibida na página pública
- Endpoints acessíveis sem autenticação
- Dados sensíveis (CPF/RG/telefone/e-mail) ausentes nas respostas públicas
- `GET /produtores/{id}` retorna 404 para produtor inexistente

**Referências**
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- https://www.gov.br/privacidade-e-protecao-de-dados (LGPD)
- https://nextjs.org/docs/app/building-your-application/routing

---

## Dev 4 · S5 — Integração propriedade-comunidade no front-app

**Objetivo**
Integrar o fluxo de propriedade-comunidade do front-app ao backend: listagem de comunidades, vínculo ao cadastro de propriedade e solicitação de nova comunidade (CDU-20/CDU-21).

**O que fazer**
- Consumir o endpoint de listagem de comunidades ativas no formulário de propriedade
- Integrar criação/alteração de propriedade via API do backend
- Integrar o sub-fluxo de solicitação de nova comunidade (status pendente de aprovação)
- Exibir, no front-app, o status da comunidade (APROVADA/PENDENTE/REJEITADA)
- Validar a regra de exclusão de propriedade com dependências via API

**O que entregar**
- Cadastro/edição de propriedade do front-app integrado com comunidades vindo do backend
- Solicitação de nova comunidade enviada e persistida no backend

**Critérios de aceite**
- Lista de comunidades do formulário vem do backend (não de dados locais)
- Criar propriedade com comunidade selecionada persiste no backend
- Solicitar comunidade inexistente cria solicitação pendente
- Exclusão de propriedade com dependências é bloqueada com listagem das dependências
- Erros de rede/validação exibem feedback padrão do app

**Referências**
- https://nextjs.org/docs/app/guides
- https://react-hook-form.com/docs
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html

---

## Dev 5 · S5 — Endpoints de catálogo público (front-site)

**Objetivo**
Expor o catálogo público de sementes para o front-site com busca, filtros e detalhamento (RF-01/RF-04/RF-05 do site).

**O que fazer**
- Criar endpoint público de listagem de produtos com paginação
- Implementar filtros: nome popular, tipo, espécie, disponibilidade, comunidade e município
- Criar endpoint público de detalhe do produto (`/produtos/{id}`)
- Publicar apenas produtos com disponibilidade pública (venda/troca/doação)
- Integrar o front-site (página inicial de catálogo e página de produto) com os endpoints

**O que entregar**
- Catálogo público do front-site consumindo o backend (listagem + detalhe + filtros)

**Critérios de aceite**
- Listagem de produtos vem do backend com paginação
- Filtros aplicados no front-site refletem a consulta da API
- Detalhe do produto carregado via API
- Produtos `INDISPONIVEL` não aparecem no catálogo público
- Endpoints acessíveis sem autenticação

**Referências**
- https://docs.spring.io/spring-data/rest/reference/paging.html
- https://nextjs.org/docs/app/building-your-application/data-fetching
- https://nextjs.org/docs/app/building-your-application/routing

---

## Dev 6 · S5 — Integração de pedidos no front-app

**Objetivo**
Integrar o fluxo de pedidos do front-app ao backend: registrar, listar, detalhar, confirmar e cancelar pedido com atualização automática de estoque.

**O que fazer**
- Consumir os endpoints de pedidos do backend (`GET/POST /pedidos`, `PATCH /pedidos/{id}/confirmar|cancelar`)
- Integrar o formulário de novo pedido com seleção de sementes e validação de estoque
- Integrar listagem e detalhe do pedido com os status do backend
- Ações condicionais por status (confirmar/cancelar apenas se PENDENTE)
- Exibir no front-app a notificação gerada pós-pedido (CDU-26)

**O que entregar**
- Fluxo de pedidos do front-app funcionando via API do backend
- Atualização de estoque refletida após registrar/cancelar pedido

**Critérios de aceite**
- Criar pedido no front-app persiste no backend e atualiza o estoque
- Listagem/detalhe do pedido vêm da API do backend
- Confirmar pedido muda o status no backend para CONFIRMADO
- Cancelar pedido restaura o estoque e muda o status para CANCELADO
- Pedido com estoque insuficiente é bloqueado com mensagem de validação

**Referências**
- https://nextjs.org/docs/app/building-your-application/data-fetching
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- https://jwt.io/introduction

---

## Dev 7 · S5 — Geração de PDF/CSV e endpoints de relatórios

**Objetivo**
Gerar relatórios (RF-07) em PDF e CSV no backend e expor endpoints de relatório para o front-app e front-site.

**O que fazer**
- Implementar endpoints de relatório de estoque e de pedidos com filtros (período, tipo, semente, disponibilidade)
- Gerar arquivos PDF (iText/OpenPDF) com cabeçalho institucional, tabela e rodapé
- Gerar arquivos CSV (OpenCSV) com cabeçalhos em português
- Entregar os arquivos como download (`Content-Disposition: attachment`)
- Integrar o front-app/front-site ao endpoint (download e compartilhamento)

**O que entregar**
- Endpoints de relatórios retornando PDF e CSV válidos, respeitando os filtros

**Critérios de aceite**
- PDF baixado com estrutura correta (cabeçalho, tabela, rodapé e página X/Y)
- CSV baixado com cabeçalhos em português e separador correto
- Filtros aplicados refletem no conteúdo do relatório
- Sem filtros, o relatório considera todos os dados
- Sem dados para o filtro, retorna arquivo vazio/informativo (não quebra)
- Erro de geração retorna status code adequado

**Referências**
- https://openpdf.github.io/en/index.html
- http://opencsv.sourceforge.net
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html

---

## Dev 8 · S5 — Testes cross-domain (pedido→estoque, notificação→pedido)

**Objetivo**
Validar, com testes de integração (TestContainers), os fluxos que atravessam domínios: pedido que altera estoque e pedido que gera notificação.

**O que fazer**
- Criar testes de integração para pedido→estoque: registrar pedido diminui saldo; cancelar restaura saldo
- Criar testes de integração para notificação→pedido: concluir pedido cria notificação "não lida"
- Testar cenários de erro: estoque insuficiente, pedido cancelado duas vezes, exclusão com dependências
- Reutilizar as fixtures de dados da Fase 0 (dados reais via PostgreSQL em container)
- Validar rollback/consistência transacional nos fluxos entre domínios

**O que entregar**
- Suíte de testes cross-domain aprovada rodando no CI

**Critérios de aceite**
- Teste "registrar pedido → saldo do estoque diminui" passa
- Teste "cancelar pedido → saldo do estoque restaura" passa
- Teste "concluir pedido → notificação criada como não lida" passa
- Teste "estoque insuficiente → pedido rejeitado" passa
- Testes rodam contra PostgreSQL real via TestContainers

**Referências**
- https://java.testcontainers.org/getting-started/
- https://docs.spring.io/spring-framework/reference/testing/testcontext-framework.html
- https://junit.org/junit5/docs/current/user-guide/

---

# Semana 6

---

## Dev 1 · S6 — Otimização de queries e cache

**Objetivo**
Melhorar a performance de leitura do backend: eliminar N+1, criar índices adequados e aplicar cache em endpoints de alta leitura.

**O que fazer**
- Auditar as consultas JPA/JPQL e eliminar problemas de N+1 (fetch joins, entidade graphs)
- Criar as migrações Flyway com os índices faltantes (busca, filtros de catálogo e relatórios)
- Aplicar cache (Spring Cache) em endpoints de leitura de catálogo público e comunidades
- Medir ganho de performance com testes de carga (apoio de Dev 8)
- Revisar `idPropietario`/`idProduto` nas consultas de estoque e pedidos

**O que entregar**
- Consultas otimizadas, índices aplicados via Flyway e cache ativo nos endpoints de leitura

**Critérios de aceite**
- N+1 eliminado nos fluxos de listagem (verificado em logs de SQL)
- Migrações com índices aplicadas sem quebrar o schema existente
- Resposta de endpoints de catálogo tem latência menor após o cache
- Cache invalidado/atualizado após escrita (sem dados obsoletos)
- Nenhuma regressão nos testes existentes

**Referências**
- https://docs.spring.io/spring-data/jpa/reference/
- https://docs.spring.io/spring-framework/reference/integration/cache.html
- https://www.baeldung.com/hibernate-n-plus-1-problem

---

## Dev 2 · S6 — Testes de autenticação cross-domain

**Objetivo**
Validar, de ponta a ponta, o caminho autenticação front-app → API → banco: login, cadastro, refresh, recuperação e autorização por role.

**O que fazer**
- Criar/executar testes de integração do fluxo de auth completo contra o banco real (TestContainers)
- Validar cadastro de proprietário gerando registro e permitindo login
- Validar autorização por role (PROPRIETARIO, ADMIN) nos endpoints protegidos
- Validar expiração do token e renovação via refresh
- Validar o fluxo de recuperação de senha de ponta a ponta

**O que entregar**
- Fluxo completo de autenticação validado de ponta a ponta (front-app → API → banco)

**Critérios de aceite**
- Cadastro → login → acesso a endpoint protegido passa de ponta a ponta
- Acesso sem token ou com token expirado retorna 401
- Refresh token renova o access token corretamente
- Role PROPRIETARIO não acessa endpoint restrito a ADMIN (e vice-versa)
- Recuperação de senha gera fluxo de redefinição funcional

**Referências**
- https://docs.spring.io/spring-security/reference/servlet/authentication/index.html
- https://jwt.io/introduction
- https://java.testcontainers.org/getting-started/

---

## Dev 3 · S6 — Integração pessoa/proprietário no front-app

**Objetivo**
Integrar cadastro, login e perfil do proprietário no front-app com o backend: dados pessoais, endereço e alteração de dados via API.

**O que fazer**
- Integrar o cadastro de proprietário do front-app com o endpoint `/auth/cadastrar` (criação de conta + dados pessoais)
- Integrar o perfil (visualização e edição) com os endpoints de proprietário
- Unificar validações (CPF, RG, e-mail, telefono) com o backend — exibir erros de unicidade vindos da API
- Consumir dados do usuário logado via `/auth/me`
- Manter o fluxo de alteração de senha do front-app

**O que entregar**
- Cadastro e perfil do front-app integrados com o backend REST

**Critérios de aceite**
- Cadastro de novo proprietário cria conta no backend e redireciona para login
- Perfil exibe os dados vindos de `/auth/me`
- Editar perfil persiste as alterações no backend
- CPF/e-mail duplicados exibem erro retornado pela API no formulário
- Alteração de senha valida a senha atual antes de trocar

**Referências**
- https://nextjs.org/docs/app/building-your-application/data-fetching
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- https://react-hook-form.com/docs

---

## Dev 4 · S6 — Validação do fluxo completo de comunidade (front-site)

**Objetivo**
Validar no front-site o fluxo completo de comunidade: consulta pública, listagem de propriedades/comunidades e solicitação de nova comunidade.

**O que fazer**
- Validar a página pública de comunidades/propriedades contra os endpoints do backend
- Validar a consulta de produtores por comunidade e município
- Validar o formulário de solicitação de nova comunidade (CDU-21 no site)
- Validar o fluxo de aprovação/rejeição de comunidade no painel do site (quando aplicável)
- Registrar e corrigir divergências encontradas na validação

**O que entregar**
- Fluxo completo de comunidade validado no front-site contra o backend

**Critérios de aceite**
- Comunidades listadas no site vêm do backend
- Consulta por comunidade/município retorna dados corretos
- Solicitação de nova comunidade cria solicitação pendente no backend
- Aprovação/rejeição de solicitação atualiza o status no backend
- Divergências encontradas são corrigidas e revisitadas

**Referências**
- https://nextjs.org/docs/app/building-your-application/routing
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- https://testing-library.com/docs/react-testing-library/setup

---

## Dev 5 · S6 — Validação do fluxo estoque ↔ pedido

**Objetivo**
Validar o relacionamento entre estoque e pedido nos dois frontends: alterações de estoque refletindo nos pedidos e vice-versa.

**O que fazer**
- Validar a visualização de estoque (saldo, movimentações, disponibilidade) contra o backend
- Validar que registrar/cancelar pedido atualiza o saldo e o histórico de movimentações
- Validar o bloqueio de pedido com estoque insuficiente no frontend (feedback claro)
- Validar o ajuste manual de estoque (CDU-23) e o reflexo no saldo
- Corrigir divergências encontradas na validação

**O que entregar**
- Fluxo estoque ↔ pedido validado de ponta a ponta nos frontends

**Critérios de aceite**
- Saldo exibido no front igual ao saldo do backend
- Histórico de movimentações atualiza após um pedido registrado/cancelado
- Pedido acima do saldo é bloqueado no frontend e no backend
- Ajuste manual de estoque persiste e reflete na listagem
- Divergências corrigidas e validadas novamente

**Referências**
- https://nextjs.org/docs/app/building-your-application/data-fetching
- https://docs.spring.io/spring-data/jpa/reference/
- https://docs.spring.io/spring-framework/reference/data-access/transaction.html

---

## Dev 6 · S6 — Validação do fluxo de pedido completo

**Objetivo**
Validar o fluxo de pedido completo nos dois frontends: registrar, confirmar, cancelar, histórico e reflexo no estoque/notificação.

**O que fazer**
- Validar o fluxo de pedido no front-app e no front-site contra o backend
- Validar mudanças de status (PENDENTE → CONFIRMADO/CANCELADO) e suas regras
- Validar a integração pedido-estoque e pedido-notificação de ponta a ponta
- Validar filtros de histórico (período, tipo, semente, status)
- Corrigir divergências encontradas na validação

**O que entregar**
- Fluxo de pedido completo validado nos dois frontends

**Critérios de aceite**
- Registrar pedido no front persiste no backend com status PENDENTE
- Confirmar/cancelar pedido atualiza estoque e notificação corretamente
- Histórico com filtros retorna os dados esperados da API
- Pedido cancelado duas vezes retorna erro do backend (sem dupla restauração)
- Divergências corrigidas e validadas novamente

**Referências**
- https://nextjs.org/docs/app/building-your-application/data-fetching
- https://docs.spring.io/spring-boot/reference/data/sql.html
- https://docs.spring.io/spring-framework/reference/data-access/transaction.html

---

## Dev 7 · S6 — Validação de relatórios com dados reais

**Objetivo**
Validar a geração de relatórios (PDF/CSV) com dados reais do ambiente integrado e dos testes, garantindo consistência entre filtros e resultado.

**O que fazer**
- Validar os relatórios contra dados reais inseridos pelos testes de integração (Fase 3)
- Validar cada filtro (período, tipo, semente, disponibilidade, comunidade) no relatório de estoque e de pedidos
- Validar a prévia/contagem de registros no frontend versus o relatório final
- Validar o download e o compartilhamento nos dois frontends
- Corrigir divergências entre dado exibido, filtro aplicado e relatório gerado

**O que entregar**
- Relatórios com dados reais validados em todos os filtros nos dois frontends

**Critérios de aceite**
- Contagem de registros da prévia igual à do relatório gerado
- Cada filtro aplicado produz relatório consistente com o esperado
- PDF/CSV baixados refletem exatamente os dados filtrados
- Relatório sem dados desabilita exportação no frontend
- Divergências corrigidas e validadas novamente

**Referências**
- https://openpdf.github.io/en/index.html
- http://opencsv.sourceforge.net
- https://nextjs.org/docs/app/building-your-application/data-fetching

---

## Dev 8 · S6 — Testes de integração web (com Thales)

**Objetivo**
Automatizar testes de ponta a ponta navegando pelos frontends (front-app e front-site) contra o backend real, com condução de Thales.

**O que fazer**
- Setar o ambiente de testes web (backend + frontends rodando juntos)
- Automatizar fluxos e2e dos principais CDUs: auth, catálogo público, propriedade/comunidade, pedido, relatório e notificação
- Validar o comportamento offline/sessão e estados de erro nos frontends
- Registrar resultado dos testes e os ajustes necessários para a Fase 4
- Disponibilizar os testes para rodar no CI

**O que entregar**
- Suíte de testes web de integração rodando de ponta a ponta

**Critérios de aceite**
- Fluxos de auth (login, cadastro, logout) automatizados e passando
- Fluxos de catálogo público e detalhe de produto passando
- Fluxos de pedido (registrar, confirmar, cancelar) passando
- Fluxo de relatório gerando PDF/CSV passando
- Testes executáveis no CI contra o backend real (sem mock de dados)

**Referências**
- https://playwright.dev/docs/intro
- https://docs.cypress.io/guides/overview/why-cypress
- https://nextjs.org/docs/app/building-your-application/testing

---

*Documento de entrega — Projeto Semente Livre*  
*Versão 1.0 — Agosto 2026*