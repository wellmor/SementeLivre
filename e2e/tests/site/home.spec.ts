import { test, expect } from '@playwright/test';

/** Página inicial do site: carrossel das comunidades (#181). */
test.describe('Página inicial (front-site)', () => {
  // O app ainda não está publicado (nem na Play Store): o site não leva a ele
  test('página inicial não tem link nem seção do app', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Produtos Disponíveis' })).toBeVisible();
    await expect(page.getByRole('link', { name: /app/i })).toHaveCount(0);
    await expect(page.getByRole('heading', { name: /Use o app/ })).toHaveCount(0);
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
