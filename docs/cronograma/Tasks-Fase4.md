# Fase 4 — Cards de Atividades

**Projeto:** Semente Livre  
**Disciplina:** AAIFE3 — IF Sudeste MG Campus Rio Pomba  
**Fase:** 4 — Qualidade & Deploy (Semanas 7–8)  
**Equipe:** 8 desenvolvedores + 2 líderes

---

# Semana 7

---

## Dev 1 · S7 — Otimização Docker, multi-stage build

**Objetivo**
Otimizar a imagem Docker do backend para ambientes de homologação e produção, reduzindo tamanho, tempo de build e superfície de ataque.

**O que fazer**
- Reestruturar o `Dockerfile` em multi-stage (build com Maven/JDK + runtime enxuto com JRE)
- Usar imagem de runtime mínima (`eclipse-temurin` JRE ou `distroless`) sem ferramentas de build
- Configurar camadas de cache para acelerar builds recorrentes
- Revisar o `docker-compose.yml` para o par backend + PostgreSQL em produção
- Validar a imagem otimizada com os testes de carga de Dev 8

**O que entregar**
- `Dockerfile` multi-stage otimizado e imagem enxuta publicada/validada

**Critérios de aceite**
- Imagem final roda sem compilar código (apenas JRE)
- Tamanho da imagem reduzido em relação à atual (medido e reportado)
- Time-to-start da aplicação reduzido no ambiente de teste
- Build recorrente usa cache das camadas (`package` reutilizado)
- Aplicação sobe e passa nos health checks com a imagem otimizada

**Referências**
- https://docs.docker.com/build/building/multi-stage/
- https://github.com/docker-library/docs/tree/master/openjdk
- https://docs.docker.com/compose/compose-file/

---

## Dev 2 · S7 — Testes de segurança (pen test básico)

**Objetivo**
Executar uma rodada de testes de segurança na API e corrigir as vulnerabilidades básicas encontradas.

**O que fazer**
- Testar injeção (SQL/NoSQL) nos parâmetros de busca e filtros
- Validar o JWT: expiração, assinatura, claims e renovação de token
- Verificar exposição de dados sensíveis (CPF, RG, senhas) em respostas e logs
- Revisar headers HTTP de segurança (CSP, X-Content-Type-Options, HSTS) e configuração de CORS
- Testar rate limiting/brute force nos endpoints de login e refresh

**O que entregar**
- Relatório do pen test básico com as vulnerabilidades encontradas
- Correções de segurança aplicadas e validadas

**Critérios de aceite**
- Nenhuma falha crítica ou alta sem correção
- Tokens JWT com expiração respeitada e assinatura validada
- Dados sensíveis ausentes de respostas, logs e exceções
- Headers de segurança presentes nas respostas da API
- Login com tentativas repetidas é limitado/bloqueado conforme configuração

**Referências**
- https://owasp.org/www-project-top-ten/
- https://jwt.io/introduction
- https://docs.spring.io/spring-security/reference/servlet/exploits/

---

## Dev 3 · S7 — Bug fixes Pessoa/Proprietário

**Objetivo**
Corrigir os bugs conhecidos e débitos técnicos do domínio de pessoas (Pessoa, Usuario, Proprietario), incluindo validações e unicidade.

**O que fazer**
- Triar os bugs de Pessoa/Proprietário abertos (issues e feedback da Fase 3)
- Corrigir validações de CPF/RG/e-mail/telefone e regras de unicidade
- Corrigir DTOs e mapeamentos (dados sensíveis expostos indevidamente)
- Corrigir tratamento de erros e mensagens de validação do domínio
- Rodar e manter a suíte de testes unitários e de integração do domínio

**O que entregar**
- Bugs do domínio de pessoas resolvidos, revisados e testados

