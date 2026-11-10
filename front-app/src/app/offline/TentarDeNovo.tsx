'use client';

import { RotateCw } from 'lucide-react';

/**
 * Volta para a tela que a pessoa tentou abrir (?de=, posto pelo Service Worker).
 * Com a internet de volta, ela abre normalmente. Só aceita caminho do próprio app.
 */
export function TentarDeNovo({ className }: { className: string }) {
  const tentar = () => {
    const de = new URLSearchParams(window.location.search).get('de');
    const destino = de && de.startsWith('/') && !de.startsWith('//') ? de : '/dashboard';
    window.location.assign(destino);
  };

  return (
    <button type="button" className={className} onClick={tentar}>
      <RotateCw size={18} aria-hidden="true" />
      Tentar novamente
    </button>
  );
}
