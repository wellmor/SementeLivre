import { test, expect } from '@playwright/test';
import { semViolacoesGraves } from '../../support/acessibilidade';

/** Página 404 do front-site: endereço que não existe. */
test.describe('Página não encontrada (front-site)', () => {
  test('endereço inexistente devolve 404 e oferece produtos e mapa', async ({ page }) => {
    const resposta = await page.goto('/pagina-que-nao-existe');
    expect(resposta?.status()).toBe(404);
    await expect(page).toHaveTitle('Página não encontrada | Semente Livre');
    await expect(page.getByRole('heading', { level: 1, name: 'Página não encontrada' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Abrir o mapa' })).toHaveAttribute('href', '/mapa');

    // O axe não pegou texto branco sobre botão branco aqui: confere as cores direto
    const verProdutos = page.getByRole('link', { name: 'Ver produtos disponíveis' });
    const [texto, fundo] = await verProdutos.evaluate((el) => {
      const estilo = getComputedStyle(el);
      return [estilo.color, estilo.backgroundColor];
    });
    expect(texto).not.toBe(fundo);

    await verProdutos.click();
    await expect(page).toHaveURL(/\/#produtos$/);
    await expect(page.getByRole('heading', { name: 'Produtos Disponíveis' })).toBeVisible();
  });

  test('acessibilidade', async ({ page }) => {
    await page.goto('/rota/inexistente/aninhada');
    await expect(page.getByRole('heading', { level: 1, name: 'Página não encontrada' })).toBeVisible();
    await semViolacoesGraves(page, '404 do site');
  });
});
