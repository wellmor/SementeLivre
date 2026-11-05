import { test, expect, type Page } from '@playwright/test';
import { criarOferta, criarProdutor, criarPropriedade, unico } from '../../support/api';
import { semViolacoesGraves } from '../../support/acessibilidade';

/** Fundo do mapa fora do teste: não depende da rede nem pesa nos servidores do OpenStreetMap. */
async function semTilesDoOsm(page: Page) {
  await page.route('https://tile.openstreetmap.org/**', (rota) => rota.abort());
}

/** RF-08 — mapa de sementes, com localização aproximada (LGPD). */
test.describe('Mapa de sementes (front-site)', () => {
  test('ponto da comunidade fica na sede do município e não leva dado do produtor', async ({ page, request }) => {
    const produtor = await criarProdutor({ exibirNoSitePublico: true });
    await criarPropriedade(produtor);
    const oferta = await criarOferta(produtor, { nomePopular: `Milho Mapa ${unico()}` });

    const res = await request.get('/api/mapa');
    expect(res.ok()).toBeTruthy();
    const pontos: Array<Record<string, unknown>> = await res.json();
    const ponto = pontos.find((p) => p.comunidade === 'Quilombo dos Coelhos');
    expect(ponto, 'Quilombo dos Coelhos no mapa').toBeTruthy();

    // Só o que o catálogo público já mostra: nada de nome, contato ou endereço de produtor
    expect(Object.keys(ponto!).sort()).toEqual(
      ['comunidade', 'comunidadeId', 'latitude', 'localizacao', 'longitude', 'precisao', 'produtos'].sort()
    );
    expect(JSON.stringify(pontos)).not.toContain(produtor.nome);
    expect(JSON.stringify(pontos)).not.toContain('Estrada');

    // Sede de Rio Pomba (IBGE), não o endereço da propriedade
    expect(ponto).toMatchObject({ localizacao: 'Rio Pomba - MG', precisao: 'municipio' });
    expect(ponto!.latitude as number).toBeCloseTo(-21.2712, 3);
    expect(ponto!.longitude as number).toBeCloseTo(-43.1696, 3);
    const produtos = ponto!.produtos as Array<{ nome: string; urlFoto: string | null }>;
    expect(produtos.map((p) => Object.keys(p).sort())).toContainEqual(['nome', 'urlFoto']);
    expect(produtos.find((p) => p.nome === oferta.nomePopular)?.urlFoto).toMatch(/^\/uploads\/produtos\//);

    await semTilesDoOsm(page);
    await page.goto('/mapa');
    await expect(page.getByRole('heading', { level: 1, name: 'Mapa de Sementes' })).toBeVisible();
    await expect(page.getByRole('region', { name: /Mapa das comunidades/ })).toBeVisible();
    const item = page.getByRole('listitem').filter({ hasText: 'Quilombo dos Coelhos' });
    await expect(item.getByText('Rio Pomba - MG')).toBeVisible();
  });

  test('card da comunidade em /mapa mostra as fotos e leva aos produtos dela', async ({ page }) => {
    const produtor = await criarProdutor();
    await criarPropriedade(produtor);
    const oferta = await criarOferta(produtor, { nomePopular: `Abóbora Lista ${unico()}` });

    await semTilesDoOsm(page);
    await page.goto('/mapa');
    const card = page.getByRole('listitem').filter({ has: page.getByRole('heading', { name: /Quilombo dos Coelhos/ }) });
    await expect(card.getByText('Rio Pomba - MG')).toBeVisible();
    await expect(card.getByText(/\d+ produtos?/)).toBeVisible();
    await expect(card.getByRole('list', { name: 'Produtos de Quilombo dos Coelhos' }).getByRole('img').first()).toBeVisible();

    // "Ver no mapa" foca o pino e abre o balão
    await card.getByRole('button', { name: /Ver no mapa/ }).click();
    await expect(page.locator('.leaflet-popup-content')).toContainText('Quilombo dos Coelhos');

    // "Ver produtos" abre a home já filtrada pela comunidade
    await card.getByRole('link', { name: /Ver produtos/ }).click();
    await expect(page).toHaveURL(/\/\?comunidade=.+#produtos$/);
    await expect(page.getByLabel('Filtrar por comunidade')).toHaveValue(/.+/);
    await expect(page.getByLabel('Filtrar por comunidade').locator('option:checked')).toHaveText('Quilombo dos Coelhos');
    await page.getByPlaceholder('Buscar por nome popular...').fill(oferta.nomePopular);
    await expect(page.locator('[data-slot="card"]').filter({ hasText: oferta.nomePopular })).toBeVisible();
  });

  test('home tem a seção do mapa, que só carrega ao rolar até ela', async ({ page }) => {
    const pedidosDoMapa: string[] = [];
    page.on('request', (r) => {
      if (r.url().includes('/api/mapa')) pedidosDoMapa.push(r.url());
    });
    await semTilesDoOsm(page);
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Banco Comunitário de Sementes' })).toBeVisible();
    expect(pedidosDoMapa).toHaveLength(0);

    const secao = page.getByRole('region', { name: 'Onde estão as comunidades' });
    await secao.scrollIntoViewIfNeeded();
    await expect(secao.getByRole('region', { name: /Mapa das comunidades/ })).toBeVisible();
    await expect(secao.getByText('A localização é aproximada.')).toBeVisible();
    expect(pedidosDoMapa.length).toBeGreaterThan(0);
  });

  test('"Ver no mapa" do card abre a região, e nome com HTML não vira script no balão', async ({ page }) => {
    const produtor = await criarProdutor();
    await criarPropriedade(produtor);
    const sufixo = unico();
    const nomePopular = `Feijão <img src=x onerror="window.__xss=1"> ${sufixo}`;
    await criarOferta(produtor, { nomePopular });

    await semTilesDoOsm(page);
    await page.goto('/');
    await page.getByPlaceholder('Buscar por nome popular...').fill(sufixo);
    const card = page.locator('[data-slot="card"]').filter({ hasText: sufixo });
    await card.getByRole('button', { name: /Ver no mapa/ }).click();

    const janela = page.getByRole('dialog', { name: 'Quilombo dos Coelhos' });
    await expect(janela).toBeVisible();
    await expect(janela.getByText('Região aproximada: Rio Pomba - MG')).toBeVisible();

    const balao = janela.locator('.leaflet-popup-content');
    await expect(balao).toContainText('Quilombo dos Coelhos');
    // Miniatura com a foto enviada e o nome como texto alternativo. O balão mostra
    // as primeiras em ordem alfabética; "<" vem antes das letras.
    const miniatura = balao.locator('img[alt^="Feijão <img src=x onerror="]').first();
    await expect(miniatura).toBeVisible();
    await expect(miniatura).toHaveAttribute('src', /^\/uploads\/produtos\//);
    // O nome não virou elemento: nenhuma imagem com src="x"
    await expect(balao.locator('img[src="x"]')).toHaveCount(0);
    expect(await page.evaluate(() => (window as unknown as { __xss?: number }).__xss)).toBeUndefined();

    await janela.getByRole('button', { name: 'Fechar mapa' }).click();
    await expect(janela).toBeHidden();
  });
});
