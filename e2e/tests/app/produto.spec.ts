import { test, expect, toast } from '../../support/fixtures';
import { API_URL, PNG_1X1, criarOferta, unico } from '../../support/api';

/** Cadastro de produto com foto: é o que alimenta o catálogo público. */
test.describe('Produtos (front-app)', () => {
  test('cadastra produto com foto e estoque inicial', async ({ appLogado: page }) => {
    const nome = `Milho E2E ${unico()}`;
    await page.goto('/produtos/nova');
    await page.getByLabel('Arquivo de foto').setInputFiles({ name: 'milho.png', mimeType: 'image/png', buffer: PNG_1X1 });
    await expect(page.getByAltText('Preview do produto')).toBeVisible();
    await page.getByLabel('Nome popular').fill(nome);
    await page.getByLabel('Nome científico').fill('Zea mays');
    await page.getByLabel('Tipo').selectOption('CEREAL');
    await page.getByLabel('Espécie').selectOption('MILHO');
    await page.getByLabel('Quantidade').fill('30');
    await page.getByLabel('Preço (R$)').fill('8.5');
    await page.getByRole('button', { name: 'Cadastrar Produto' }).click();

    await expect(toast(page, 'Produto cadastrado com sucesso!')).toBeVisible();
    await expect(page).toHaveURL(/\/produtos$/);
    await expect(page.getByText(nome)).toBeVisible();
  });

  test('foto acima de 5 MB é recusada na tela com o motivo, sem enviar nada', async ({ appLogado: page }) => {
    // O servidor derruba a conexão em uploads grandes; sem a checagem no cliente a tela diria "sem conexão".
    let enviou = false;
    await page.route('**/produtos/upload-foto', (route) => { enviou = true; return route.abort(); });
    await page.goto('/produtos/nova');
    await page.getByLabel('Arquivo de foto').setInputFiles({
      name: 'grande.png', mimeType: 'image/png', buffer: Buffer.alloc(5 * 1024 * 1024 + 1),
    });
    await expect(page.getByText('A foto excede o tamanho máximo de 5 MB.')).toBeVisible();
    await expect(page.getByAltText('Preview do produto')).toHaveCount(0);

    await page.getByLabel('Nome popular').fill(`Grande ${unico()}`);
    await page.getByLabel('Quantidade').fill('3');
    await page.getByRole('button', { name: 'Cadastrar Produto' }).click();
    await expect(page.getByText('A foto excede o tamanho máximo de 5 MB.')).toBeVisible();
    await expect(page.getByText(/Sem conexão/)).toHaveCount(0);
    expect(enviou).toBe(false);
  });

  test('estoque zerado por pedido aparece como indisponível e sai do site (#127)', async ({ appLogado: page, produtor, request }) => {
    const oferta = await criarOferta(produtor, { quantidade: 5 });
    const headers = { Authorization: `Bearer ${produtor.accessToken}` };
    const pedido = await request.post(`${API_URL}/pedidos`, {
      headers,
      data: { tipoPedido: 'VENDA', proprietarioRecebedorId: produtor.id,
              itens: [{ produtoId: oferta.produtoId, quantidade: 5, precoUnitario: 10 }], comprador: { nome: 'Esgota' } },
    });
    expect(pedido.ok(), await pedido.text()).toBeTruthy();
    expect((await request.patch(`${API_URL}/pedidos/${(await pedido.json()).id}/confirmar`, { headers })).ok()).toBeTruthy();

    await page.goto('/produtos');
    const card = page.getByRole('listitem').filter({ hasText: oferta.nomePopular });
    await expect(card.getByText('0 KG')).toBeVisible();
    await expect(card.getByText('Indisponível')).toBeVisible();

    const catalogo = await (await request.get(`${API_URL}/catalogo/produtos?nomePopular=${encodeURIComponent(oferta.nomePopular)}`)).json();
    expect(catalogo.content).toHaveLength(0);
  });

  test('endereços antigos /sementes levam para /produtos (#186)', async ({ appLogado: page }) => {
    await page.goto('/sementes');
    await expect(page).toHaveURL(/\/produtos$/);

    await page.goto('/sementes/nova');
    await expect(page).toHaveURL(/\/produtos\/nova$/);
    await expect(page.getByRole('button', { name: 'Cadastrar Produto' })).toBeVisible();
  });

  test('exige foto, nome e quantidade', async ({ appLogado: page }) => {
    await page.goto('/produtos/nova');
    await page.getByRole('button', { name: 'Cadastrar Produto' }).click();

    await expect(page.getByText('Foto é obrigatória')).toBeVisible();
    await expect(page.getByText('Nome popular é obrigatório')).toBeVisible();
    await expect(page.getByText('Quantidade inválida')).toBeVisible();
  });
});
