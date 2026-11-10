# Projeto de Extensão Semente Livre
**IF Sudeste MG – Campus Rio Pomba | Curso de Ciência da Computação**  
Fevereiro de 2026

---

## Visão Geral

O projeto visa o desenvolvimento de sistemas para as necessidades tecnológicas da **comunidade Quilombola dos Coelhos** em Rio Pomba/MG, com foco no sistema **Semente Livre**: cadastro e gestão de bancos de sementes crioulas (sementes livres de transgênicos, repassadas de geração em geração).

**Palavras-chave:** Quilombolas, tecnologia, sementes crioulas, agricultura familiar.

---

## Sistemas a Desenvolver

| Sistema | Propósito | Público |
|---|---|---|
| **Aplicativo Semente Livre** | Gestão de bancos de sementes pelo produtor rural | Produtor rural familiar |
| **Site Semente Livre** | Acesso público aos bancos de sementes | Público interessado em sementes crioulas |

---

## Stack Tecnológica

| Componente | Tecnologia |
|---|---|
| App (mobile) | React Native + TypeScript |
| Site (web) | HTML, CSS, JavaScript (React.js recomendado) |
| Banco de dados | Firebase (ou outro gratuito em nuvem) |
| OS alvo (app) | Android 5.0+ (Lollipop) |
| Padrão arquitetural | MVP (Model-View-Presenter) |
| Autenticação | HTTPS/SSL, hashing + salting de senhas |

---

## Metodologia de Desenvolvimento

- Metodologia ágil inspirada em **SCRUM**
- Sprints quinzenais com encontros presenciais
- Uso de IA permitido conforme orientação dos docentes
- Testes: caixa branca, caixa preta, carga e stress, com suporte de ferramentas e IA

---

## Fases do Projeto

| Fase | Disciplina | Período |
|---|---|---|
| Visita técnica + Engenharia de Requisitos | Linguagens de Programação | 2025 |
| Desenvolvimento do app e site | AAIFE3 | 1º sem. 2026 |
| Testes, ajustes, treinamento, implantação | AAIFE4 | 2º sem. 2026 |

### Atividades previstas (AAIFE3 e AAIFE4)

1. Atualização do documento de requisitos e diagrama de caso de uso
2. Diagrama de atividades
3. Diagrama de classe (nível de análise)
4. Diagrama de classe (nível de projeto)
5. Diagrama de implantação
6. Diagramas de sequência
7. Prototipação das interfaces
8. Teste de aceitação com a comunidade
9. Implementação do app e site
10. Revisão da modelagem conforme implementação
11. Testes de unidade
12. Testes funcionais
13. Testes de carga e stress
14. Implantação no host adequado
15. Manuais de uso
16. Treinamento dos usuários da comunidade
17. Correção de erros e atualizações
18. Uso de ferramentas de gerência de projetos e configuração

---

# APÊNDICE A — Aplicativo Semente Livre

> Documento de Requisitos v6.0 (06/04/2026) — padrão IEEE/ANSI 830-1998

## Usuários

Produtores rurais familiares com base agroecológica, familiarizados com smartphones.

## Requisitos Funcionais

### [RF-01] Manter Proprietário
CRUD completo dos dados do proprietário.

**Campos:** nome*, RG**, CPF**, telefone, email, endereço completo*  
`* obrigatório | ** unique`

- Exclusão em cascata de todas as dependências
- ID gerado automaticamente pelo SGBD

---

### [RF-02] Manter Propriedade
CRUD completo dos dados da propriedade rural.

**Campos:** nome*, tamanho (hectares), endereço completo*, comunidade  
- Um proprietário pode ter mais de uma propriedade

---

### [RF-03] Manter Semente
CRUD completo das sementes/mudas.

**Campos:**
- nome popular*
- descrição
- nome científico
- foto*
- quantidade
- peso
- tipo* (hortaliça | frutífera | forrageira | cereal | leguminosa | outras)
- preço*
- forma de precificação* (ex: R$10,00/kg, R$0,10/unidade)
- disponibilidade* (para troca | venda | doação | indisponível)
- data de inclusão *(auto)*
- data da última alteração *(auto)*

- Semente vinculada ao proprietário
- Exclusão não apaga pedidos já realizados

---

### [RF-05] Manter Pedidos
CRUD dos pedidos de saída de sementes.

**Campos:** quantidade, semente/muda*, tipo* (venda | troca | doação), nome e contato do recebedor, data do pedido*, atualização do estoque*

---

