import { test, expect } from '@playwright/test';
import { semViolacoesGraves } from '../../support/acessibilidade';

/** Página 404 do front-app: endereço que não existe. */
test.describe('Página não encontrada (front-app)', () => {
  test('endereço inexistente devolve 404 e leva de volta ao início', async ({ page }) => {
    const resposta = await page.goto('/pagina-que-nao-existe');
    expect(resposta?.status()).toBe(404);
    await expect(page).toHaveTitle('Página não encontrada | Semente Livre');
    await expect(page.getByRole('heading', { level: 1, name: 'Página não encontrada' })).toBeVisible();

    // O splash cobre a tela nos primeiros segundos de cada abertura
    await expect(page.getByRole('status', { name: /carregando aplicação/ })).toBeHidden();
    await page.getByRole('link', { name: 'Voltar ao início' }).click();

    // Sem sessão, o início manda para o login
    await expect(page).toHaveURL(/\/entrar$/);
  });

  test('acessibilidade', async ({ page }) => {
    await page.goto('/rota/inexistente/aninhada');
    await expect(page.getByRole('heading', { level: 1, name: 'Página não encontrada' })).toBeVisible();
    await semViolacoesGraves(page, '404 do app');
  });
});
