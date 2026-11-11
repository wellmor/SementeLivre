import { test, expect } from '../../support/fixtures';
import { criarOferta } from '../../support/api';
import { registrarPedido } from '../../support/fluxos';

/** Busca e filtros da lista de pedidos do front-app. */
test.describe('Filtros da lista de pedidos (front-app)', () => {
  test('"Limpar filtros" também esvazia a busca da lista', async ({ appLogado: page, produtor }) => {
    const oferta = await criarOferta(produtor, { quantidade: 20 });
    await registrarPedido(page, oferta, 1, 'Alfa Buscada');
    await registrarPedido(page, oferta, 1, 'Beta Outra');

    await page.goto('/pedidos');
    const busca = page.getByLabel('Buscar pedido');
    await busca.fill('alfa');
    await expect(page.getByText('Beta Outra')).toHaveCount(0);

    // O botão aparece mesmo só com a busca preenchida
    await page.getByRole('button', { name: 'Limpar filtros' }).click();

    await expect(busca).toHaveValue('');
    await expect(page.getByText('Alfa Buscada')).toBeVisible();
    await expect(page.getByText('Beta Outra')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Limpar filtros' })).toHaveCount(0);

    // Com filtro de status + busca, tudo volta junto
    await busca.fill('alfa');
    await page.getByRole('button', { name: 'Pendente', exact: true }).click();
    await page.getByRole('button', { name: 'Limpar filtros' }).click();
    await expect(busca).toHaveValue('');
    await expect(page.getByText('Beta Outra')).toBeVisible();
  });
});
