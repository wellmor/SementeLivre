'use client';

import { WifiOff } from 'lucide-react';
import { useOffline } from '@/hooks/useOffline';
import styles from './AvisoOffline.module.css';

/** Faixa no topo de todas as telas enquanto o aparelho está sem internet (#234). */
export function AvisoOffline() {
  const { isOffline } = useOffline();

  return (
    // A região fica sempre no DOM para o leitor de tela anunciar quando o texto aparece
    <div role="status" aria-live="polite" className={styles.regiao}>
      {isOffline && (
        <p className={styles.aviso}>
          <WifiOff size={16} aria-hidden="true" />
          Sem internet. O que já foi aberto continua disponível.
        </p>
      )}
    </div>
  );
}
