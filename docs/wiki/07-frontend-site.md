# 07. Frontend — Site Público (front-site)

**Stack:** Next.js (15/16) + TypeScript · React 19 · shadcn/ui + Tailwind CSS · API Routes  
Porta de desenvolvimento: **3001**

O `front-site` é o **site público** de vitrine do catálogo de sementes crioulas, com área de acesso para produtores/admin e persistência em PostgreSQL via API Routes.

---

## 7.1 Estrutura

```
front-site/src/
├── app/
│   ├── login, cadastro, recuperar-senha        # Área de autenticação
│   ├── dashboard/                              # Painel do produtor
│   │   ├── catalog, properties, plantings, techniques, pedidos, profile
│   ├── produto/[id]                            # Detalhes públicos da semente
│   ├── admin/                                  # Área administrativa
│   │   └── comunidades, registrations          # Aprovação de comunidades/cadastros
│   └── api/                                    # API Routes (mock/espelho do backend)
├── components/         # public-header, toast-provider, ui (shadcn)
├── hooks/              # use-admin-pending-count
└── lib/                # auth, store, types, utils
```

---

## 7.2 Funcionalidades

| Módulo | Descrição |
|---|---|
| **Vitrine pública** | Listagem de sementes do catálogo com detalhes por produto |
| **Dashboard produtor** | Catálogo, propriedades, **plantios** e **adubações**, **técnicas agroecológicas**, pedidos, perfil |
| **Área administrativa** | Aprovação/rejeição de **comunidades** e **solicitações de cadastro** de produtores |
| **Catálogo (dashboard)** | Gestão das sementes/mudas do produtor |
| **Pedidos** | Pedidos realizados pelo produtor no site |

---

## 7.3 API Routes (mock/espelho)

| Endpoint | Descrição |
|---|---|
| `/api/comunidades` | Listar comunidades |
| `/api/catalog` | Catálogo de sementes (com filtro por comunidade) |
| `/api/properties` | CRUD de propriedades |
| `/api/plantings` | CRUD de plantios |
| `/api/techniques` | Técnicas agroecológicas |
| `/api/fertilization` | Adubações/fertilização |
| `/api/pedidos` | Pedidos (com filtro por comunidade) |
| `/api/auth/login` | Login |
| `/api/registrations` | Solicitações de cadastro de produtores |
| `/api/notificacoes` | Notificações |
| `/api/proprietario` | Dados do proprietário |

> Essas rotas hoje operam sobre o `store.ts` local (dados em memória) e servem de espelho/contrato para o backend Spring Boot.

---

## 7.4 Interface

- Design system **shadcn/ui** sobre base **Base UI**.
- Estilização com **Tailwind CSS 4**.
- Componentes de notificação via **react-toastify**.

---

*[Voltar à Wiki](README.md) · [06. Frontend — PWA](06-frontend-pwa.md) · [08. Autenticação e Segurança](08-autenticacao-seguranca.md)*