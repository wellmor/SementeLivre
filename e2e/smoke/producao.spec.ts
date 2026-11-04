import { APIRequestContext, APIResponse, expect, request, test } from '@playwright/test';
import { gerarCpf, unico } from '../support/api';

/**
 * Smoke tests dos fluxos críticos em produção (#138): auth, catálogo,
 * propriedade, pedido, relatório e notificação, mais health check, HTTPS e os
 * dois frontends. Rodam em série sobre uma conta "Smoke" criada aqui e apagada
 * no final, junto com tudo o que ela gerou.
 */

const API_URL = process.env.API_URL ?? 'http://localhost:8080';
const APP_URL = process.env.APP_URL ?? 'http://localhost:3000';
const SITE_URL = process.env.SITE_URL ?? 'http://localhost:3001';
// A cópia do pedido vai para este e-mail; em produção, use uma caixa da equipe
const EMAIL_COMPRADOR = process.env.SMOKE_EMAIL_COMPRADOR ?? 'smoke-comprador@example.com';

const SENHA = 'SmokeTest123';
const ID = unico();

test.describe.configure({ mode: 'serial' });

interface Estado {
  email: string;
  produtorId: string;
  accessToken: string;
  refreshToken: string;
  produtoId: string;
  estoqueId: string;
  nomeOferta: string;
  pedidoId: string;
}
const s: Partial<Estado> = {};

async function api(token?: string): Promise<APIRequestContext> {
  return request.newContext({
    baseURL: API_URL,
    extraHTTPHeaders: token ? { Authorization: `Bearer ${token}` } : {},
  });
}

async function corpo(res: APIResponse, oQue: string, status: number) {
  expect(res.status(), `${oQue}: ${await res.text()}`).toBe(status);
  return res.status() === 204 ? null : res.json();
}

