/**
 * authApi.ts — Funções que integram com os endpoints de autenticação do backend.
 *
 * Endpoints consumidos:
 *  POST /auth/login             → retorna { accessToken, refreshToken, tokenType, expiresInSeconds }
 *  POST /auth/cadastrar         → retorna UsuarioResponseDTO (201 Created)
 *  POST /auth/refresh           → retorna { accessToken, refreshToken, tokenType, expiresInSeconds }
 *  POST /auth/recuperar-senha   → retorna { mensagem: string }
 *  GET  /auth/me                → retorna UsuarioResponseDTO (usuário autenticado)
 */

import { apiGet, apiPost, saveTokens, clearTokens } from './api';

// ── DTOs (espelham os Records do backend) ─────────────────────────────────────

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface UsuarioResponse {
  id: string;
  nome: string;
  email: string;
  telefone?: string;
  tipoDocumento?: string;
  documento?: string;
  tipoPessoa?: string;
  roles?: string[];
  dataCadastro?: string;
  dataUltimaAlteracao?: string;
  endereco?: {
    logradouro: string;
    numero: string;
    complemento?: string;
    bairro: string;
    municipio: string;
    uf: string;
    cep: string;
  };
}

// ── Auth Session (o que guardamos na sessão local após login) ─────────────────

export interface AuthSession {
  uid: string;
  email: string;
  nome: string;
}

// ── login ─────────────────────────────────────────────────────────────────────

export async function loginApi(email: string, senha: string): Promise<AuthSession> {
  const data = await apiPost<TokenResponse>('/auth/login', { email, senha }, { public: true });
  saveTokens(data.accessToken, data.refreshToken);

  // Obtém dados do usuário autenticado para montar a sessão
  const me = await apiGet<UsuarioResponse>('/auth/me');
  return { uid: me.id, email: me.email, nome: me.nome };
}

// ── cadastrar ─────────────────────────────────────────────────────────────────

export interface CadastroPayload {
  nome: string;
  email: string;
  senha: string;
}

export async function cadastrarApi(payload: CadastroPayload): Promise<UsuarioResponse> {
  return apiPost<UsuarioResponse>('/auth/cadastrar', payload, { public: true });
}

// ── recuperar-senha ───────────────────────────────────────────────────────────

export async function recuperarSenhaApi(email: string): Promise<void> {
  await apiPost<{ mensagem: string }>('/auth/recuperar-senha', { email }, { public: true });
}

// ── logout (limpa tokens localmente) ─────────────────────────────────────────

export function logoutApi(): void {
  clearTokens();
}