**Critérios de aceite**
- Unicidade de CPF/RG/e-mail validada sem duplicatas
- Validações exibem mensagens corretas (padrão global de erros)
- Nenhum dado sensível extra vaza nas respostas
- Testes unitários e de integração de pessoa/sistema passando
- Nenhuma regressão nos demais domínios

**Referências**
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- https://docs.spring.io/spring-data/jpa/reference/
- https://junit.org/junit5/docs/current/user-guide/

---

## Dev 4 · S7 — Bug fixes Comunidade/Propriedade

**Objetivo**
Corrigir os bugs conhecidos do domínio de território (Comunidade e Propriedade), incluindo dependências de exclusão e similaridade de nome.

**O que fazer**
- Triar os bugs de Comunidade/Propriedade abertos da Fase 3
- Corrigir a regra de exclusão com dependências (bloqueio + listagem das dependências)
- Corrigir a detecção de similalidade de nome de comunidade (CDU-21)
- Corrigir o fluxo de aprovação/rejeição de solicitação de comunidade (status)
- Rodar e manter a suíte de testes unitários e de integração do domínio

**O que entregar**
- Bugs do domínio de território resolvidos, revisados e testados

**Critérios de aceite**
- Excluir propriedade/comunidade com dependências bloqueado com lista de dependências
- Similaridade de nome de comunidade sugere alternativa antes de criar
- Aprovação/rejeição atualiza o status e os vínculos corretamente
- Testes unitários e de integração do domínio passando
- Nenhuma regressão nos demais domínios

**Referências**
- https://docs.spring.io/spring-data/jpa/reference/
- https://www.baeldung.com/jpa-joins
- https://junit.org/junit5/docs/current/user-guide/

---

## Dev 5 · S7 — Bug fixes Produto/Estoque

**Objetivo**
Corrigir os bugs conhecidos do domínio de catálogo (Produto e Estoque), incluindo upload de foto, disponibilidade e movimentações.

**O que fazer**
- Triar os bugs de Produto/Estoque abertos da Fase 3
- Corrigir upload e armazenamento de foto do produto (validação de arquivo, URL pública)
- Corrigir cálculo de saldo e histórico de movimentações do estoque
- Corrigir regras de disponibilidade (quantidade 0 → INDISPONIVEL) e precificação
- Rodar e manter a suíte de testes unitários e de integração do domínio

**O que entregar**
- Bugs do domínio de catálogo resolvidos, revisados e testados

**Critérios de aceite**
- Upload de foto funciona e a URL pública fica acessível
- Saldo e movimentações refletem corretamente as alterações de estoque
- Disponibilidade e preço validados conforme regras de negócio
- Testes unitários e de integração do domínio passando
- Nenhuma regressão nos demais domínios

**Referências**
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- https://docs.spring.io/spring-data/jpa/reference/
- https://junit.org/junit5/docs/current/user-guide/

---

## Dev 6 · S7 — Bug fixes Pedido/Itens

**Objetivo**
Corrigir os bugs conhecidos do domínio de operações (Pedido e Itens), incluindo regras de status, ciclo e restauração de estoque.

**O que fazer**
- Triar os bugs de Pedido/Itens abertos da Fase 3
- Corrigir transições de status (PENDENTE → CONFIRMADO/CANCELADO) e validações de ciclo
- Corrigir a restauração do estoque no cancelamento e a validação de estoque insuficiente
- Corrigir inconsistências entre item e produto no pedido
- Rodar e manter a suíte de testes unitários e de integração do domínio

**O que entregar**
- Bugs do domínio de operações resolvidos, revisados e testados

**Critérios de aceite**
- Transições de status válidas e inválidas tratadas corretamente
- Cancelamento restaura o estoque exatamente uma vez
- Pedido com estoque insuficiente bloqueado com mensagem correta
- Itens do pedido consistentes com os produtos/quantidades
- Testes unitários e de integração do domínio passando
- Nenhuma regressão nos demais domínios

