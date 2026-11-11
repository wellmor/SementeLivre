import { test, expect } from '../../support/fixtures';
import { criarOferta } from '../../support/api';
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

  test('tela aberta só tocando nos links do app também abre sem internet', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor);
    await esperarServiceWorker(page);

    // Como no celular, sem digitar endereço: Produtos → o produto → Ver Movimentações.
    // Por dentro do app o Next só busca os dados RSC, que o Service Worker não guarda
    const nav = page.getByRole('navigation', { name: 'Navegação principal' });
    await nav.getByRole('link', { name: 'Produtos' }).click();
    await page.getByText(oferta.nomePopular).click();
    await page.getByText('Ver Movimentações').click();
    const historico = `/produtos/${oferta.estoqueId}/estoque`;
    await expect(page).toHaveURL(new RegExp(`${historico}$`));
    await expect(page.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();

    // O Service Worker guarda o HTML das telas em segundo plano
    for (const tela of [`/produtos/${oferta.estoqueId}`, historico]) {
      await expect
        .poll(() => page.evaluate(async (t) => Boolean(await (await caches.open('sl-paginas-v1')).match(t)), tela), {
          timeout: 15_000,
        })
        .toBe(true);
    }

    // Sem internet, recarregar (ou reabrir) o Histórico abre a tela, não "Sem internet"
    await ficarOffline(context);
    const recarga = await page.reload();
    expect(recarga?.fromServiceWorker()).toBe(true);
    await expect(page).toHaveURL(new RegExp(`${historico}$`));
    await expect(page.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();
    await expect(page.getByRole('heading', { level: 1, name: 'Sem internet' })).toHaveCount(0);

    // A tela do produto, aberta no caminho, também ficou guardada
    await page.goto(`/produtos/${oferta.estoqueId}`);
    await expect(page).toHaveURL(new RegExp(`/produtos/${oferta.estoqueId}$`));
    await expect(page.getByRole('heading', { name: oferta.nomePopular })).toBeVisible();
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
