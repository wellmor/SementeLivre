'use client';

/**
 * admin/layout.tsx — Guarda das telas de administração.
 *
 * O layout de (app) já garante a sessão; aqui só entra conta de admin (ROLE_ADMIN).
 * Proprietário que abrir /admin/... volta ao painel com um aviso. O backend
 * continua barrando as ações (@PreAuthorize), isto só evita a tela quebrada.
 */

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/components/feedback/Toast';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  const { showToast } = useToast();
  const router = useRouter();
  const permitido = !!user?.admin;

  useEffect(() => {
    if (user && !user.admin) {
      showToast('Área restrita ao administrador.', 'warning');
      router.replace('/dashboard');
    }
  }, [user, router, showToast]);

  if (!permitido) return null;

  return <>{children}</>;
}
