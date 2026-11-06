import { request } from '@playwright/test';
import { test, expect, toast, gravarSessao } from '../../support/fixtures';
import { API_URL, Produtor, unico } from '../../support/api';
import { semViolacoesGraves } from '../../support/acessibilidade';

/**
 * Aprovação de comunidades pelo admin no front-app (/admin/comunidades).
 *
 * A conta de admin é a do seeder (V1_3). A senha vem de E2E_ADMIN_SENHA; sem ela,
 * vale a da documentação do modelo de dados. Se o login não passar, os testes de
 * admin são pulados (o de proprietário roda sempre).
 */
const ADMIN_EMAIL = process.env.E2E_ADMIN_EMAIL ?? 'admin@sementelivre.com.br';
const ADMIN_SENHA = process.env.E2E_ADMIN_SENHA ?? 'admin123';

async function loginAdmin(): Promise<{ accessToken: string; refreshToken: string } | null> {
  const api = await request.newContext({ baseURL: API_URL });
  try {
    const res = await api.post('/auth/login', { data: { email: ADMIN_EMAIL, senha: ADMIN_SENHA } });
    return res.ok() ? await res.json() : null;
  } finally {
    await api.dispose();
  }
}

/** Comunidade pedida pelo produtor, como o app faz em /propriedades/nova: nasce PENDENTE_APROVACAO. */
async function solicitarComunidade(produtor: Produtor): Promise<{ id: string; nome: string }> {
  const api = await request.newContext({
    baseURL: API_URL,
    extraHTTPHeaders: { Authorization: `Bearer ${produtor.accessToken}` },
  });
  // Nome bem distinto: o backend recusa nomes parecidos com os que já existem
  const nome = `Comunidade E2E ${unico()} ${unico()}`;
  const res = await api.post('/api/comunidades', {
    data: { nome, endereco: { logradouro: 'Córrego Fundo', municipio: 'Mercês', uf: 'MG' } },
  });
  expect(res.ok(), `comunidade: ${await res.text()}`).toBeTruthy();
  const corpo = await res.json();
  expect(corpo.status).toBe('PENDENTE_APROVACAO');
  await api.dispose();
  return { id: corpo.id, nome };
}

async function statusDaComunidade(produtor: Produtor, id: string): Promise<string> {
  const api = await request.newContext({
    baseURL: API_URL,
    extraHTTPHeaders: { Authorization: `Bearer ${produtor.accessToken}` },
  });
  const res = await api.get(`/api/comunidades/${id}`);
  expect(res.ok()).toBeTruthy();
  const { status } = await res.json();
  await api.dispose();
  return status;
}

