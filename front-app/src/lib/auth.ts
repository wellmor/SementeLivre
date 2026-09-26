/**
 * Autenticação e conta do proprietário, via backend REST.
 */

import { ApiError, apiFetch, getSession, salvarSessao, Session, TokenResponse } from './api';
import { PerfilUsuario, PessoaUpdateRequest, Proprietario, ProprietarioCadastroRequest } from '@/types/user';

export type { Session } from './api';
export { getSession } from './api';

type AuthCallback = (session: Session | null) => void;
const listeners = new Set<AuthCallback>();

export function onAuthStateChanged(callback: AuthCallback): () => void {
  callback(getSession());
  listeners.add(callback);
  return () => {
    listeners.delete(callback);
  };
}

function notifyListeners(): void {
  const session = getSession();
  listeners.forEach((cb) => cb(session));
}

export function cadastrarProprietario(dados: ProprietarioCadastroRequest): Promise<PerfilUsuario> {
  return apiFetch<PerfilUsuario>('/auth/cadastrar', { method: 'POST', body: dados, autenticado: false });
}

export async function buscarPerfil(): Promise<PerfilUsuario> {
  const perfil = await apiFetch<PerfilUsuario>('/auth/me');
  const session = getSession();
  if (session && (session.uid !== perfil.id || session.email !== perfil.email)) {
    salvarSessao({ ...session, uid: perfil.id, email: perfil.email });
  }
  return perfil;
}

export async function entrar(email: string, senha: string): Promise<PerfilUsuario> {
  const tokens = await apiFetch<TokenResponse>('/auth/login', {
    method: 'POST',
    body: { email, senha },
    autenticado: false,
  });
  salvarSessao({ uid: '', email, accessToken: tokens.accessToken, refreshToken: tokens.refreshToken });

  try {
    const perfil = await buscarPerfil();
    if (perfil.tipoPessoa !== 'PROPRIETARIO') {
      throw new ApiError(403, 'Esta conta não é de proprietário. Use o painel administrativo.');
    }
    notifyListeners();
    return perfil;
  } catch (err) {
    salvarSessao(null);
    throw err;
  }
}

export function sair(): void {
  salvarSessao(null);
  notifyListeners();
}

export function atualizarPerfil(id: string, dados: PessoaUpdateRequest): Promise<Proprietario> {
  return apiFetch<Proprietario>(`/api/proprietarios/${id}`, { method: 'PUT', body: dados });
}

export function alterarSenha(senhaAtual: string, novaSenha: string): Promise<{ mensagem: string }> {
  return apiFetch<{ mensagem: string }>('/auth/alterar-senha', {
    method: 'POST',
    body: { senhaAtual, novaSenha },
  });
}

export function sendPasswordResetEmail(_email: string): void {
  // Ainda não integrado ao backend (POST /auth/recuperar-senha).
}