**Referências**
- https://docs.spring.io/spring-framework/reference/data-access/transaction.html
- https://docs.spring.io/spring-data/jpa/reference/
- https://junit.org/junit5/docs/current/user-guide/

---

## Dev 7 · S7 — Bug fixes Notificação/Relatório

**Objetivo**
Corrigir os bugs conhecidos do domínio de alertas e relatórios (Notificacao e Relatorio), incluindo geração automática, filtros e formatos de exportação.

**O que fazer**
- Triar os bugs de Notificação/Relatório abertos da Fase 3
- Corrigir a geração automática de notificação pós-pedido (CDU-26)
- Corrigir filtros, contagem e conteúdo dos relatórios (PDF/CSV)
- Corrigir o marcador de lida/não lida das notificações
- Rodar e manter a suíte de testes unitários e de integração do domínio

**O que entregar**
- Bugs do domínio de alertas/relatórios resolvidos, revisados e testados

**Critérios de aceite**
- Notificação gerada automaticamente após cada novo pedido
- Relatórios com filtros corretos e conteúdo consistente
- Marcação de lida/não lida persistida corretamente
- Testes unitários e de integração do domínio passando
- Nenhuma regressão nos demais domínios

**Referências**
- https://openpdf.github.io/en/index.html
- http://opencsv.sourceforge.net
- https://junit.org/junit5/docs/current/user-guide/

---

## Dev 8 · S7 — Testes de carga e stress

**Objetivo**
Executar testes de carga e stress na API para mensurar limites, tempos de resposta e estabilidade sob concorrência, e validar a meta de cobertura de testes.

**O que fazer**
- Definir a baseline de performance dos endpoints críticos (auth, catálogo, estoque, pedidos, relatórios)
- Configurar os testes de carga (concorrência, duração, cenários) com ferramenta adequada (JMeter/k6)
- Executar testes de stress até encontrar o ponto de degradação/erro
- Coletar métricas (latência, throughput, taxa de erro, uso de recursos)
- Validar a meta mínima de cobertura de código (JaCoCo) da fase

**O que entregar**
- Relatório de testes de carga e stress com os resultados e recomendações
- Relatório de cobertura de código da fase

**Critérios de aceite**
- Endpoints críticos dentro do tempo de resposta definido sob carga nominal
- Taxa de erro sob stress documentada (com limites identificados)
- Comportamento sob concorrência estável (sem deadlock/estouro de conexão)
- Cobertura de código igual ou acima da meta definida na Fase 0
- Resultados e recomendações registrados para a Semana 8

**Referências**
- https://k6.io/docs/
- https://jmeter.apache.org/usermanual/
- https://www.eclemma.org/jacoco/trunk/doc/counters.html

---

# Semana 8

---

## Dev 1 · S8 — Deploy final, variáveis de ambiente

**Objetivo**
Executar o deploy final do backend em produção: variáveis de ambiente, secrets, orquestração e configurações de produção funcionando.

**O que fazer**
- Configurar variáveis de ambiente/secrets de produção (banco, JWT, armazenamento, CORS)
- Publicar a imagem otimizada e subir o ambiente de produção (Docker Compose/Plataforma)
- Configurar HTTPS, domínio e backups automáticos do banco
- Garantir o acesso a logs e ao monitoramento em produção
- Documentar o processo de deploy (runbook) no repositório

**O que entregar**
- Backend em produção funcionando via Docker, com secrets e backup configurados
- Runbook de deploy documentado

**Critérios de aceite**
- Aplicação acessível via HTTPS no ambiente de produção
- Segredos gerenciados por variáveis de ambiente (sem hardcode/commit)
- Health checks do ambiente de produção passando
- Backup automático configurado e testado (restauração)
- Deploy reproduzível a partir do runbook

**Referências**
- https://docs.docker.com/compose/compose-file/
- https://docs.spring.io/spring-boot/reference/features/external-config.html
- https://12factor.net/config

---

## Dev 2 · S8 — Validação JWT em produção

