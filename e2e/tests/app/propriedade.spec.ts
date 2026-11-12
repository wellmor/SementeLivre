import { test, expect, toast } from '../../support/fixtures';
import { listarPropriedades, unico } from '../../support/api';

/** CDU de propriedade/comunidade no front-app, gravando no backend (#100). */
test.describe('Propriedades e comunidades (front-app)', () => {
  test('cadastra propriedade em comunidade existente e grava no backend', async ({ appLogado: page, produtor }) => {
    const nome = `Sítio E2E ${unico()}`;
    await page.goto('/propriedades/nova');
    await page.getByLabel('Nome da propriedade').fill(nome);
    await page.getByLabel('Tamanho (hectares)').fill('3.5');
    // Comunidade criada pelo seeder (V1_3) e ATIVA
    await page.getByLabel('Comunidade').selectOption({ label: 'Quilombo dos Coelhos — Rio Pomba/MG' });
    await page.getByLabel('Logradouro').fill('Estrada da Serra, km 4');
    await page.getByLabel('Município').fill('Rio Pomba');
    await page.getByLabel('UF').fill('MG');
    await page.getByRole('button', { name: 'Cadastrar Propriedade' }).click();

    await expect(toast(page, 'Propriedade cadastrada!')).toBeVisible();
    await expect(page).toHaveURL(/\/propriedades$/);
    await expect(page.getByText(nome)).toBeVisible();

    const salvas = await listarPropriedades(produtor);
    expect(salvas).toHaveLength(1);
    expect(salvas[0]).toMatchObject({ nome, comunidadeNome: 'Quilombo dos Coelhos', logradouroMunicipio: 'Rio Pomba' });

    // Outra sessão (sem o localStorage desta) enxerga a mesma propriedade
    await page.evaluate(() => {
      Object.keys(localStorage).filter((k) => !k.endsWith('_token')).forEach((k) => localStorage.removeItem(k));
    });
    await page.reload();
    await page.getByText(nome).click();
    await expect(page).toHaveURL(new RegExp(`/propriedades/${salvas[0].id}$`));
    await expect(page.getByText('Quilombo dos Coelhos').first()).toBeVisible();
  });

  test('solicita nova comunidade e vincula a propriedade a ela', async ({ appLogado: page, produtor }) => {
    const comunidade = `Comunidade E2E ${unico()}`;
    const nome = `Roça E2E ${unico()}`;
    await page.goto('/propriedades/nova');
    await page.getByLabel('Nome da propriedade').fill(nome);
    await page.getByLabel('Tamanho (hectares)').fill('1');
    await page.getByLabel('Comunidade').selectOption({ label: '+ Minha comunidade não está na lista' });

    const dialogo = page.getByRole('dialog', { name: 'Solicitar Nova Comunidade' });
    await dialogo.getByLabel('Nome da Comunidade').fill(comunidade);
    await dialogo.getByLabel('Localidade').fill('Córrego Fundo');
    await dialogo.getByLabel('Município').fill('Mercês');
    await dialogo.getByLabel('UF').fill('MG');
    await dialogo.getByRole('button', { name: 'Enviar Solicitação' }).click();
    await expect(toast(page, 'Solicitação de comunidade enviada!')).toBeVisible();

    await page.getByLabel('Logradouro').fill('Córrego Fundo, S/N');
    await page.getByLabel('Município').fill('Mercês');
    await page.getByLabel('UF').fill('MG');
    await page.getByRole('button', { name: 'Cadastrar Propriedade' }).click();
    await expect(toast(page, 'Propriedade cadastrada!')).toBeVisible();
    await expect(page.getByText(nome)).toBeVisible();

    expect((await listarPropriedades(produtor))[0]).toMatchObject({ nome, comunidadeNome: comunidade });
  });

  test('comunidade pendente continua na lista ao voltar à tela', async ({ appLogado: page }) => {
    const comunidade = `Pendente E2E ${unico()}`;
    await page.goto('/propriedades/nova');
    await page.getByLabel('Comunidade').selectOption({ label: '+ Minha comunidade não está na lista' });
    const dialogo = page.getByRole('dialog', { name: 'Solicitar Nova Comunidade' });
    await dialogo.getByLabel('Nome da Comunidade').fill(comunidade);
    await dialogo.getByLabel('Localidade').fill('Córrego Fundo');
    await dialogo.getByLabel('Município').fill('Mercês');
    await dialogo.getByLabel('UF').fill('MG');
    await dialogo.getByRole('button', { name: 'Enviar Solicitação' }).click();
    await expect(toast(page, 'Solicitação de comunidade enviada!')).toBeVisible();

    // Sai da tela sem cadastrar a propriedade e volta: a pendente tem de continuar no select.
    await page.goto('/propriedades/nova');
    const opcoes = page.getByLabel('Comunidade').locator('option');
    await expect(opcoes.filter({ hasText: `${comunidade} — Mercês/MG (pendente)` })).toHaveCount(1);
    // Vem depois das ativas (a ativa do seeder aparece antes).
    const textos = await opcoes.allTextContents();
    expect(textos.findIndex((t) => t.includes('Quilombo dos Coelhos'))).toBeLessThan(textos.findIndex((t) => t.includes(comunidade)));
  });

  test('não cadastra sem os campos obrigatórios', async ({ appLogado: page }) => {
    await page.goto('/propriedades/nova');
    await page.getByRole('button', { name: 'Cadastrar Propriedade' }).click();

    await expect(page.getByRole('alert').filter({ hasText: 'Nome é obrigatório' })).toBeVisible();
    await expect(page.getByRole('alert').filter({ hasText: 'Município é obrigatório' })).toBeVisible();
    await expect(page).toHaveURL(/\/propriedades\/nova$/);
  });

  test('cadastra propriedade sem comunidade (produtor isolado)', async ({ appLogado: page, produtor }) => {
    const nome = `Sítio Isolado ${unico()}`;
    await page.goto('/propriedades/nova');
    await page.getByLabel('Nome da propriedade').fill(nome);
    await page.getByLabel('Tamanho (hectares)').fill('2');
    await expect(page.getByLabel('Comunidade')).toHaveValue('');
    await page.getByLabel('Logradouro').fill('Estrada da Serra, km 4');
    await page.getByLabel('Município').fill('Rio Pomba');
    await page.getByLabel('UF').fill('MG');
    await page.getByRole('button', { name: 'Cadastrar Propriedade' }).click();

    await expect(toast(page, 'Propriedade cadastrada!')).toBeVisible();
    await expect(page).toHaveURL(/\/propriedades$/);
    await expect(page.getByText('Sem comunidade')).toBeVisible();

    const salvas = await listarPropriedades(produtor);
    expect(salvas).toHaveLength(1);
    expect(salvas[0]).toMatchObject({ nome, comunidadeId: null, comunidadeNome: null });

    await page.getByText(nome).click();
    await expect(page.getByText('Sem comunidade')).toBeVisible();
  });
});