### [RF-06] Autenticação e Acesso
- Login com email e senha
- Cadastro de novos usuários
- Recuperação de senha
- Sessão expira após 30 min de inatividade

---

### [RF-07] Gerar Relatórios
- Listagem de sementes e pedidos
- Filtros: período, semente, tipo de pedido
- Exportação em **PDF** e **CSV**

---

### [RF-08] Gerar Notificação
- Notificação ao registrar novo pedido
- Histórico de notificações (lidas/não lidas)
- Clique no alerta abre detalhes do pedido

---

## Requisitos Não Funcionais

### Usabilidade

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-01 | Operação em smartphones online | Essencial |
| RNF-02 | Android 5.0+ (Lollipop) | Essencial |
| RNF-03 | Interface amigável, paleta de cores do IF, splash screen, tela "sobre", help em PT-BR, acessibilidade ARIA | Essencial |

**Requisitos ARIA:**
- `aria-label` / `aria-labelledby` em todos os campos e botões
- `aria-live="polite"` para mensagens de status dinâmicas
- `role="tablist"`, `role="tab"`, `role="tabpanel"` para navegação complexa
- `aria-invalid="true"` + `aria-describedby` em campos com erro

### Software

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-04 | Persistência em nuvem (Firebase ou similar), suporte offline com sincronização | Essencial |
| RNF-05 | React Native + TypeScript, padrão OO | Essencial |

### Desempenho

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-06 | Execução ágil das operações | Importante |

### Confiabilidade

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-07 | Uptime ≥ 99,5%, backup automático diário, mecanismo de recuperação | Importante |

### Segurança

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-08 | HTTPS/SSL, senhas com hashing+salting, sessão expira em 30 min | Essencial |
| RNF-09 | Conformidade com LGPD | Essencial |

### Manutenção

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-10 | Código modular, padrão MVP, curva de aprendizado ≤ 2 semanas | Importante |

---

## Casos de Uso — Aplicativo

### [CDU-01] Alterar Dados Cadastrais do Proprietário
**Prioridade:** Essencial | **RF:** RF-01  
**Pré-condição:** usuário logado  
**Pós-condição:** dados atualizados no banco  

**Fluxo principal:**
1. Usuário acessa "Alterar Dados Cadastrais"
2. Sistema exibe dados atuais no formulário
3. Usuário modifica os campos desejados
4. Usuário clica em "Salvar"
5. Sistema valida e atualiza no banco
6. Sistema exibe confirmação

**Exceção:** alteração de senha exige confirmação da senha atual

---

### [CDU-02] Cadastrar Propriedade
**Prioridade:** Essencial | **RF:** RF-02  
**Pré-condição:** usuário logado  

**Fluxo principal:**
1. Usuário acessa "Cadastrar Propriedade"
2. Sistema exibe formulário (nome, tamanho, endereço, comunidade)
3. Usuário preenche e clica em "Cadastrar"
4. Sistema valida e cadastra
5. Sistema exibe confirmação

**Exceção:** erro ao salvar na nuvem exibe mensagem de erro

---

### [CDU-03] Alterar Propriedade
**Prioridade:** Essencial | **RF:** RF-02  
**Pré-condição:** usuário logado, propriedade cadastrada  

**Fluxo principal:**
1. Usuário acessa "Alterar Propriedade"
2. Sistema lista propriedades
3. Usuário seleciona e edita
4. Clica em "Salvar Alterações"
5. Sistema valida, atualiza e confirma

**Exceção:** campos obrigatórios vazios bloqueiam alteração

---

### [CDU-04] Excluir Propriedade
**Prioridade:** Essencial | **RF:** RF-02  
**Pré-condição:** usuário logado, sem dependências vinculadas  

**Fluxo principal:**
1. Usuário acessa "Excluir Propriedade"
2. Sistema lista propriedades
3. Usuário seleciona e confirma exclusão
4. Sistema remove e confirma

**Exceção:** propriedade com dependências tem exclusão bloqueada com listagem das dependências

---

### [CDU-05] Gerar Relatórios
**Prioridade:** Essencial | **RF:** RF-07  
**Pré-condição:** usuário logado, dados cadastrados  

**Fluxo principal:**
1. Usuário acessa "Gerar Relatórios"
2. Sistema invoca `CDU-25 Selecionar Relatório`
3. Sistema processa e exibe relatório
4. Sistema invoca `CDU-17 Exportar Relatório`

**Exceção:** sem dados para o filtro exibe mensagem informativa

---