**Objetivo**
Validar o fluxo completo de autenticação JWT no ambiente de produção: login, cadastro, refresh, recuperação e expiração de sessão.

**O que fazer**
- Executar cenários de autenticação diretamente em produção (usuários de teste)
- Validar access/refresh token, expiração e renovação no ambiente real
- Validar a autorização por role nos endpoints protegidos
- Validar a recuperação de senha end-to-end em produção
- Registrar e corrigir qualquer divergência entre dev e produção

**O que entregar**
- Fluxo JWT validado em produção, com divergências corrigidas

**Critérios de aceite**
- Login e cadastro funcionando em produção
- Token expira no tempo configurado e refresh renova a sessão
- Rotas públicas acessíveis sem token; protegidas exigem token
- Roles restringem o acesso conforme esperado
- Recuperação de senha concluída de ponta a ponta

**Referências**
- https://jwt.io/introduction
- https://docs.spring.io/spring-security/reference/servlet/authentication/index.html
- https://docs.spring.io/spring-boot/reference/features/external-config.html

---

## Dev 3 · S8 — Validação de cadastros em produção

**Objetivo**
Validar o domínio de pessoas (Pessoa, Usuario, Proprietario) em produção: CRUD, unicidade, validações e integridade dos dados reais.

**O que fazer**
- Executar cenários de cadastro, consulta, alteração e exclusão em produção (dados de teste)
- Validar unicidade de CPF/RG/e-mail e mensagens de erro no ambiente real
- Validar o fluxo de cadastro via frontend contra o backend de produção
- Conferir a integridade dos dados e a aplicação das migrações em produção
- Registrar e corrigir divergências

**O que entregar**
- Domínio de pessoas validado em produção

**Critérios de aceite**
- CRUD de pessoa/proprietário funcionando em produção
- Unicidade validada (duplicatas rejeitadas) no ambiente real
- Dados sensíveis não expostos nas respostas públicas
- Migrações aplicadas sem erros em produção
- Divergências corrigidas e validadas novamente

**Referências**
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- https://docs.spring.io/spring-data/jpa/reference/
- https://www.gov.br/privacidade-e-protecao-de-dados (LGPD)

---

## Dev 4 · S8 — Validação de comunidades em produção

**Objetivo**
Validar o domínio de território (Comunidade e Propriedade) em produção: CRUD, status de comunidade, solicitação e aprovação/rejeição.

**O que fazer**
- Executar cenários de CRUD de comunidade e propriedade em produção
- Validar solicitação de nova comunidade e o fluxo de aprovação/rejeição (status)
- Validar exclusão com dependências e similaridade de nome no ambiente real
- Validar a integração com o frontend (front-site e front-app) em produção
- Registrar e corrigir divergências

**O que entregar**
- Domínio de território validado em produção

**Critérios de aceite**
- Comunidade criada com status PENDENTE e aprovada pelo admin
- Propriedade vinculada à comunidade corretamente
- Exclusão com dependências bloqueada em produção
- Similaridade de nome sugerida antes da criação
- Divergências corrigidas e validadas novamente

**Referências**
- https://docs.spring.io/spring-data/jpa/reference/
- https://nextjs.org/docs/app/building-your-application/routing
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html

---

## Dev 5 · S8 — Validação de estoque em produção

**Objetivo**
Validar o domínio de catálogo (Produto e Estoque) em produção: CRUD, saldo, movimentações, disponibilidade e upload de foto.

**O que fazer**
- Executar cenários de CRUD de produto e estoque em produção
- Validar upload de foto e URL pública da imagem no ambiente real
- Validar saldo, histórico de movimentações e recálculo pós-pedido
- Validar regra de disponibilidade (quantidade 0 → INDISPONIVEL)
- Registrar e corrigir divergências

**O que entregar**
- Domínio de catálogo validado em produção

