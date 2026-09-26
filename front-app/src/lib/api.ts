/**
 * api.ts — Cliente HTTP centralizado para o backend REST (Spring Boot).
 *
 * Responsabilidades:
 *  - Anexar o accessToken JWT em cada requisição autenticada.
 *  - Tentar renovar o accessToken via /auth/refresh quando receber 401.
 *  - Persistir / limpar tokens no localStorage de forma padronizada.
 *  - Exportar helpers tipados: apiGet, apiPost, apiPut, apiDelete.
 */

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

// ── Chaves de armazenamento ────────────────────────────────────────────────────

const TOKEN_KEY = 'sl_access_token';
const REFRESH_KEY = 'sl_refresh_token';

// ── Persistência de tokens ─────────────────────────────────────────────────────

export function getAccessToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(REFRESH_KEY);
}

export function saveTokens(accessToken: string, refreshToken: string): void {
  localStorage.setItem(TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_KEY, refreshToken);
}

export function clearTokens(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_KEY);
}

// ── Tipos auxiliares ───────────────────────────────────────────────────────────

export interface ApiError {
  status: number;
  message: string;
  /** Código semântico de erro retornado pelo backend, ex: "auth/invalid-credential" */
  code?: string;
}

function buildApiError(status: number, body: unknown): ApiError {
  if (body && typeof body === 'object') {
    const b = body as Record<string, unknown>;
    return {
      status,
      message: (b.mensagem ?? b.message ?? b.error ?? 'Erro desconhecido') as string,
      code: b.code as string | undefined,
    };
  }
  return { status, message: String(body ?? 'Erro desconhecido') };
}

// ── Refresh silencioso ─────────────────────────────────────────────────────────

let _refreshPromise: Promise<string | null> | null = null;

async function silentRefresh(): Promise<string | null> {
  // Evitar múltiplas chamadas simultâneas de refresh
  if (_refreshPromise) return _refreshPromise;

  _refreshPromise = (async () => {
    const refreshToken = getRefreshToken();
    if (!refreshToken) return null;

    try {
      const res = await fetch(`${BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });

      if (!res.ok) {
        clearTokens();
        return null;
      }

      const data = await res.json();
      saveTokens(data.accessToken, data.refreshToken ?? refreshToken);
      return data.accessToken as string;
    } catch {
      clearTokens();
      return null;
    } finally {
      _refreshPromise = null;
    }
  })();

  return _refreshPromise;
}

// ── Fetch base ─────────────────────────────────────────────────────────────────

interface RequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown;
  /** Se true, não envia o header Authorization (para rotas públicas) */
  public?: boolean;
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { body, public: isPublic, headers: extraHeaders, ...rest } = options;

  const buildHeaders = (token: string | null): HeadersInit => {
    const h: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(extraHeaders as Record<string, string>),
    };
    if (!isPublic && token) {
      h['Authorization'] = `Bearer ${token}`;
    }
    return h;
  };

  const doFetch = (token: string | null) =>
    fetch(`${BASE_URL}${path}`, {
      ...rest,
      headers: buildHeaders(token),
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });

  let token = getAccessToken();
  let res = await doFetch(token);

  // ── Se 401, tenta refresh uma vez ──────────────────────────────────────────
  if (res.status === 401 && !isPublic) {
    const newToken = await silentRefresh();
    if (!newToken) {
      clearTokens();
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new Event('sl:session-expired'));
      }
      throw buildApiError(401, { message: 'Sessão expirada. Faça login novamente.', code: 'auth/session-expired' });
    }
    token = newToken;
    res = await doFetch(token);
  }

  // ── Tratar erros HTTP ──────────────────────────────────────────────────────
  if (!res.ok) {
    let errorBody: unknown;
    try {
      errorBody = await res.json();
    } catch {
      errorBody = await res.text();
    }
    throw buildApiError(res.status, errorBody);
  }

  if (res.status === 204) return undefined as unknown as T;

  return res.json() as Promise<T>;
}

// ── Helpers públicos ───────────────────────────────────────────────────────────

export function apiGet<T>(path: string, options?: RequestOptions): Promise<T> {
  return request<T>(path, { ...options, method: 'GET' });
}

export function apiPost<T>(path: string, body?: unknown, options?: RequestOptions): Promise<T> {
  return request<T>(path, { ...options, method: 'POST', body });
}

export function apiPut<T>(path: string, body?: unknown, options?: RequestOptions): Promise<T> {
  return request<T>(path, { ...options, method: 'PUT', body });
}

export function apiDelete<T>(path: string, options?: RequestOptions): Promise<T> {
  return request<T>(path, { ...options, method: 'DELETE' });
}
