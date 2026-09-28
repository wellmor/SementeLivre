'use client';

import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import {
  listarNotificacoes,
  marcarNotificacaoLida,
  type NotificacaoDTO,
} from '@/lib/pedidoApi';
import { Notificacao } from '@/types/notification';
import { useAuth } from './AuthContext';

interface NotificationContextType {
  notifications: Notificacao[];
  unreadCount: number;
  markAsRead: (id: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  loading: boolean;
  recarregar: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType>({
  notifications: [],
  unreadCount: 0,
  markAsRead: async () => {},
  markAllAsRead: async () => {},
  loading: false,
  recarregar: async () => {},
});

function paraNotificacao(dto: NotificacaoDTO): Notificacao {
  return {
    idNotificacao: dto.id,
    idProprietario: dto.proprietarioId,
    idPedido: dto.pedidoRelacionadoId ?? '',
    titulo: dto.titulo,
    mensagem: dto.mensagem,
    lida: dto.lida,
    dataGeracao: new Date(dto.dataGeracao),
    dataLeitura: dto.dataLeitura ? new Date(dto.dataLeitura) : undefined,
  };
}

export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notificacao[]>([]);
  const [loading, setLoading] = useState(false);

  const recarregar = useCallback(async () => {
    if (!user) {
      setNotifications([]);
      return;
    }
    setLoading(true);
    try {
      // O endpoint devolve todas; filtramos pelas do dono logado.
      const todas = await listarNotificacoes();
      setNotifications(
        todas
          .filter((n) => n.proprietarioId === user.uid)
          .map(paraNotificacao)
          .sort((a, b) => b.dataGeracao.getTime() - a.dataGeracao.getTime())
      );
    } catch {
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    void (async () => {
      await recarregar();
    })();
  }, [recarregar]);

  const markAsRead = useCallback(
    async (id: string) => {
      // Atualiza na hora e confirma com o servidor: sem isso o badge não reage.
      setNotifications((atuais) =>
        atuais.map((n) =>
          n.idNotificacao === id && !n.lida
            ? { ...n, lida: true, dataLeitura: new Date() }
            : n
        )
      );
      try {
        await marcarNotificacaoLida(id);
      } catch {
        await recarregar();
      }
    },
    [recarregar]
  );

  const markAllAsRead = useCallback(async () => {
    const naoLidas = notifications.filter((n) => !n.lida);
    if (naoLidas.length === 0) return;

    setNotifications((atuais) =>
      atuais.map((n) => (n.lida ? n : { ...n, lida: true, dataLeitura: new Date() }))
    );
    await Promise.allSettled(naoLidas.map((n) => marcarNotificacaoLida(n.idNotificacao)));
    await recarregar();
  }, [notifications, recarregar]);

  const unreadCount = notifications.filter((n) => !n.lida).length;

  return (
    <NotificationContext.Provider
      value={{ notifications, unreadCount, markAsRead, markAllAsRead, loading, recarregar }}
    >
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  return useContext(NotificationContext);
}
