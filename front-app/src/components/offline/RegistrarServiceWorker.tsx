'use client';

import { useEffect } from 'react';
import { usePathname } from 'next/navigation';

/** Só no build de produção: no `next dev` os arquivos mudam a cada salvamento e o cache atrapalharia. */
const ATIVO = process.env.NODE_ENV === 'production';

/**
 * Registra o Service Worker (public/sw.js) que deixa o app abrir sem internet (#234).
 *
 * A cada troca de tela também avisa o SW: tela aberta por um link do app não passa
 * por ele como navegação (o Next só busca os dados RSC), então sem o aviso ela não
 * ficaria guardada para abrir offline.
 */
export function RegistrarServiceWorker() {
  const caminho = usePathname();

  useEffect(() => {
    if (!ATIVO || !('serviceWorker' in navigator)) return;
    navigator.serviceWorker
      .register('/sw.js', { scope: '/', updateViaCache: 'none' })
      .catch(() => undefined); // sem Service Worker o app segue funcionando online
  }, []);

  useEffect(() => {
    if (!ATIVO || !('serviceWorker' in navigator) || !navigator.onLine) return;
    // ready (e não controller): na primeira visita o SW ainda não controla a página
    navigator.serviceWorker.ready
      .then((registro) => registro.active?.postMessage({ tipo: 'tela-aberta', caminho }))
      .catch(() => undefined);
  }, [caminho]);

  return null;
}
