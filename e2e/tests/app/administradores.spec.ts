import { request } from '@playwright/test';
import { test, expect, toast, gravarSessao } from '../../support/fixtures';
import { API_URL, gerarCpf, unico } from '../../support/api';

/**
 * Administradores no front-app (/admin/administradores).
 *
 * O administrador principal destes testes é criado no globalSetup a partir do
 * admin do seed (precisa de E2E_ADMIN_SENHA); sem ele, os testes de admin são pulados.
 */
const SUPER_EMAIL = process.env.E2E_SUPER_ADMIN_EMAIL ?? '';
const SUPER_SENHA = process.env.E2E_SUPER_ADMIN_SENHA ?? '';

async function login(email: string, senha: string): Promise<{ accessToken: string; refreshToken: string }> {
  const api = await request.newContext({ baseURL: API_URL });
  try {
    const res = await api.post('/auth/login', { data: { email, senha } });
    expect(res.ok(), `login ${email}: ${res.status()}`).toBeTruthy();
    return await res.json();
  } finally {
    await api.dispose();
  }
}

async function listarPelaApi(accessToken: string): Promise<{ email: string; nivelAcesso: string }[]> {
  const api = await request.newContext({ baseURL: API_URL, extraHTTPHeaders: { Authorization: `Bearer ${accessToken}` } });
  try {
    const res = await api.get('/admin/administradores');
    expect(res.ok()).toBeTruthy();
    return await res.json();
  } finally {
    await api.dispose();
  }
}

test.describe('Administradores (front-app)', () => {
  test('proprietário não abre a tela de administradores', async ({ appLogado: page }) => {
    await page.goto('/admin/administradores');
    await expect(toast(page, 'Área restrita ao administrador.')).toBeVisible();
    await expect(page).toHaveURL(/\/dashboard$/);
  });

  test.describe('com o administrador principal', () => {
    // Em série: os testes entram com a mesma conta, e cada login derruba a sessão anterior
    test.describe.configure({ mode: 'serial' });

    test.beforeEach(async ({ page, baseURL }) => {
      test.skip(!SUPER_EMAIL, 'sem administrador principal de teste; defina E2E_ADMIN_SENHA');
      const sessao = await login(SUPER_EMAIL, SUPER_SENHA);
      await gravarSessao(page, baseURL!, sessao.refreshToken);
    });

    test('adiciona um administrador, que já consegue entrar', async ({ page }) => {
      const nome = `Admin E2E ${unico()}`;
      const email = `e2e-admin-${unico()}@sementelivre.test`;

      await page.goto('/perfil');
      await page.getByRole('link', { name: 'Administradores' }).click();
      await expect(page).toHaveURL(/\/admin\/administradores$/);
      await expect(page.getByRole('listitem').filter({ hasText: 'Principal E2E (você)' })).toBeVisible();

      await page.getByRole('button', { name: 'Adicionar administrador' }).click();
      const dialogo = page.getByRole('dialog', { name: 'Adicionar administrador' });

      // Validação antes de enviar
      await dialogo.getByRole('button', { name: 'Adicionar', exact: true }).click();
      await expect(dialogo.getByText('CPF inválido')).toBeVisible();

      await dialogo.getByLabel('Nome').fill(nome);
      await dialogo.getByLabel('E-mail').fill(email);
      await dialogo.getByLabel('CPF').fill(gerarCpf());
      await dialogo.getByLabel('Senha provisória').fill('Provisoria123');
      await dialogo.getByRole('button', { name: 'Adicionar', exact: true }).click();

      await expect(toast(page, `${nome} já pode entrar com o e-mail ${email} e a senha provisória.`)).toBeVisible();
      await expect(dialogo).toBeHidden();
      await expect(page.getByRole('listitem').filter({ hasText: nome })).toContainText('Administrador');

      const novo = await login(email, 'Provisoria123');
      const lista = await listarPelaApi(novo.accessToken);
      expect(lista.find((a) => a.email === email)?.nivelAcesso).toBe('ADMIN');
    });

    test('administrador comum vê a lista, mas não adiciona', async ({ page, baseURL }) => {
      const principal = await login(SUPER_EMAIL, SUPER_SENHA);
      const email = `e2e-admin-${unico()}@sementelivre.test`;
      const api = await request.newContext({
        baseURL: API_URL,
        extraHTTPHeaders: { Authorization: `Bearer ${principal.accessToken}` },
      });
      const res = await api.post('/admin/administradores', {
        data: { nome: 'Comum E2E', email, tipoDocumento: 'CPF', documento: gerarCpf(), senha: 'Provisoria123' },
      });
      expect(res.status()).toBe(201);
      await api.dispose();

      const sessao = await login(email, 'Provisoria123');
      await gravarSessao(page, baseURL!, sessao.refreshToken);
      await page.goto('/admin/administradores');

      await expect(page.getByRole('listitem').filter({ hasText: 'Comum E2E (você)' })).toBeVisible();
      await expect(page.getByText('Só o administrador principal pode adicionar outros administradores.')).toBeVisible();
      await expect(page.getByRole('button', { name: 'Adicionar administrador' })).toHaveCount(0);
    });
  });
});
