'use client';

import React from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Bell, BellDot, Leaf, User } from 'lucide-react';
import styles from './Header.module.css';
import { useNotifications } from '@/context/NotificationContext';

interface HeaderProps {
  title: string;
  showBack?: boolean;
  actions?: React.ReactNode;
  showNotifications?: boolean;
  /** Atalho para o perfil, para quem não tem a barra de navegação de baixo (admin) */
  showPerfil?: boolean;
}

export function Header({ title, showBack = false, actions, showNotifications = false, showPerfil = false }: HeaderProps) {
  const router = useRouter();
  const { unreadCount } = useNotifications();

  return (
    <header className={styles.header} role="banner">
      <div className={styles.inner}>
        <div className={styles.left}>
          {showBack ? (
            <button
              type="button"
              onClick={() => router.back()}
              className={styles.backBtn}
              aria-label="Voltar"
            >
              <ArrowLeft size={18} strokeWidth={2.5} />
            </button>
          ) : (
            <div className={styles.logoMark} aria-hidden="true">
              <Leaf size={16} strokeWidth={2.5} />
            </div>
          )}
          <h1 className={styles.title}>{title}</h1>
        </div>
        <div className={styles.right}>
          {actions}
          {showPerfil && (
            <Link href="/perfil" className={styles.notifBtn} aria-label="Meu perfil">
              <User size={19} strokeWidth={2} />
            </Link>
          )}
          {showNotifications && (
            <Link
              href="/notificacoes"
              className={styles.notifBtn}
              aria-label={`Notificações${unreadCount > 0 ? `, ${unreadCount} não lidas` : ''}`}
            >
              {unreadCount > 0 ? (
                <BellDot size={19} strokeWidth={2} className={styles.bellActive} />
              ) : (
                <Bell size={19} strokeWidth={2} />
              )}
              {unreadCount > 0 && (
                <span className={styles.badge} aria-hidden="true">
                  {unreadCount > 9 ? '9+' : unreadCount}
                </span>
              )}
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}
