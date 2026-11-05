import type { BrowserContext, Page } from '@playwright/test';
import { expect } from '@playwright/test';

/**
 * Sem internet de verdade, inclusive para o Service Worker do PWA (#234).
 *
 * O context.setOffline sozinho não alcança as requisições que o Service Worker faz;
 * o route as derruba também (PW_EXPERIMENTAL_SERVICE_WORKER_NETWORK_EVENTS, ligado
 * no playwright.config.ts).
 */
export async function ficarOffline(context: BrowserContext) {
  await context.route('**/*', (rota) => rota.abort('internetdisconnected'));
  await context.setOffline(true);
}

export async function voltarOnline(context: BrowserContext) {
  await context.unrouteAll({ behavior: 'ignoreErrors' });
  await context.setOffline(false);
}

/** Espera o Service Worker controlar a página e terminar de guardar as telas principais. */
export async function esperarServiceWorker(page: Page) {
  await page.evaluate(() => navigator.serviceWorker.ready);
  await expect
    .poll(
      () =>
        page.evaluate(async () => {
          const telas = await caches.open('sl-paginas-v1');
          return Boolean(navigator.serviceWorker.controller) && Boolean(await telas.match('/dashboard'));
        }),
      { timeout: 30_000 }
    )
    .toBe(true);
}
