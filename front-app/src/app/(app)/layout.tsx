'use client';

import React, { useEffect } from 'react';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { NotificationProvider } from '@/context/NotificationContext';
import { Header } from '@/components/layout/Header';
import { BottomNavigation } from '@/components/layout/BottomNavigation';
import { SincronizarMovimentacoes } from '@/components/offline/SincronizarMovimentacoes';
import { Button } from '@/components/ui/button';
import { usePathname, useRouter } from 'next/navigation';
import styles from './app.module.css';

const headerConfig: Record<string, { title: string; showBack?: boolean; showNotifications?: boolean }> = {
  '/dashboard': { title: 'Semente Livre', showNotifications: true },
  '/produtos': { title: 'Meus Produtos', showNotifications: true },
  '/produtos/nova': { title: 'Cadastrar Produto', showBack: true },
  '/pedidos': { title: 'Pedidos', showNotifications: true },
  '/pedidos/novo': { title: 'Novo Pedido', showBack: true },
  '/propriedades': { title: 'Propriedades', showNotifications: true },
  '/propriedades/nova': { title: 'Nova Propriedade', showBack: true },
  '/relatorios': { title: 'Relatórios', showBack: true },
  '/notificacoes': { title: 'Notificações', showBack: true },
  '/perfil': { title: 'Meu Perfil', showNotifications: true },
  '/privacidade': { title: 'Política de Privacidade', showBack: true },
  '/termos': { title: 'Termos de Uso', showBack: true },
  '/admin/comunidades': { title: 'Aprovar Comunidades' },
  '/admin/administradores': { title: 'Administradores', showBack: true },
};

// O admin não tem cadastro de proprietário: as telas de produtos, pedidos, propriedades,
// relatórios e notificações chamam endpoints do proprietário e ficariam vazias ou com erro.
// Ele fica na área de administração, no perfil e nas telas informativas.
const INICIO_DO_ADMIN = '/admin/comunidades';
const ROTAS_DO_ADMIN = ['/admin', '/perfil', '/termos', '/privacidade', '/ajuda'];

function adminPodeAbrir(pathname: string): boolean {
  return ROTAS_DO_ADMIN.some((rota) => pathname === rota || pathname.startsWith(rota + '/'));
}

function AppLayoutInner({ children }: { children: React.ReactNode }) {
  const { user, loading, semConexao } = useAuth();
  const router = useRouter();
  const pathname = usePathname();
  const admin = !!user?.admin;
  const adminForaDaArea = admin && !adminPodeAbrir(pathname);

  useEffect(() => {
    if (!loading && !user && !semConexao) {
      router.replace('/entrar');
    }
  }, [loading, user, semConexao, router]);

  useEffect(() => {
    if (adminForaDaArea) router.replace(INICIO_DO_ADMIN);
  }, [adminForaDaArea, router]);

  if (loading) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center' }}>
        <p style={{ color: 'var(--color-text-muted)', fontFamily: 'sans-serif' }}>Carregando sessão...</p>
      </div>
    );
  }

  // Sessão salva, mas o backend não respondeu: não desloga, oferece tentar de novo.
  if (!user && semConexao) {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', padding: 'var(--content-padding)' }}>
        <p style={{ color: 'var(--color-text-muted)', textAlign: 'center' }}>Não foi possível falar com o servidor.</p>
        <Button variant="primary" onClick={() => window.location.reload()}>Tentar novamente</Button>
      </div>
    );
  }

  // Admin numa tela de proprietário: não monta a tela (nem as chamadas dela) enquanto redireciona
  if (!user || adminForaDaArea) {
    return null;
  }

  const config = Object.entries(headerConfig).find(([key]) =>
    pathname === key || (key !== '/produtos' && key !== '/pedidos' && key !== '/propriedades' && pathname.startsWith(key + '/'))
  )?.[1] || { title: 'Semente Livre', showBack: true };

  return (
    <div className={styles.appShell}>
      <Header
        title={config.title}
        showBack={config.showBack}
        // Notificações são de pedidos do proprietário; o admin tem o atalho do perfil no lugar da barra de baixo
        showNotifications={config.showNotifications && !admin}
        showPerfil={admin && pathname !== '/perfil'}
      />
      <main className={admin ? `${styles.content} ${styles.contentSemNav}` : styles.content}>
        {children}
      </main>
      {!admin && <BottomNavigation />}
      <SincronizarMovimentacoes />
    </div>
  );
}

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider>
      <NotificationProvider>
        <AppLayoutInner>{children}</AppLayoutInner>
      </NotificationProvider>
    </AuthProvider>
  );
}
