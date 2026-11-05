'use client';

import { useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/components/feedback/Toast';
import { listarPendentes, sincronizarFila } from '@/lib/filaMovimentacoes';

/** Enquanto houver pendências, tenta de novo a cada intervalo (o evento "online" falha com Wi-Fi sem internet). */
const INTERVALO_MS = 30_000;

/**
 * Envia as movimentações de estoque guardadas sem internet (#234): ao abrir o app,
 * quando a conexão volta e, enquanto houver pendências, a cada 30 s.
 */
export function SincronizarMovimentacoes() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const donoId = user?.uid;

  useEffect(() => {
    if (!donoId) return;

    const sincronizar = async () => {
      if (!listarPendentes(donoId).some((p) => !p.erro)) return;
      const { enviadas, recusadas } = await sincronizarFila(donoId);
      if (enviadas) {
        showToast(
          enviadas === 1
            ? '1 movimentação guardada sem internet foi enviada.'
            : `${enviadas} movimentações guardadas sem internet foram enviadas.`,
          'success'
        );
      }
      if (recusadas) {
        showToast(
          recusadas === 1
            ? '1 movimentação guardada foi recusada pelo servidor. Veja no histórico do estoque.'
            : `${recusadas} movimentações guardadas foram recusadas pelo servidor. Veja no histórico do estoque.`,
          'error'
        );
      }
    };

    void sincronizar();
    window.addEventListener('online', sincronizar);
    const intervalo = setInterval(sincronizar, INTERVALO_MS);
    return () => {
      window.removeEventListener('online', sincronizar);
      clearInterval(intervalo);
    };
  }, [donoId, showToast]);

  return null;
}