/** http://host redireciona para https://host (Caddy). Só faz sentido quando a URL é https. */
async function confereRedirecionamentoHttps(url: string) {
  const ctx = await request.newContext();
  const res = await ctx.get(url.replace(/^https:/, 'http:'), { maxRedirects: 0 });
  expect([301, 302, 307, 308], `http de ${url} deve redirecionar`).toContain(res.status());
  expect(res.headers()['location']).toMatch(/^https:\/\//);
  await ctx.dispose();
}

test('API no ar: health, liveness e readiness UP', async () => {
  const ctx = await api();
  for (const caminho of ['/actuator/health', '/actuator/health/liveness', '/actuator/health/readiness']) {
    const saude = await corpo(await ctx.get(caminho), caminho, 200);
    expect(saude.status, caminho).toBe('UP');
  }
  await ctx.dispose();
});

test('HTTPS: API, app e site respondem em https e redirecionam o http', async () => {
  const urls = [API_URL, APP_URL, SITE_URL].filter((u) => u.startsWith('https://'));
  test.skip(urls.length === 0, 'URLs em http (ambiente local): verificação de HTTPS só em produção');
  for (const url of urls) {
    await confereRedirecionamentoHttps(url);
  }
});

test('auth: cadastro, login, perfil e refresh do token', async () => {
  const ctx = await api();
  s.email = `smoke-${ID}@example.com`;
  const cadastro = await corpo(await ctx.post('/auth/cadastrar', {
    data: {
      nome: `Smoke Test ${ID}`, tipoDocumento: 'CPF', documento: gerarCpf(), rg: `SMK${ID.slice(-8)}`,
      email: s.email, senha: SENHA, exibirNoSitePublico: true,
      endereco: { logradouro: 'Rua do Smoke Test', numero: '1', municipio: 'Rio Pomba', uf: 'MG', cep: '36180000' },
    },
  }), 'cadastro', 201);
  s.produtorId = cadastro.id;

  const tokens = await corpo(await ctx.post('/auth/login', { data: { email: s.email, senha: SENHA } }), 'login', 200);
  s.accessToken = tokens.accessToken;
  s.refreshToken = tokens.refreshToken;

  const autenticado = await api(s.accessToken);
  const perfil = await corpo(await autenticado.get('/auth/me'), 'perfil', 200);
  expect(perfil.email).toBe(s.email);
  expect(perfil.roles).toContain('ROLE_PROPRIETARIO');

  const renovado = await corpo(await ctx.post('/auth/refresh', { data: { refreshToken: s.refreshToken } }), 'refresh', 200);
  expect(renovado.accessToken).toBeTruthy();
  s.accessToken = renovado.accessToken;
  s.refreshToken = renovado.refreshToken;

  // Sem token, rota protegida responde 401
  expect((await ctx.get('/auth/me')).status()).toBe(401);
  await Promise.all([ctx.dispose(), autenticado.dispose()]);
});

test('propriedade: cadastro numa comunidade ativa', async () => {
  const ctx = await api(s.accessToken);
  const comunidades: Array<{ id: string; nome: string; status: string }> =
    await corpo(await ctx.get('/api/comunidades'), 'comunidades', 200);
  const ativa = comunidades.find((c) => c.status === 'ATIVA');
  expect(ativa, 'precisa existir ao menos uma comunidade ativa').toBeTruthy();

  const nome = `Sítio Smoke ${ID}`;
  await corpo(await ctx.post('/api/propriedades', {
    data: {
      nome, tamanhoHectares: 1, proprietarioId: s.produtorId, comunidadeId: ativa!.id,
      endereco: { logradouro: 'Estrada do Smoke', numero: '1', municipio: 'Rio Pomba', uf: 'MG', cep: '36180000' },
    },
  }), 'propriedade', 200);

  const minhas: Array<{ nome: string }> =
    await corpo(await ctx.get(`/api/propriedades?proprietarioId=${s.produtorId}`), 'listar propriedades', 200);
  expect(minhas.map((p) => p.nome)).toContain(nome);
  await ctx.dispose();
});

test('catálogo: a oferta cadastrada aparece no catálogo público', async () => {
  const ctx = await api(s.accessToken);
  s.nomeOferta = `Feijão Smoke ${ID}`;
  const produto = await corpo(await ctx.post('/produtos', {
    data: {
      nomePopular: s.nomeOferta, urlFoto: '/uploads/produtos/smoke.png',
      tipo: 'LEGUMINOSA', especie: 'FEIJAO', formato: 'SEMENTE',
    },
  }), 'produto', 200);
  s.produtoId = produto.id;

  const estoque = await corpo(await ctx.post('/estoques', {
    data: {
      proprietarioId: s.produtorId, produtoId: s.produtoId, preco: 10, quantidade: 20,
      tipoPesagem: 'KG', disponibilidade: 'PARA_VENDA', tipoMovimentacao: 'ENTRADA', aceitaNegociacao: false,
    },
  }), 'estoque', 200);
  s.estoqueId = estoque.id;

  const publico = await api();
  // O catálogo é paginado (Page do Spring): os itens vêm em "content"
  const catalogo: { content: Array<{ nomePopular: string; estoqueId: string }> } = await corpo(
    await publico.get(`/catalogo/produtos?nomePopular=${encodeURIComponent(s.nomeOferta)}`), 'catálogo', 200);
  expect(catalogo.content.map((c) => c.estoqueId)).toContain(s.estoqueId);

  // Perfil público do produtor sem dado pessoal (LGPD)
  const perfil = await corpo(await publico.get(`/produtores/${s.produtorId}`), 'perfil público', 200);
  for (const campo of ['email', 'documento', 'rg', 'telefone']) {
    expect(perfil, `perfil público não expõe ${campo}`).not.toHaveProperty(campo);
  }
  await Promise.all([ctx.dispose(), publico.dispose()]);
});

test('pedido: pedido do site público, notificação ao produtor e aceite com baixa de estoque', async () => {
  const publico = await api();
  const pedido = await corpo(await publico.post('/catalogo/pedidos', {
    data: { estoqueId: s.estoqueId, quantidade: 3, nome: 'Comprador Smoke', email: EMAIL_COMPRADOR, telefone: '32999990000' },
  }), 'pedido público', 201);
  s.pedidoId = pedido.id;
  expect(pedido.status).toBe('PENDENTE');

  const ctx = await api(s.accessToken);
  const notificacoes: Array<{ pedidoRelacionadoId?: string; lida: boolean }> =
    await corpo(await ctx.get('/notificacoes'), 'notificações', 200);
  expect(notificacoes.some((n) => n.pedidoRelacionadoId === s.pedidoId), 'produtor é avisado do pedido').toBeTruthy();
  const naoLidas = await corpo(await ctx.get('/notificacoes/nao-lidas/contagem'), 'contagem', 200);
  expect(naoLidas.total).toBeGreaterThan(0);

  const confirmado = await corpo(await ctx.patch(`/pedidos/${s.pedidoId}/confirmar`), 'aceitar pedido', 200);
  expect(confirmado.status).toBe('CONFIRMADO');
  const estoque = await corpo(await ctx.get(`/estoques/${s.estoqueId}`), 'estoque', 200);
  expect(estoque.quantidade).toBe(17);
  await Promise.all([ctx.dispose(), publico.dispose()]);
});

test('relatórios: estoque em PDF e pedidos em CSV', async () => {
  const ctx = await api(s.accessToken);
  const pdf = await ctx.get(`/relatorios/estoque/pdf?proprietarioId=${s.produtorId}`);
  expect(pdf.status()).toBe(200);
  expect(pdf.headers()['content-type']).toContain('application/pdf');
  expect((await pdf.body()).subarray(0, 4).toString()).toBe('%PDF');

  const csv = await ctx.get(`/relatorios/pedidos/csv?proprietarioId=${s.produtorId}`);
  expect(csv.status()).toBe(200);
  expect(await csv.text()).toContain('Comprador Smoke');
  await ctx.dispose();
});

test('front-app: login pela tela abre o dashboard', async ({ page }) => {
  await page.goto(`${APP_URL}/entrar`);
  await page.getByLabel('E-mail').fill(s.email!);
  await page.getByLabel(/^Senha/).fill(SENHA);
  await page.getByRole('button', { name: 'Entrar' }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
  await expect(page.getByRole('navigation', { name: 'Navegação principal' })).toBeVisible();
});

test('front-site: o catálogo mostra a oferta', async ({ page }) => {
  await page.goto(SITE_URL);
  await expect(page.getByRole('heading', { name: 'Banco Comunitário de Sementes' })).toBeVisible();
  await page.getByPlaceholder('Buscar por nome popular...').fill(s.nomeOferta!);
  await expect(page.locator('[data-slot="card"]').filter({ hasText: s.nomeOferta! })).toBeVisible();
});

// Roda mesmo se algum teste falhar: nada do smoke pode ficar em produção
test.afterAll(async () => {
  if (!s.accessToken || !s.produtorId) return;
  const ctx = await api(s.accessToken);
  if (s.pedidoId) await ctx.delete(`/pedidos/${s.pedidoId}`);
  if (s.estoqueId) await ctx.delete(`/estoques/${s.estoqueId}`);
  if (s.produtoId) await ctx.delete(`/produtos/${s.produtoId}`);
  const exclusao = await ctx.delete(`/api/pessoas/${s.produtorId}/conta`);
  expect(exclusao.status(), `exclusão da conta smoke: ${await exclusao.text()}`).toBe(204);

  // A conta não existe mais
  const login = await (await api()).post('/auth/login', { data: { email: s.email, senha: SENHA } });
  expect(login.status()).toBe(401);
  await ctx.dispose();
});