### [CDU-06] Autenticar Proprietário
**Prioridade:** Essencial | **RF:** RF-06  
**Pré-condição:** cadastro prévio existente  

**Fluxo principal:**
1. Usuário acessa tela de login
2. Insere email e senha
3. Clica em "Entrar"
4. Sistema valida com servidor
5. Sessão iniciada, redireciona para tela principal

**Exceções:** credenciais inválidas → "Email ou senha incorretos" | falha de conexão → "Verifique sua conexão"

---

### [CDU-07] Recuperar Senha
**Prioridade:** Essencial | **RF:** RF-06  

**Fluxo principal:**
1. Usuário clica em "Esqueceu sua senha?"
2. Sistema solicita email cadastrado
3. Usuário insere email e confirma
4. Sistema envia link/código de redefinição
5. Sistema confirma envio

**Exceções:** email não encontrado | falha no servidor de email

---

### [CDU-08] Cadastrar Semente
**Prioridade:** Essencial | **RF:** RF-03  
**Pré-condição:** usuário logado, ao menos uma propriedade cadastrada  

**Fluxo principal:**
1. Usuário acessa "Cadastrar Semente"
2. Sistema exibe formulário completo
3. Usuário preenche campos obrigatórios
4. Clica em "Cadastrar"
5. Sistema valida, registra e confirma

**Exceções:** campo obrigatório vazio | falha no upload de foto | sem propriedade cadastrada

---

### [CDU-09] Alterar Semente
**Prioridade:** Essencial | **RF:** RF-03  

**Fluxo principal:**
1. Usuário acessa "Alterar Semente"
2. Sistema lista sementes
3. Usuário seleciona e edita
4. Clica em "Salvar Alterações"
5. Sistema valida, atualiza (inclusive data de alteração) e confirma

**Exceções:** campos obrigatórios vazios | falha ao salvar na nuvem

---

### [CDU-10] Excluir Semente
**Prioridade:** Essencial | **RF:** RF-03  

**Fluxo principal:**
1. Usuário acessa "Excluir Semente"
2. Sistema lista sementes
3. Usuário seleciona e confirma
4. Sistema remove semente, preserva pedidos anteriores

**Exceções:** cancelamento aborta operação | falha no servidor

---

### [CDU-11] Consultar Semente
**Prioridade:** Essencial | **RF:** RF-03  

**Fluxo principal:**
1. Usuário acessa "Consultar Sementes"
2. Sistema exibe listagem completa
3. Usuário aplica filtros (disponibilidade, nome popular, nome científico)
4. Sistema atualiza listagem
5. Usuário seleciona semente para ver detalhes

**Exceções:** sem sementes cadastradas | nenhum resultado para o filtro

---

### [CDU-12] Consultar Pedidos
**Prioridade:** Essencial | **RF:** RF-05  

**Fluxo principal:**
1. Usuário acessa "Consultar Pedidos"
2. Sistema exibe histórico completo
3. Usuário aplica filtros (período, semente, tipo)
4. Sistema atualiza listagem
5. Usuário seleciona pedido para ver detalhes

**Exceções:** sem pedidos registrados | nenhum resultado para o filtro

---

### [CDU-13] Alterar Pedido
**Prioridade:** Importante | **RF:** RF-05  

**Fluxo principal:**
1. Usuário acessa histórico e seleciona pedido
2. Sistema exibe formulário com dados atuais
3. Usuário modifica e clica em "Salvar Alterações"
4. Sistema valida, atualiza pedido e recalcula estoque

**Exceções:** quantidade maior que estoque bloqueia alteração | campos obrigatórios vazios

---

### [CDU-14] Excluir Pedido
**Prioridade:** Importante | **RF:** RF-05  

**Fluxo principal:**
1. Usuário seleciona pedido e aciona exclusão
2. Sistema confirma: "Estoque da semente será restaurado"
3. Usuário confirma
4. Sistema remove pedido e restaura estoque

**Exceções:** cancelamento aborta operação | falha no servidor

---

### [CDU-15] Cadastrar Proprietário (novo usuário)
**Prioridade:** Essencial | **RF:** RF-06  
**Pré-condição:** CPF e e-mail não cadastrados  

**Fluxo principal:**
1. Usuário acessa tela de cadastro
2. Preenche: nome, RG, CPF, telefone, e-mail, endereço, senha
3. Clica em "Cadastrar"
4. Sistema valida (unicidade CPF/e-mail, força da senha)
5. Sistema cria conta com senha em hashing+salting
6. Redireciona para tela de login

