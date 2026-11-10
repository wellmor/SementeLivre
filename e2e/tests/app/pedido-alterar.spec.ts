import { test, expect, toast } from '../../support/fixtures';
import { buscarEstoque, criarOferta, listarPedidos } from '../../support/api';
import { registrarPedido } from '../../support/fluxos';

/** CDU-13: alterar pedido da fila no front-app. O estoque só baixa na confirmação (#183). */
test.describe('Alterar pedido (front-app)', () => {
  test('altera quantidade, recebedor e tipo sem mexer no estoque', async ({ appLogado: page, produtor }) => {
    const oferta = await criarOferta(produtor, { quantidade: 40 });
    const id = await registrarPedido(page, oferta, 5, 'Nome Antigo');

    await page.getByRole('button', { name: 'Alterar Pedido' }).click();
    await expect(page.getByRole('heading', { name: /^Alterar pedido/ })).toBeFocused();

    // O formulário abre com os dados atuais
    await expect(page.getByLabel('Nome completo')).toHaveValue('Nome Antigo');
    const quantidade = page.getByLabel(`Quantidade de ${oferta.nomePopular}`);
    await expect(quantidade).toHaveValue('5');

    await page.getByLabel('Nome completo').fill('Nome Novo');
    await quantidade.fill('12');
    await page.getByRole('radio', { name: 'Troca' }).click();
    await page.getByRole('button', { name: 'Salvar Alterações' }).click();

    await expect(toast(page, 'Pedido alterado com sucesso!')).toBeVisible();
    // O detalhe volta com os dados salvos
    await expect(page.getByText('Nome Novo')).toBeVisible();
    await expect(page.getByRole('listitem').filter({ hasText: oferta.nomePopular })).toContainText('12');
    await expect(page.getByRole('button', { name: 'Alterar Pedido' })).toBeFocused();

    expect((await listarPedidos(produtor)).find((p) => p.id === id)?.tipoPedido).toBe('TROCA');
    expect((await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(40);

    // A confirmação baixa a quantidade alterada
    await page.getByRole('button', { name: 'Confirmar Pedido' }).click();
    await expect(toast(page, 'Pedido confirmado!')).toBeVisible();
    expect((await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(28);
  });

  test('quantidade acima do estoque e recebedor vazio bloqueiam a alteração', async ({ appLogado: page, produtor }) => {
    const oferta = await criarOferta(produtor, { quantidade: 10 });
    const id = await registrarPedido(page, oferta, 2, 'Pedido Intacto');

    await page.getByRole('button', { name: 'Alterar Pedido' }).click();
    await page.getByLabel('Nome completo').fill('');
    const quantidade = page.getByLabel(`Quantidade de ${oferta.nomePopular}`);
    await quantidade.fill('99');
    await page.getByRole('button', { name: 'Salvar Alterações' }).click();

    await expect(page.getByText('Nome é obrigatório')).toBeVisible();
    await expect(page.getByText(/Quantidade maior que o estoque disponível \(10/)).toBeVisible();
    await expect(quantidade).toHaveAttribute('aria-invalid', 'true');
    await expect(page.getByLabel('Nome completo')).toBeFocused();

    // Descartar volta ao detalhe sem mudar nada
    await page.getByRole('button', { name: 'Descartar alterações' }).click();
    await expect(page.getByText('Pedido Intacto')).toBeVisible();
    const pedido = (await listarPedidos(produtor)).find((p) => p.id === id);
    expect(pedido?.status).toBe('PENDENTE');
  });

  test('pedido confirmado não pode ser alterado', async ({ appLogado: page, produtor }) => {
    const oferta = await criarOferta(produtor, { quantidade: 10 });
    await registrarPedido(page, oferta, 2, 'Já Confirmado');

    await page.getByRole('button', { name: 'Confirmar Pedido' }).click();
    await expect(toast(page, 'Pedido confirmado!')).toBeVisible();

    await expect(page.getByRole('button', { name: 'Alterar Pedido' })).toHaveCount(0);
  });

  test('sem internet, a alteração fica bloqueada com aviso', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor, { quantidade: 10 });
    await registrarPedido(page, oferta, 2, 'Sem Internet');

    await context.setOffline(true);
    const alterar = page.getByRole('button', { name: 'Alterar Pedido' });
    await expect(alterar).toBeDisabled();
    await expect(page.getByText('Sem internet: para alterar o pedido, conecte-se primeiro.')).toBeVisible();

    await context.setOffline(false);
    await expect(alterar).toBeEnabled();
  });
});
