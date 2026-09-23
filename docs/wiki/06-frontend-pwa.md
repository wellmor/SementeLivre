# 06. Frontend — PWA (front-app)

**Stack:** Next.js (15/16) + TypeScript · React 19 · `next-pwa` · Firebase (Auth, Firestore, Storage)

O `front-app` é a **PWA interna** usada pelos produtores rurais para gerir bancos de sementes. Porta de desenvolvimento: **3000**.

---

## 6.1 Estrutura

```
front-app/src/
├── app/
│   ├── (auth)/entrar, cadastrar, recuperar-senha   # Área de autenticação
│   └── (app)/dashboard, sementes, estoque (via hooks),
│             propriedades, pedidos, relatorios, notificacoes, perfil
├── components/        # Componentes de interface
├── context/           # AuthContext, NotificationContext
├── data/              # Dados mock (mockSementes, mockPedidos, mockMovimentacoes)
├── hooks/             # useOffline, useProperties, useOrders, useSeeds
├── lib/               # auth, db, storage, validators
└── types/             # notification, order, property, seed, stock, user
```

---

## 6.2 Funcionalidades

| Módulo | Descrição |
|---|---|
| **Autenticação** | Login, cadastro e recuperação de senha (Firebase Auth) |
| **Dashboard** | Visão geral de sementes, estoque e alertas |
| **Sementes** | CRUD de sementes com foto (Storage do Firebase) |
| **Estoque** | Controle de quantidades, preço e disponibilidade; histórico de movimentações |
| **Propriedades** | CRUD de propriedades ligadas à comunidade |
| **Pedidos** | CRUD de pedidos (venda/troca/doação) com atualização de estoque |
| **Relatórios** | Geração/exportação de relatórios em **PDF (jsPDF)** e **CSV (PapaParse)** |
| **Notificações** | Alerta ao registrar pedido, com estado lida/não lida |
| **Perfil** | Dados cadastrais do usuário |

---

## 6.3 Funcionamento Offline (PWA)

O `useOffline` hook detecta conectividade; o `next-pwa` registra o Service Worker responsável por:

1. Cachear assets estáticos (shell do app).
2. Manter a interface utilizável sem conexão.
3. Sincronizar as operações quando a rede é restabelecida.

> Atende ao requisito RNF-04 (suporte offline com sincronização).

---

## 6.4 Hooks Principais

| Hook | Responsabilidade |
|---|---|
| `useProperties` | CRUD de propriedades e comunidades |
| `useOrders` | CRUD de pedidos de sementes |
| `useSeeds` | CRUD de sementes/estoque |
| `useOffline` | Detecção de conectividade e sincronização |

---

## 6.5 Banco de Dados

- **Firestore** (coleções espelhando o modelo relacional).
- **Firebase Storage** para fotos de sementes/propriedades.
- **Firebase Auth** para autenticação.
- Em desenvolvimento, dados mock estão em `src/data/`.

---

*[Voltar à Wiki](README.md) · [05. API Backend](05-api-backend.md) · [07. Frontend — Site Público](07-frontend-site.md)*