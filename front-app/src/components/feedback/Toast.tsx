'use client';

import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { TOAST_EVENT, ToastEventDetail, ToastType } from '@/lib/toast';
import styles from './Toast.module.css';

interface Toast {
  id: string;
  type: ToastType;
  message: string;
}

interface ToastContextType {
  showToast: (message: string, type?: ToastType) => void;
}

const ToastContext = createContext<ToastContextType>({ showToast: () => {} });

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const showToast = useCallback((message: string, type: ToastType = 'success') => {
    const id = Math.random().toString(36).slice(2);
    // Várias requisições falhando juntas (ex.: sem conexão) não empilham a mesma mensagem.
    setToasts((prev) => (prev.some((t) => t.message === message) ? prev : [...prev, { id, type, message }]));
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000);
  }, []);

  // Toasts disparados fora do React (lib/toast.ts), ex.: pelo cliente HTTP.
  useEffect(() => {
    const handler = (e: Event) => {
      const { message, type } = (e as CustomEvent<ToastEventDetail>).detail;
      showToast(message, type);
    };
    window.addEventListener(TOAST_EVENT, handler);
    return () => window.removeEventListener(TOAST_EVENT, handler);
  }, [showToast]);

  const icons: Record<ToastType, string> = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ',
  };

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      <div
        className={styles.container}
        aria-live="polite"
        aria-atomic="false"
        role="region"
        aria-label="Notificações"
      >
        {toasts.map((t) => (
          <div
            key={t.id}
            className={`${styles.toast} ${styles[t.type]}`}
            role={t.type === 'error' ? 'alert' : 'status'}
          >
            <span className={styles.icon} aria-hidden="true">{icons[t.type]}</span>
            <span>{t.message}</span>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}
