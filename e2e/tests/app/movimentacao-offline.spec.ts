import type { Page } from '@playwright/test';
import { test, expect, gravarSessao, toast } from '../../support/fixtures';
import { API_URL, buscarEstoque, criarOferta, criarProdutor } from '../../support/api';

async function registrar(page: Page, tipo: string, quantidade: string, motivo: string) {
  await page.getByRole('button', { name: 'Nova movimentação' }).click();
  await page.getByLabel('Tipo').selectOption(tipo);
  await page.getByLabel('Quantidade').fill(quantidade);
  await page.getByLabel('Motivo (opcional)').fill(motivo);
  await page.getByRole('button', { name: 'Registrar' }).click();
}

function filaDoAparelho(page: Page) {
  return page.evaluate(() => JSON.parse(localStorage.getItem('sl_fila_movimentacoes') ?? '[]') as unknown[]);
}

/** RNF-04 (#234): movimentação de estoque registrada sem internet vai para a fila e é enviada depois. */
test.describe('Movimentação de estoque offline (front-app)', () => {
  test('sem internet, a movimentação fica na fila e é enviada quando a conexão volta', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor, { quantidade: 50 });
    await page.goto(`/produtos/${oferta.estoqueId}/estoque`);
    await expect(page.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();

    await context.setOffline(true);
    await registrar(page, 'ENTRADA', '5', 'Colheita sem internet');

    await expect(toast(page, /Sem internet: a movimentação foi guardada/)).toBeVisible();
    const pendentes = page.getByRole('region', { name: 'Aguardando envio' });
    await expect(pendentes.getByText('Colheita sem internet')).toBeVisible();
    await expect(pendentes.getByText('Será enviada quando a internet voltar')).toBeVisible();
    // Nada chegou ao servidor ainda
    expect((await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(50);

    // A conexão volta: a fila é enviada sozinha e o histórico se atualiza
    await context.setOffline(false);
    await expect(toast(page, '1 movimentação guardada sem internet foi enviada.')).toBeVisible();
    await expect(pendentes).toHaveCount(0);
    await expect(page.getByRole('listitem').filter({ hasText: 'Colheita sem internet' })).toContainText('saldo 50 → 55');
    expect((await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(55);
    expect(await filaDoAparelho(page)).toEqual([]);
  });

  test('movimentação recusada pelo servidor fica marcada e pode ser descartada', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor, { quantidade: 10 });
    await page.goto(`/produtos/${oferta.estoqueId}/estoque`);
    await expect(page.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();

    // Saída maior que o saldo: sem internet não dá para saber; o servidor recusa depois
    await context.setOffline(true);
    await registrar(page, 'SAIDA_VENDA', '999', 'Venda grande demais');
    await context.setOffline(false);

    await expect(toast(page, /1 movimentação guardada foi recusada pelo servidor/)).toBeVisible();
    const pendentes = page.getByRole('region', { name: 'Aguardando envio' });
    await expect(pendentes.getByText(/Recusada pelo servidor:/)).toBeVisible();
    expect((await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(10);

    await pendentes.getByRole('button', { name: /Descartar Saída — Venda de 999/ }).click();
    await expect(pendentes).toHaveCount(0);
    expect(await filaDoAparelho(page)).toEqual([]);
  });

  test('a fila de uma conta não é enviada nem vista por outra conta no mesmo aparelho', async ({ appLogado: page, produtor, context, baseURL }) => {
    const oferta = await criarOferta(produtor, { quantidade: 20 });
    await page.goto(`/produtos/${oferta.estoqueId}/estoque`);
    await expect(page.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();

    await context.setOffline(true);
    await registrar(page, 'ENTRADA', '3', 'Da primeira conta');
    await expect(toast(page, /Sem internet: a movimentação foi guardada/)).toBeVisible();

    // A primeira conta fecha o app ainda sem internet; outra conta entra no mesmo navegador
    await page.close();
    const segunda = await criarProdutor();
    const aba = await context.newPage();
    await gravarSessao(aba, baseURL!, segunda.refreshToken);
    await context.setOffline(false);
    await aba.goto('/dashboard');
    await expect(aba.getByRole('navigation', { name: 'Navegação principal' })).toBeVisible();

    // Dá tempo de a sincronização da segunda conta rodar: a pendência da primeira fica onde está
    await aba.waitForTimeout(2_000);
    expect(await filaDoAparelho(aba)).toHaveLength(1);
    expect((await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(20);
  });

  test('sair do aplicativo avisa e apaga do aparelho as movimentações guardadas desta conta', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor, { quantidade: 12 });
    await page.goto(`/produtos/${oferta.estoqueId}/estoque`);
    await expect(page.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();

    // O envio da movimentação não chega ao servidor: ela fica guardada, com o motivo digitado
    await context.route(`${API_URL}/estoques/${oferta.estoqueId}/movimentacoes`, (rota) =>
      rota.request().method() === 'POST' ? rota.abort('internetdisconnected') : rota.continue()
    );
    await registrar(page, 'ENTRADA', '6', 'Motivo pessoal guardado');
    await expect(toast(page, /Sem internet: a movimentação foi guardada/)).toBeVisible();
    // Pendência de outra conta no mesmo aparelho: não é desta pessoa apagar
    await page.evaluate(() => {
      const fila = JSON.parse(localStorage.getItem('sl_fila_movimentacoes') ?? '[]') as unknown[];
      fila.push({ idLocal: 'de-outra-conta', donoId: 'outra-conta', estoqueId: 'outro', dados: { tipo: 'ENTRADA', quantidade: 1 }, criadaEm: new Date().toISOString() });
      localStorage.setItem('sl_fila_movimentacoes', JSON.stringify(fila));
    });

    await page.getByRole('navigation', { name: 'Navegação principal' }).getByRole('link', { name: 'Perfil' }).click();
    await page.getByRole('button', { name: 'Sair do Aplicativo' }).click();
    const dialogo = page.getByRole('dialog');
    await expect(dialogo).toContainText('1 movimentação guardada sem internet ainda não foi enviada e será descartada deste aparelho.');
    await dialogo.getByRole('button', { name: 'Sair', exact: true }).click();
    await expect(page).toHaveURL(/\/entrar$/);

    const fila = (await filaDoAparelho(page)) as { donoId: string }[];
    expect(fila.filter((item) => item.donoId === produtor.id)).toEqual([]);
    expect(JSON.stringify(fila)).not.toContain('Motivo pessoal guardado');
    expect(fila.map((item) => item.donoId)).toEqual(['outra-conta']);
  });

  test('envio que chegou ao servidor sem a resposta voltar não é repetido', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor, { quantidade: 30 });
    await page.goto(`/produtos/${oferta.estoqueId}/estoque`);
    await expect(page.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();

    await context.setOffline(true);
    await registrar(page, 'ENTRADA', '4', 'Resposta perdida');
    await expect(toast(page, /Sem internet: a movimentação foi guardada/)).toBeVisible();

    // A conexão volta, mas cai de novo no meio do envio: o servidor grava e a resposta se perde
    await context.route(`${API_URL}/estoques/${oferta.estoqueId}/movimentacoes`, async (rota) => {
      if (rota.request().method() !== 'POST') return rota.continue();
      await rota.fetch();
      await rota.abort('connectionreset');
    });
    await context.setOffline(false);
    await expect.poll(async () => (await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(34);
    const pendentes = page.getByRole('region', { name: 'Aguardando envio' });
    await expect(pendentes.getByText('Resposta perdida')).toBeVisible();

    // Conexão estável de novo: a fila percebe que já chegou e não manda outra vez
    await context.unrouteAll({ behavior: 'ignoreErrors' });
    await context.setOffline(true);
    await context.setOffline(false);
    await expect(pendentes).toHaveCount(0);
    expect((await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(34);
    await expect(page.getByRole('listitem').filter({ hasText: 'Resposta perdida' })).toHaveCount(1);
  });

  test('com o app aberto em duas abas, as duas acompanham a fila e a movimentação vai uma vez só', async ({ appLogado: page, produtor, context }) => {
    const oferta = await criarOferta(produtor, { quantidade: 15 });
    const tela = `/produtos/${oferta.estoqueId}/estoque`;
    await page.goto(tela);
    await expect(page.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();
    const outra = await context.newPage();
    await outra.goto(tela);
    await expect(outra.getByRole('heading', { name: 'Histórico de Estoque' })).toBeVisible();

    await context.setOffline(true);
    await registrar(page, 'ENTRADA', '2', 'Registrada na primeira aba');

    // A outra aba fica sabendo pela mudança no armazenamento do navegador
    await expect(outra.getByRole('region', { name: 'Aguardando envio' }).getByText('Registrada na primeira aba')).toBeVisible();

    // A conexão volta: uma aba envia, as duas limpam a pendência
    await context.setOffline(false);
    for (const aba of [page, outra]) {
      await expect(aba.getByRole('region', { name: 'Aguardando envio' })).toHaveCount(0);
    }
    await expect.poll(async () => (await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(17);
    // Dá tempo de a outra aba tentar também: o saldo não pode subir de novo
    await page.waitForTimeout(2_000);
    expect((await buscarEstoque(produtor, oferta.estoqueId)).quantidade).toBe(17);
  });
});