**Exceções:** CPF duplicado | e-mail já cadastrado | senha fraca

---

### [CDU-16] Consultar Propriedade
**Prioridade:** Essencial | **RF:** RF-02  

**Fluxo principal:**
1. Usuário acessa "Consultar Propriedade"
2. Sistema lista propriedades
3. Usuário seleciona para ver detalhes (nome, tamanho, endereço, comunidade)

**Exceção:** sem propriedades → atalho para cadastro

---

### [CDU-17] Exportar Relatório
**Prioridade:** Essencial | **RF:** RF-07  

**Fluxo principal:**
1. Usuário acessa "Gerar Relatórios"
2. Seleciona tipo e filtros
3. Clica em "Gerar"
4. Sistema exibe prévia
5. Usuário escolhe "Exportar PDF" ou "Exportar CSV"
6. Sistema gera e disponibiliza para download/compartilhamento

**Exceções:** sem dados → desabilita exportação | erro técnico → mensagem de erro

---

### [CDU-18] Visualizar Estoque
**Prioridade:** Essencial | **RF:** RF-03, RF-05  

**Fluxo principal:**
1. Usuário acessa tela de Estoque ou Painel Inicial
2. Sistema exibe resumo: sementes, quantidades, disponibilidade
3. Usuário seleciona semente para ver histórico de movimentações

**Exceção:** banco vazio → atalho para cadastro

---

### [CDU-19] Sair do Sistema
**Prioridade:** Essencial | **RF:** RF-06  

**Fluxo principal:**
1. Usuário clica em "Sair"
2. Sistema solicita confirmação
3. Usuário confirma
4. Sistema invalida sessão, limpa dados temporários
5. Redireciona para tela de login

**Exceção:** falha ao comunicar com servidor → força limpeza local e redireciona mesmo assim

---

### [CDU-20] Selecionar Comunidade
**Prioridade:** Essencial | **RF:** RF-02  

**Fluxo principal:**
1. No formulário de propriedade, usuário interage com campo "Comunidade"
2. Sistema exibe listagem de comunidades cadastradas
3. Usuário busca e seleciona
4. Campo atualizado no formulário

**Exceção:** comunidade não encontrada → aciona `CDU-21` | falha de conexão → mensagem de erro

---

### [CDU-21] Solicitar Comunidade
**Prioridade:** Importante | **RF:** RF-02  

**Fluxo principal:**
1. Usuário aciona "Solicitar nova comunidade"
2. Sistema exibe formulário (nome da comunidade, município)
3. Usuário preenche e clica em "Enviar solicitação"
4. Sistema registra como "pendente de aprovação" e vincula provisoriamente à propriedade
5. Exibe confirmação

**Exceções:** nome vazio bloqueia envio | nome similar a existente → sistema sugere alternativa antes de criar

---

### [CDU-22] Cadastrar Estoque
**Prioridade:** Essencial | **RF:** RF-03  
*Ocorre integrado ao cadastro da semente*  

**Fluxo principal:**
1. Durante cadastro da semente, usuário insere quantidade inicial e unidade de medida
2. Clica em "Cadastrar"
3. Sistema salva semente e registra estoque separadamente no banco

**Exceções:** valor negativo bloqueia cadastro | campo vazio → estoque inicial = 0, status = "indisponível"

---

### [CDU-23] Alterar Estoque
**Prioridade:** Essencial | **RF:** RF-03, RF-05  
*Acionado pelo usuário (manual) ou pelo sistema (pós-pedido)*  

**Fluxo principal:**
1. Estoque atual é recuperado
2. Valor da alteração (adição ou subtração) é inserido
3. Sistema recalcula e atualiza saldo

**Exceção:** subtração maior que estoque atual bloqueia a ação

---

### [CDU-24] Excluir Estoque
**Prioridade:** Essencial | **RF:** RF-03  

**Fluxo principal:**
1. Usuário acessa gestão de estoque e aciona exclusão
2. Sistema solicita confirmação
3. Usuário confirma
4. Sistema zera quantidade e define status como "indisponível"

**Exceção:** cancelamento aborta operação

---

### [CDU-25] Selecionar Relatório
**Prioridade:** Essencial | **RF:** RF-07  

**Fluxo principal:**
1. Sistema lista tipos de relatório (sementes, pedidos)
2. Usuário seleciona tipo
3. Sistema exibe filtros (período, semente, tipo de pedido)
4. Usuário preenche e confirma

**Exceção:** sem filtros → relatório completo com todos os dados

---

