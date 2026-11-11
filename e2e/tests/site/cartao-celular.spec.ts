import { test, expect } from '@playwright/test';
import { criarOferta, criarProdutor, unico } from '../../support/api';

/** No celular não existe hover: o cartão da semente precisa abrir ao toque e o botão de ação precisa aparecer. */
test.describe('Cartão da semente no celular (front-site)', () => {
  test.use({ viewport: { width: 390, height: 844 }, hasTouch: true, isMobile: true });

  test('botão de ação visível sem hover e toque no nome abre a página da semente', async ({ page }) => {
    const produtor = await criarProdutor();
    const oferta = await criarOferta(produtor, { nomePopular: `Feijão Toque ${unico()}` });

    await page.goto('/');
    await page.getByPlaceholder('Buscar por nome popular...').fill(oferta.nomePopular);
    const card = page.locator('[data-slot="card"]').filter({ hasText: oferta.nomePopular });
    await expect(card).toBeVisible();

    const acao = card.getByRole('button', { name: /Fazer pedido|Quero negociar/ });
    await expect(acao).toHaveCSS('opacity', '1');

    await card.getByRole('link', { name: oferta.nomePopular }).tap();
    await expect(page).toHaveURL(/\/produto\/[^/?]+\?oferta=/);
  });
});
