import { request } from '@playwright/test';
import { API_URL, gerarCpf, unico } from './api';

/** Falha cedo, com mensagem clara, se o backend real não estiver no ar. */
export default async function globalSetup() {
  const api = await request.newContext({ baseURL: API_URL });
  try {
    const res = await api.get('/actuator/health');
    if (!res.ok()) throw new Error(`status ${res.status()}`);
  } catch (err) {
    throw new Error(
      `Backend fora do ar em ${API_URL} (${(err as Error).message}). Suba o backend antes dos testes: veja e2e/README.md.`
    );
  } finally {
    await api.dispose();
  }
  await criarSuperAdminDosTestes();
}

/**
 * Administrador principal só desta execução, para administradores.spec.ts.
 *
 * Cada login apaga os refresh tokens anteriores da conta: se esse spec também
 * usasse o admin do seed, derrubaria a sessão de aprovar-comunidades.spec.ts
 * rodando em paralelo. Aqui o admin do seed entra uma vez só, antes de tudo.
 * Sem E2E_ADMIN_SENHA (ou se o login falhar) as variáveis ficam vazias e os
 * testes que dependem delas são pulados.
 */
async function criarSuperAdminDosTestes() {
  const senhaDoSeed = process.env.E2E_ADMIN_SENHA;
  if (!senhaDoSeed) return;
  const api = await request.newContext({ baseURL: API_URL });
  try {
    const login = await api.post('/auth/login', {
      data: { email: process.env.E2E_ADMIN_EMAIL ?? 'admin@sementelivre.com.br', senha: senhaDoSeed },
    });
    if (!login.ok()) return;
    const { accessToken } = await login.json();

    const email = `e2e-super-${unico()}@sementelivre.test`;
    const senha = 'SuperE2e123';
    const criado = await api.post('/admin/administradores', {
      headers: { Authorization: `Bearer ${accessToken}` },
      data: { nome: 'Principal E2E', email, tipoDocumento: 'CPF', documento: gerarCpf(), senha, nivelAcesso: 'SUPER_ADMIN' },
    });
    if (!criado.ok()) return;
    // Variáveis definidas no globalSetup chegam aos workers
    process.env.E2E_SUPER_ADMIN_EMAIL = email;
    process.env.E2E_SUPER_ADMIN_SENHA = senha;
  } finally {
    await api.dispose();
  }
}
