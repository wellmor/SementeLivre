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
    expect(ponto!.produtos).toContain(oferta.nomePopular);

    await semTilesDoOsm(page);
    await page.goto('/mapa');
    await expect(page.getByRole('heading', { level: 1, name: 'Mapa de Sementes' })).toBeVisible();
    await expect(page.getByRole('region', { name: /Mapa das comunidades/ })).toBeVisible();
    const item = page.getByRole('listitem').filter({ hasText: 'Quilombo dos Coelhos' });
    await expect(item.getByText('Rio Pomba - MG')).toBeVisible();
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
    // O balão lista só os 5 primeiros em ordem alfabética; "<" vem antes das letras
    await expect(balao).toContainText('Feijão <img src=x onerror="window.__xss=1">');
    await expect(balao.locator('img')).toHaveCount(0);
    expect(await page.evaluate(() => (window as unknown as { __xss?: number }).__xss)).toBeUndefined();

    await janela.getByRole('button', { name: 'Fechar mapa' }).click();
    await expect(janela).toBeHidden();
  });
});
