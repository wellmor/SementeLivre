import { test, expect } from '../../support/fixtures';
import { esperarServiceWorker, ficarOffline, voltarOnline } from '../../support/offline';

const AVISO = 'Sem internet. O que já foi aberto continua disponível.';

/** RNF-04 (#234): o PWA abre sem internet. */
test.describe('PWA offline (front-app)', () => {
  test('telas abrem sem internet, com aviso, e tela nunca aberta cai em "Sem internet"', async ({ page, context }) => {
    await page.goto('/entrar');
    await esperarServiceWorker(page);

    await ficarOffline(context);

    // Recarregar offline: a tela vem do Service Worker, não o erro do navegador
    const recarga = await page.reload();
    expect(recarga?.fromServiceWorker()).toBe(true);
    await expect(page.getByRole('button', { name: 'Entrar' })).toBeVisible();
    await expect(page.getByRole('status').filter({ hasText: AVISO })).toBeVisible();

    // Tela guardada na instalação, mesmo sem ter sido aberta antes nesta visita
    const termos = await page.goto('/termos');
    expect(termos?.fromServiceWorker()).toBe(true);
    await expect(page.getByRole('heading', { level: 1 }).first()).toBeVisible();

    // Tela que não está no aparelho: página "Sem internet" do próprio app
    await page.goto('/produtos/um-produto-que-nunca-abriu');
    await expect(page).toHaveURL(/\/offline\?de=%2Fprodutos%2Fum-produto-que-nunca-abriu$/);
    await expect(page.getByRole('heading', { level: 1, name: 'Sem internet' })).toBeVisible();

    // A conexão voltou: o aviso some, e "Tentar novamente" volta à tela pedida
    await voltarOnline(context);
    await expect(page.getByRole('status').filter({ hasText: AVISO })).toHaveCount(0);
    // O splash cobre a tela nos primeiros segundos de cada abertura
    await expect(page.getByRole('status', { name: /carregando aplicação/ })).toBeHidden();
    await page.getByRole('button', { name: 'Tentar novamente' }).click();
    await expect(page).toHaveURL(/\/produtos\/um-produto-que-nunca-abriu$/);
  });

  test('o Service Worker não guarda respostas da API nem da sessão', async ({ appLogado: page }) => {
    await esperarServiceWorker(page);
    await page.goto('/produtos');
    await page.goto('/pedidos');

    const guardados = await page.evaluate(async () => {
      const urls: string[] = [];
      for (const nome of await caches.keys()) {
        for (const req of await (await caches.open(nome)).keys()) urls.push(req.url);
      }
      return urls;
    });
    expect(guardados.length).toBeGreaterThan(0);
    const origemDoApp = new URL(page.url()).origin;
    expect(guardados.filter((url) => !url.startsWith(origemDoApp))).toEqual([]);
    expect(guardados.filter((url) => new URL(url).pathname.startsWith('/api/'))).toEqual([]);
  });
});
