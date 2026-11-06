import { test, expect } from '../../support/fixtures';
import { semViolacoesGraves } from '../../support/acessibilidade';

/** Tela de ajuda em português (RNF-03): abre pelo Perfil e se expande por assunto. */
test.describe('Ajuda (front-app)', () => {
  test('abre pelo Perfil e expande um assunto', async ({ appLogado: page }) => {
    await page.goto('/perfil');
    await page.getByRole('link', { name: 'Ajuda' }).click();
    await expect(page).toHaveURL(/\/ajuda$/);
    await expect(page.getByRole('heading', { level: 1, name: 'Ajuda' })).toBeVisible();

    const assunto = page.getByRole('button', { name: 'Usar sem internet' });
    await expect(assunto).toHaveAttribute('aria-expanded', 'false');
    await assunto.click();
    await expect(assunto).toHaveAttribute('aria-expanded', 'true');
    await expect(page.getByText('Só isso funciona sem conexão.', { exact: false })).toBeVisible();
  });

  test('acessibilidade', async ({ appLogado: page }) => {
    await page.goto('/ajuda');
    await page.getByRole('button', { name: 'Pedidos: registrar, confirmar e cancelar' }).click();
    await semViolacoesGraves(page, '/ajuda');
  });
});
