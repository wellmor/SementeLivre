/**
 * Cliente HTTP do backend. As chamadas passam pelo proxy /api/backend/[...path],
 * que repassa para o BACKEND_URL. A sessão (tokens JWT) fica no localStorage.
 */

const SESSION_KEY = 'sl_auth_session';

export interface Session {
  uid: string;
  email: string;
  accessToken: string;
  refreshToken: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export function getSession(): Session | null {
  if (typeof window === 'undefined') return null;
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? (JSON.parse(raw) as Session) : null;
  } catch {
    return null;
  }
}

export function salvarSessao(session: Session | null): void {
  if (session) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  } else {
    localStorage.removeItem(SESSION_KEY);
  }
}

/** Erro devolvido pela API. `fieldErrors` vem indexado pelo campo do DTO (ex.: "email", "endereco.uf"). */
export class ApiError extends Error {
  status: number;
  fieldErrors: Record<string, string>;

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

interface ErrorResponse {
  message?: string;
  error?: string;
  fieldErrors?: string[] | null;
}

// O backend manda fieldErrors como "campo: mensagem".
async function lerErro(response: Response): Promise<ApiError> {
  let corpo: ErrorResponse = {};
  try {
    corpo = await response.json();
  } catch {
    // resposta sem corpo JSON
  }

  const fieldErrors: Record<string, string> = {};
  for (const item of corpo.fieldErrors ?? []) {
    const separador = item.indexOf(': ');
    if (separador > 0) {
      const campo = item.slice(0, separador);
      fieldErrors[campo] ??= item.slice(separador + 2);
    }
  }

  return new ApiError(
    response.status,
    corpo.message ?? corpo.error ?? 'Ocorreu um erro inesperado. Tente novamente.',
    fieldErrors
  );
}

let renovacaoEmAndamento: Promise<boolean> | null = null;

/** Troca o refresh token por um novo par de tokens. Chamadas simultâneas compartilham a mesma renovação. */
export function renovarSessao(): Promise<boolean> {
  renovacaoEmAndamento ??= (async () => {
    const session = getSession();
    if (!session) return false;
    try {
      const response = await fetch('/api/backend/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: session.refreshToken }),
      });
      if (!response.ok) return false;
      const tokens: TokenResponse = await response.json();
      salvarSessao({ ...session, accessToken: tokens.accessToken, refreshToken: tokens.refreshToken });
      return true;
    } catch {
      return false;
    }
  })().finally(() => {
    renovacaoEmAndamento = null;
  });
  return renovacaoEmAndamento;
}

interface ApiOptions {
  method?: string;
  body?: unknown;
  /** Envia o token e trata 401 como sessão expirada. Padrão: true. */
  autenticado?: boolean;
}

export async function apiFetch<T>(path: string, { method = 'GET', body, autenticado = true }: ApiOptions = {}): Promise<T> {
  const enviar = () => {
    const headers: Record<string, string> = {};
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    const session = getSession();
    if (autenticado && session) headers.Authorization = `Bearer ${session.accessToken}`;
    return fetch(`/api/backend${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  };

  let response: Response;
  try {
    response = await enviar();
    // Token expirado (ou e-mail trocado, que invalida o token): renova e tenta de novo uma vez.
    if (response.status === 401 && autenticado && getSession() && (await renovarSessao())) {
      response = await enviar();
    }
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor. Verifique sua conexão.');
  }

  if (response.status === 401 && autenticado) {
    salvarSessao(null);
    window.location.assign('/entrar?sessao=expirada');
  }

  if (!response.ok) throw await lerErro(response);

  const texto = await response.text();
  return (texto ? JSON.parse(texto) : undefined) as T;
}
