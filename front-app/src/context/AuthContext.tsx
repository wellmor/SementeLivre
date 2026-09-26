'use client';

import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import { buscarPerfil, onAuthStateChanged, sair, Session } from '@/lib/auth';
import { ApiError } from '@/lib/api';
import { PerfilUsuario } from '@/types/user';

interface AuthContextType {
  user: Session | null;
  /** Perfil da conta logada, vindo de GET /auth/me. */
  proprietario: PerfilUsuario | null;
  loading: boolean;
  erroPerfil: string;
  recarregarPerfil: () => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  proprietario: null,
  loading: true,
  erroPerfil: '',
  recarregarPerfil: async () => {},
  logout: async () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<Session | null>(null);
  const [proprietario, setProprietario] = useState<PerfilUsuario | null>(null);
  const [loading, setLoading] = useState(true);
  const [erroPerfil, setErroPerfil] = useState('');
  const inactivityTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const resetTimer = () => {
    if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
    inactivityTimer.current = setTimeout(() => {
      sair();
    }, 30 * 60 * 1000); // 30 min
  };

  const recarregarPerfil = useCallback(async () => {
    setErroPerfil('');
    try {
      setProprietario(await buscarPerfil());
    } catch (err) {
      setErroPerfil(err instanceof ApiError ? err.message : 'Não foi possível carregar o perfil.');
    }
  }, []);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(async (session) => {
      setUser(session);
      if (session) {
        await recarregarPerfil();
        resetTimer();
      } else {
        setProprietario(null);
      }
      setLoading(false);
    });

    const events = ['mousedown', 'keydown', 'touchstart', 'scroll'];
    events.forEach((e) => document.addEventListener(e, resetTimer));

    return () => {
      unsubscribe();
      if (inactivityTimer.current) clearTimeout(inactivityTimer.current);
      events.forEach((e) => document.removeEventListener(e, resetTimer));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const logout = async () => {
    sair();
  };

  return (
    <AuthContext.Provider value={{ user, proprietario, loading, erroPerfil, recarregarPerfil, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
