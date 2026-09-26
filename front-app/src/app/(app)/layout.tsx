'use client';

import React, { useEffect } from 'react';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { NotificationProvider } from '@/context/NotificationContext';
import { Header } from '@/components/layout/Header';
import { BottomNavigation } from '@/components/layout/BottomNavigation';
import { ToastProvider } from '@/components/feedback/Toast';
import { usePathname, useRouter } from 'next/navigation';
import styles from './app.module.css';

const headerConfig: Record<string, { title: string; showBack?: boolean; showNotifications?: boolean }> = {
  '/dashboard': { title: 'Semente Livre', showNotifications: true },
  '/sementes': { title: 'Meus Produtos', showNotifications: true },
  '/sementes/nova': { title: 'Cadastrar Produto', showBack: true },
  '/pedidos': { title: 'Pedidos', showNotifications: true },
  '/pedidos/novo': { title: 'Novo Pedido', showBack: true },
  '/propriedades': { title: 'Propriedades', showNotifications: true },
  '/propriedades/nova': { title: 'Nova Propriedade', showBack: true },
  '/relatorios': { title: 'Relatórios', showBack: true },
  '/notificacoes': { title: 'Notificações', showBack: true },
  '/perfil': { title: 'Meu Perfil', showNotifications: true },
};

function AppLayoutInner({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!loading && !user) {
      router.replace('/entrar');
    }
  }, [loading, user, router]);

  if (loading) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center' }}>
        <p style={{ color: 'var(--color-text-muted)', fontFamily: 'sans-serif' }}>Carregando sessão...</p>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  const config = Object.entries(headerConfig).find(([key]) =>
    pathname === key || (key !== '/sementes' && key !== '/pedidos' && key !== '/propriedades' && pathname.startsWith(key + '/'))
  )?.[1] || { title: 'Semente Livre', showBack: true };

  return (
    <div className={styles.appShell}>
      <Header
        title={config.title}
        showBack={config.showBack}
        showNotifications={config.showNotifications}
      />
      <main className={styles.content}>
        {children}
      </main>
      <BottomNavigation />
    </div>
  );
}

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider>
      <NotificationProvider>
        <ToastProvider>
          <AppLayoutInner>{children}</AppLayoutInner>
        </ToastProvider>
      </NotificationProvider>
    </AuthProvider>
  );
}