test.describe('Aprovar comunidades (front-app, admin)', () => {
  test('proprietário não vê a entrada nem abre a tela de admin', async ({ appLogado: page }) => {
    await page.goto('/perfil');
    await expect(page.getByRole('heading', { level: 2 }).first()).toBeVisible();
    await expect(page.getByRole('link', { name: 'Aprovar comunidades' })).toHaveCount(0);

    await page.goto('/admin/comunidades');
    await expect(toast(page, 'Área restrita ao administrador.')).toBeVisible();
    await expect(page).toHaveURL(/\/dashboard$/);
  });

  test.describe('com a conta de admin', () => {
    // Em série: todos usam a mesma conta de admin, e cada login apaga o refresh token
    // anterior do usuário (AuthService.criarRefreshToken). Em paralelo, um teste
    // derrubava a sessão do outro e ele caía na tela de login.
    test.describe.configure({ mode: 'serial' });

    let adminDisponivel = false;

    test.beforeAll(async () => {
      // Uma tentativa só: senha errada repetida bloqueia o e-mail por um tempo
      adminDisponivel = (await loginAdmin()) !== null;
    });

    test.beforeEach(async ({ page, baseURL }) => {
      test.skip(!adminDisponivel, `login do admin ${ADMIN_EMAIL} falhou; defina E2E_ADMIN_SENHA`);
      // Sessão nova a cada teste: o refresh token troca a cada uso
      const sessao = await loginAdmin();
      await gravarSessao(page, baseURL!, sessao!.refreshToken);
    });

    test('admin aprova a comunidade pendente e ela fica ATIVA', async ({ page, produtor }) => {
      const comunidade = await solicitarComunidade(produtor);

      await page.goto('/perfil');
      await page.getByRole('link', { name: 'Aprovar comunidades' }).click();
      await expect(page).toHaveURL(/\/admin\/comunidades$/);

      const card = page.getByRole('listitem').filter({ hasText: comunidade.nome });
      await expect(card).toBeVisible();
      await expect(card.getByText('Mercês/MG')).toBeVisible();
      await card.getByRole('button', { name: `Aprovar a comunidade ${comunidade.nome}` }).click();

      const dialogo = page.getByRole('dialog', { name: 'Aprovar comunidade' });
      await expect(dialogo).toContainText(comunidade.nome);
      await dialogo.getByRole('button', { name: 'Aprovar', exact: true }).click();

      await expect(toast(page, `Comunidade "${comunidade.nome}" aprovada.`)).toBeVisible();
      await expect(card).toHaveCount(0);
      expect(await statusDaComunidade(produtor, comunidade.id)).toBe('ATIVA');
    });

    test('admin rejeita a comunidade; cancelar antes não muda nada', async ({ page, produtor }) => {
      const comunidade = await solicitarComunidade(produtor);
      await page.goto('/admin/comunidades');

      const card = page.getByRole('listitem').filter({ hasText: comunidade.nome });
      const rejeitar = card.getByRole('button', { name: `Rejeitar a comunidade ${comunidade.nome}` });

      await rejeitar.click();
      const dialogo = page.getByRole('dialog', { name: 'Rejeitar comunidade' });
      await dialogo.getByRole('button', { name: 'Cancelar' }).click();
      await expect(dialogo).toBeHidden();
      await expect(card).toBeVisible();
      expect(await statusDaComunidade(produtor, comunidade.id)).toBe('PENDENTE_APROVACAO');

      await rejeitar.click();
      await dialogo.getByRole('button', { name: 'Rejeitar', exact: true }).click();

      await expect(toast(page, `Comunidade "${comunidade.nome}" rejeitada.`)).toBeVisible();
      await expect(card).toHaveCount(0);
      expect(await statusDaComunidade(produtor, comunidade.id)).toBe('REJEITADA');
    });

    test('comunidade já analisada em outra sessão: avisa e atualiza a lista', async ({ page, produtor }) => {
      const comunidade = await solicitarComunidade(produtor);
      await page.goto('/admin/comunidades');
      const card = page.getByRole('listitem').filter({ hasText: comunidade.nome });
      await expect(card).toBeVisible();

      // Outro admin aprova pela API enquanto a tela está aberta
      const sessao = await loginAdmin();
      const api = await request.newContext({
        baseURL: API_URL,
        extraHTTPHeaders: { Authorization: `Bearer ${sessao!.accessToken}` },
      });
      expect((await api.patch(`/api/comunidades/${comunidade.id}/aprovar`)).ok()).toBeTruthy();
      await api.dispose();

      await card.getByRole('button', { name: `Rejeitar a comunidade ${comunidade.nome}` }).click();
      await page.getByRole('dialog', { name: 'Rejeitar comunidade' }).getByRole('button', { name: 'Rejeitar', exact: true }).click();

      await expect(toast(page, 'Esta comunidade já foi analisada. A lista foi atualizada.')).toBeVisible();
      await expect(card).toHaveCount(0);
      expect(await statusDaComunidade(produtor, comunidade.id)).toBe('ATIVA');
    });

    test('acessibilidade', async ({ page, produtor }) => {
      const comunidade = await solicitarComunidade(produtor);
      await page.goto('/admin/comunidades');
      await expect(page.getByRole('listitem').filter({ hasText: comunidade.nome })).toBeVisible();
      await semViolacoesGraves(page, 'Aprovar comunidades');
    });
  });
});
