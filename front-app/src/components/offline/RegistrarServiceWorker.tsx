'use client';

import { useEffect } from 'react';

/**
 * Registra o Service Worker (public/sw.js) que deixa o app abrir sem internet (#234).
 * Só no build de produção: no `next dev` os arquivos mudam a cada salvamento e o
 * cache atrapalharia o desenvolvimento.
 */
export function RegistrarServiceWorker() {
  useEffect(() => {
    if (process.env.NODE_ENV !== 'production' || !('serviceWorker' in navigator)) return;
    navigator.serviceWorker
      .register('/sw.js', { scope: '/', updateViaCache: 'none' })
      .catch(() => undefined); // sem Service Worker o app segue funcionando online
  }, []);

  return null;
}