**Critérios de aceite**
- CRUD de produto/estoque funcionando em produção
- Foto do produto enviada e acessível publicamente
- Saldo e movimentações corretos após pedidos e ajustes manuais
- Regra de disponibilidade aplicada corretamente
- Divergências corrigidas e validadas novamente

**Referências**
- https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html
- https://docs.spring.io/spring-data/jpa/reference/
- https://docs.spring.io/spring-boot/reference/features/external-config.html

---

## Dev 6 · S8 — Validação de pedidos em produção

**Objetivo**
Validar o domínio de operações (Pedido e Itens) em produção: ciclo completo de pedido com atualização de estoque e geração de notificação.

**O que fazer**
- Executar cenários do ciclo completo do pedido em produção (registrar, confirmar, cancelar)
- Validar a atualização do estoque após registrar e cancelar pedido
- Validar bloqueio por estoque insuficiente no ambiente real
- Validar a notificação gerada pós-pedido (CDU-26)
- Registrar e corrigir divergências

**O que entregar**
- Domínio de operações validado em produção

**Critérios de aceite**
- Pedido registrado com status PENDENTE em produção
- Confirmar pedido atualiza estoque; cancelar pedido restaura estoque
- Pedido com estoque insuficiente rejeitado com mensagem correta
- Notificação criada automaticamente após o pedido
- Divergências corrigidas e validadas novamente

**Referências**
- https://docs.spring.io/spring-framework/reference/data-access/transaction.html
- https://docs.spring.io/spring-data/jpa/reference/
- https://docs.spring.io/spring-boot/reference/data/sql.html

---

## Dev 7 · S8 — Validação de relatórios em produção

**Objetivo**
Validar o domínio de relatórios e notificações em produção: geração PDF/CSV, filtros, exportação e geração automática de notificações.

**O que fazer**
- Executar cenários de geração de relatórios em produção (estoque e pedidos)
- Validar filtros, contagem e conteúdo dos arquivos PDF/CSV no ambiente real
- Validar o download e compartilhamento nos frontends em produção
- Validar o marcador de lida/não lida das notificações
- Registrar e corrigir divergências

**O que entregar**
- Domínio de relatórios/notificações validado em produção

**Critérios de aceite**
- Relatório de estoque e pedidos gerados em produção
- Filtros aplicados corretamente no ambiente real
- PDF/CSV baixados corretamente pelos frontends
- Notificações criadas e marcadas como lidas/não lidas corretamente
- Divergências corrigidas e validadas novamente

**Referências**
- https://openpdf.github.io/en/index.html
- http://opencsv.sourceforge.net
- https://nextjs.org/docs/app/building-your-application/routing

---

## Dev 8 · S8 — Deploy completo, smoke tests

**Objetivo**
Concluir o deploy completo do sistema (backend + frontends), executar a suíte de smoke tests em produção e finalizar a documentação de API.

**O que fazer**
- Coordenar o deploy integrado (backend, front-app e front-site) em produção
- Executar os smoke tests dos fluxos críticos em produção (auth, catálogo, propriedade, pedido, relatório, notificação)
- Validar os health checks e o monitoramento do ambiente de produção
- Finalizar e revisar a documentação de API (Swagger/OpenAPI)
- Registrar o resultado final dos smoke tests e fechar a entrega

**O que entregar**
- Deploy completo em produção com smoke tests passando
- Documentação de API finalizada
- Resultados dos smoke tests registrados

**Critérios de aceite**
- Todos os sistemas acessíveis em produção via HTTPS
- Smoke tests dos fluxos críticos passando em produção
- Health checks e monitoramento ativos no ambiente real
- Documentação de API atualizada e acessível
- Checklist de deploy concluído e registrado

**Referências**
- https://docs.spring.io/springdoc/index.html
- https://playwright.dev/docs/intro
- https://docs.docker.com/compose/compose-file/

---

*Documento de entrega — Projeto Semente Livre*  
*Versão 1.0 — Agosto 2026*