### [CDU-26] Gerar Notificação de Pedido
**Prioridade:** Essencial | **RF:** RF-05, RF-08  
*Acionado pelo sistema ao concluir um pedido*  

**Fluxo principal:**
1. Pedido finalizado e salvo
2. Sistema invoca `CDU-23 Alterar Estoque`
3. Sistema gera notificação com detalhes do pedido
4. Alerta visual exibido ao usuário
5. Clique abre detalhes do pedido

**Exceção:** sem clique → notificação armazenada como "não lida"

---

# APÊNDICE B — Site Semente Livre

> Documento de Requisitos v3.0 (25/11/2025) — padrão IEEE/ANSI 830-1998

## Usuários

Pessoas interessadas em sementes crioulas para consulta, compra, troca ou doação.

## Visão Geral

Site público de consulta e contato, funcionando como vitrine/catálogo dos bancos de sementes dos produtores rurais, podendo expandir para comércio eletrônico, escambo ou doação.

## Requisitos Funcionais

### [RF-01] Listar Sementes
Vitrine pública de todas as sementes/mudas disponíveis, exibindo foto, nome popular e disponibilidade.  
**Prioridade:** Essencial

---

### [RF-02] Listar Produtor
Página de perfil do produtor com breve histórico e lista de sementes cultivadas.  
**Prioridade:** Essencial

---

### [RF-03] Listar Propriedades
Página de perfil da propriedade com histórico e sementes cultivadas.  
**Prioridade:** Essencial

---

### [RF-04] Selecionar Exibição (Busca/Filtros)
Ferramenta de busca com filtros por: nome da semente/muda, município, comunidade, nome do produtor, tipo de pedido (venda/troca), ou mapa georreferenciado.  
**Prioridade:** Essencial

---

### [RF-05] Visualizar Detalhes
Ao clicar em item do catálogo, abre visão detalhada com todos os dados pertinentes.  
**Prioridade:** Essencial

---

### [RF-06] Imprimir Relatórios
Geração e impressão de listagem em PDF das informações exibidas na tela.  
**Prioridade:** Essencial

---

### [RF-07] Comprar / Trocar / Solicitar Doação
Mecanismo de contato (formulário ou exibição de WhatsApp/e-mail do produtor, se autorizado) para iniciar negociação, em interface padrão de comércio eletrônico.  
**Prioridade:** Essencial

---

### [RF-08] Mapa de Sementes (Geolocalização)
Mapa interativo com localização aproximada (por comunidade ou município) das propriedades com bancos de sementes ativos.  
**Prioridade:** Desejável

---

## Requisitos Não Funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RNF-01 | Design responsivo (desktop, tablet, mobile) | Essencial |
| RNF-02 | Consumir mesmo banco de dados do app (Firebase), dados sempre sincronizados | Essencial |
| RNF-03 | Acessibilidade WCAG (alto contraste, alt text, leitores de tela), paleta de cores do IF | Essencial |
| RNF-04 | HTML, CSS, JavaScript (React.js recomendado) | Essencial |
| RNF-05 | Performance: cache e otimização de imagens | Importante |
| RNF-06 | Conformidade com LGPD | Essencial |

---

## Casos de Uso — Site

### [CDU-01] Listar Sementes
Listagem pública com filtros por: proprietário, propriedade, município, região, estado, data de inclusão, data de última alteração. Clique abre modal de detalhamento. Ambas as telas com opção de impressão em PDF.  
**Prioridade:** Essencial

---

### [CDU-02] Listar Proprietários
Listagem com filtros por: proprietário, propriedade, município, região, estado. Clique abre modal de detalhamento com opção de impressão em PDF.  
**Prioridade:** Essencial

---

### [CDU-03] Listar Propriedades
Listagem com filtros por: proprietário, propriedade, município, região, estado. Clique abre modal de detalhamento com opção de impressão em PDF.  
**Prioridade:** Essencial

---

### [CDU-04] Pedido de Compra / Troca / Doação
Usuário seleciona semente, quantidade e forma (compra, troca ou doação) e registra o pedido. Sistema confirma a operação.  
**Prioridade:** Essencial | **RF:** RF-07

---

## Referências

- ALTIERI, Miguel. *Agroecologia: bases científicas para uma agricultura sustentável.* Guaíba: Agropecuária, 2002.
- PRESSMAN, Roger; MAXIM, Bruce. *Engenharia de Software* – 8ª Edição. 2016.
- IEEE/ANSI 830-1998 – Software Requirements Specifications.