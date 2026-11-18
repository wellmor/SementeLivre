import { test, expect } from '@playwright/test';

// O mesmo valor que o build do front-site recebeu (o CI define NEXT_PUBLIC_APP_URL)
const APP_URL = process.env.NEXT_PUBLIC_APP_URL ?? 'http://localhost:3000';

/** Página inicial do site: link do app e carrossel das comunidades (#181). */
test.describe('Página inicial (front-site)', () => {
  test('"Acessar o app" do cabeçalho e do "Sobre a Rede" usa NEXT_PUBLIC_APP_URL', async ({ page }) => {
    await page.goto('/');

    const cabecalho = page.getByRole('banner').getByRole('link', { name: 'Acessar o app' });
    const sobre = page.locator('#sobre').getByRole('link', { name: 'Acessar o app' });
    await expect(cabecalho).toHaveAttribute('href', APP_URL);
    await expect(sobre).toHaveAttribute('href', APP_URL);
  });

  test('o botão do cabeçalho aparece no celular e leva à tela de login do app', async ({ page }) => {
    await page.setViewportSize({ width: 360, height: 800 });
    await page.goto('/');

    const botao = page.getByRole('banner').getByRole('link', { name: 'Acessar o app' });
    await expect(botao).toBeVisible();
    await botao.click();
    // Sem sessão no app, a raiz manda para /entrar
    await expect(page).toHaveURL(`${APP_URL.replace(/\/$/, '')}/entrar`);
  });

  test('carrossel troca de foto pelos botões e pelos indicadores', async ({ page }) => {
    await page.goto('/');
    const carrossel = page.getByRole('region', { name: 'Fotos das comunidades' });
    await carrossel.scrollIntoViewIfNeeded();

    await expect(carrossel).toHaveAttribute('aria-roledescription', 'carrossel');
    await expect(carrossel.getByRole('group', { name: '1 de 5' })).toBeVisible();

    await carrossel.getByRole('button', { name: 'Próxima foto' }).click();
    await expect(carrossel.getByRole('group', { name: '2 de 5' })).toBeVisible();
    await expect(carrossel.getByRole('group', { name: '1 de 5' })).toBeHidden();

    await carrossel.getByRole('button', { name: 'Foto anterior' }).click();
    await carrossel.getByRole('button', { name: 'Foto anterior' }).click();
    await expect(carrossel.getByRole('group', { name: '5 de 5' })).toBeVisible();

    const indicador = carrossel.getByRole('button', { name: /^Mostrar foto 3/ });
    await indicador.click();
    await expect(carrossel.getByRole('group', { name: '3 de 5' })).toBeVisible();
    await expect(indicador).toHaveAttribute('aria-current', 'true');
  });

  test('carrossel gira sozinho e o botão de pausa para a rotação', async ({ page }) => {
    await page.clock.install();
    await page.goto('/');
    const carrossel = page.getByRole('region', { name: 'Fotos das comunidades' });
    await expect(carrossel.getByRole('group', { name: '1 de 5' })).toBeVisible();

    // Girando sozinho, a região não é anunciada a cada troca
    await expect(page.locator('#carrossel-comunidades-slides')).toHaveAttribute('aria-live', 'off');
    await page.clock.runFor(5_100);
    await expect(carrossel.getByRole('group', { name: '2 de 5' })).toBeVisible();

    await carrossel.getByRole('button', { name: 'Pausar rotação automática das fotos' }).click();
    await page.mouse.move(0, 0);
    await page.locator('body').click({ position: { x: 1, y: 1 } });
    await page.clock.runFor(15_000);
    await expect(carrossel.getByRole('group', { name: '2 de 5' })).toBeVisible();
    await expect(page.locator('#carrossel-comunidades-slides')).toHaveAttribute('aria-live', 'polite');
    await expect(carrossel.getByRole('button', { name: 'Iniciar rotação automática das fotos' })).toBeVisible();
  });

  test('quem pediu menos movimento não vê o carrossel girar', async ({ page }) => {
    await page.emulateMedia({ reducedMotion: 'reduce' });
    await page.clock.install();
    await page.goto('/');
    const carrossel = page.getByRole('region', { name: 'Fotos das comunidades' });

    await page.clock.runFor(15_000);
    await expect(carrossel.getByRole('group', { name: '1 de 5' })).toBeVisible();
  });

  test('no celular a página não rola para o lado', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/');
    await page.getByRole('region', { name: 'Fotos das comunidades' }).scrollIntoViewIfNeeded();

    const rolaParaOLado = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth);
    expect(rolaParaOLado).toBe(false);
  });
});
