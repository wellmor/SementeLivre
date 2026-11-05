import type { BrowserContext, Page } from '@playwright/test';
import { test, expect, gravarSessao, toast } from '../../support/fixtures';
import { API_URL, criarOferta, criarProdutor } from '../../support/api';

/**
 * Sem conexão com a API nem com a sessão (o servidor do app continua no ar, então
 * as telas carregam; o Service Worker do PR do PWA cobre a internet toda).
 */
async function semConexao(context: BrowserContext) {
  await context.route(`${API_URL}/**`, (rota) => rota.abort('internetdisconnected'));
  await context.route('**/api/sessao/**', (rota) => rota.abort('internetdisconnected'));
}

async function dadosOffline(page: Page): Promise<Record<string, string>> {
  return page.evaluate(() => {
    const dados: Record<string, string> = {};
    for (let i = 0; i < localStorage.length; i++) {
      const chave = localStorage.key(i)!;
      if (chave.startsWith('sl_offline')) dados[chave] = localStorage.getItem(chave)!;
    }
    return dados;
  });
}

/** RNF-04 (#234): dados já carregados continuam visíveis sem conexão. */
test.describe('Leitura offline (front-app)', () => {
  test('sem conexão, a tela mostra os últimos dados carregados, sem erro', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor);
    await page.goto('/produtos');
    await expect(page.getByText(oferta.nomePopular)).toBeVisible();

    await semConexao(context);
    await page.reload();

    await expect(page.getByText(oferta.nomePopular)).toBeVisible();
    await expect(toast(page, 'Sem conexão com o servidor')).toHaveCount(0);
  });

  test('com a sessão salva e sem conexão, o app abre com o perfil salvo', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor);
    await page.goto('/produtos');
    await expect(page.getByText(oferta.nomePopular)).toBeVisible();

    // Aba nova (como reabrir o PWA): sem access token, a sessão teria de ser renovada
    await semConexao(context);
    const aba = await context.newPage();
    await aba.goto('/produtos');

    await expect(aba.getByRole('navigation', { name: 'Navegação principal' })).toBeVisible();
    await expect(aba.getByText(oferta.nomePopular)).toBeVisible();
    await expect(aba.getByText('Não foi possível falar com o servidor.')).toHaveCount(0);
  });

  test('sair da conta apaga os dados salvos', async ({ appLogado: page }) => {
    await page.goto('/produtos');
    await expect.poll(async () => Object.keys(await dadosOffline(page)).length).toBeGreaterThan(0);

    await page.getByRole('navigation', { name: 'Navegação principal' }).getByRole('link', { name: 'Perfil' }).click();
    await page.getByRole('button', { name: 'Sair do Aplicativo' }).click();
    await page.getByRole('dialog').getByRole('button', { name: 'Sair', exact: true }).click();
    await expect(page).toHaveURL(/\/entrar$/);

    expect(await dadosOffline(page)).toEqual({});
  });

  test('outra conta no mesmo aparelho não vê os dados da anterior', async ({ appLogado: page, produtor, context, baseURL }) => {
    await page.goto('/perfil');
    await expect(page.getByText(produtor.email).first()).toBeVisible();
    expect(JSON.stringify(await dadosOffline(page))).toContain(produtor.email);

    // Segunda conta entra no mesmo navegador sem a primeira ter saído
    const segunda = await criarProdutor();
    await gravarSessao(page, baseURL!, segunda.refreshToken);
    await page.evaluate(() => sessionStorage.clear());
    await page.goto('/perfil');
    await expect(page.getByText(segunda.email).first()).toBeVisible();

    // O que era da primeira (perfil, e-mail, nome) saiu do aparelho
    const dados = await dadosOffline(page);
    expect(dados['sl_offline_dono']).toBe(segunda.id);
    expect(JSON.stringify(dados)).not.toContain(produtor.email);
    expect(JSON.stringify(dados)).not.toContain(produtor.nome);

    // E sem conexão, o app abre com o perfil da segunda, nunca o da primeira
    await semConexao(context);
    const aba = await context.newPage();
    await aba.goto('/perfil');
    await expect(aba.getByText(segunda.email).first()).toBeVisible();
    await expect(aba.getByText(produtor.email)).toHaveCount(0);
  });
});
