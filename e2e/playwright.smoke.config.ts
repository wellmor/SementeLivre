import { defineConfig, devices } from '@playwright/test';

/**
 * Smoke tests de produção (#138): passam pelos fluxos críticos contra um ambiente
 * JÁ NO AR. Diferente do playwright.config.ts, não sobe frontend nenhum.
 *
 *   API_URL=https://api.exemplo APP_URL=https://app.exemplo SITE_URL=https://exemplo npm run test:smoke
 *
 * Criam uma conta "Smoke" com oferta, propriedade e pedido e apagam tudo no final
 * (exclusão de conta). Ver docs/testes/smoke-tests.md.
 */
export default defineConfig({
  testDir: './smoke',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 60_000,
  expect: { timeout: 15_000 },
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'smoke-report' }]],
  use: {
    ...devices['Pixel 7'],
    locale: 'pt-BR',
    timezoneId: 'America/Sao_Paulo',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
});
