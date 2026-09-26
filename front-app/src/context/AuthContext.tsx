'use client';

/**
 * AuthContext.tsx — Contexto de autenticação integrado ao backend REST.
 *
 * ALTERAÇÕES em relação à versão anterior (localStorage mock):
 *   - Importa `loginApi`, `logoutApi`, `AuthSession` de `@/lib/authApi` em vez de `@/lib/auth`
 *   - `user` agora é do tipo `AuthSession` (uid + email + nome) em vez de `Session`
 *   - A inicialização da sessão lê o accessToken do localStorage e chama GET /auth/me
 *     para revalidar a sessão de forma segura
 *   - `logout` chama `logoutApi()` que limpa os tokens JWT
 *   - Escuta o evento customizado `sl:session-expired` disparado por `api.ts`
 *     para fazer logout automático quando o refresh falha
 *
 * LINHAS REMOVIDAS do arquivo anterior (comentadas para auditoria):
 *   - import { onAuthStateChanged, signOutAndNotify, Session } from '@/lib/auth';
 *   - import { dbGet } from '@/lib/db';
 *   - import { Proprietario } from '@/types/user';
 *   - const prop = dbGet<Proprietario & { id: string }>('proprietarios', session.uid);
 *   - setProprietario({ ...prop, idProprietario: prop.id });
 *   - Toda lógica de inatividade (resetTimer / 30 min) é mantida sem mudanças
 */

import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { AuthSession, logoutApi, UsuarioResponse } from '@/lib/authApi';
import { apiGet, getAccessToken } from '@/lib/api';

interface AuthContextType {
  user: AuthSession | null;
  loading: boolean;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  loading: true,
  logout: async () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthSession | null>(null);
  const [loading, setLoading] = useState(true);
  const inactivityTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const doLogout = () => {
    logoutApi();
    setUser(null);
  };

  const resetTimer = () => {
    if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
    inactivityTimer.current = setTimeout(() => {
      doLogout();
    }, 30 * 60 * 1000); // 30 min de inatividade
  };

  // ── Revalidar sessão na montagem ──────────────────────────────────────────
  useEffect(() => {
    let cancelled = false;

    const init = async () => {
      const token = getAccessToken();
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        const me = await apiGet<UsuarioResponse>('/auth/me');
        if (!cancelled) {
          setUser({ uid: me.id, email: me.email, nome: me.nome });
          resetTimer();
        }
      } catch {
        // Token inválido ou expirado sem refresh → limpa estado
        if (!cancelled) {
          logoutApi();
          setUser(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    init();

    // Escuta evento de sessão expirada disparado por api.ts
    const handleExpired = () => {
      setUser(null);
    };
    window.addEventListener('sl:session-expired', handleExpired);

    const events = ['mousedown', 'keydown', 'touchstart', 'scroll'];
    events.forEach((e) => document.addEventListener(e, resetTimer));

    return () => {
      cancelled = true;
      if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
      window.removeEventListener('sl:session-expired', handleExpired);
      events.forEach((e) => document.removeEventListener(e, resetTimer));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const logout = async () => {
    doLogout();
  };

  return (
    <AuthContext.Provider value={{ user, loading, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
