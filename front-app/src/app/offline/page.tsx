import type { Metadata } from 'next';
import Link from 'next/link';
import { WifiOff } from 'lucide-react';
import { TentarDeNovo } from './TentarDeNovo';
import styles from '../not-found.module.css';

export const metadata: Metadata = {
  title: 'Sem internet | Semente Livre',
};

/**
 * Tela que o Service Worker (public/sw.js) mostra quando não há internet e a tela
 * pedida ainda não foi aberta neste aparelho (#234). Mesmo visual da 404.
 */
export default function SemInternet() {
  return (
    <main className={styles.container}>
      <div className={styles.hero}>
        <div className={`${styles.blob} ${styles.blob1}`} aria-hidden="true" />
        <div className={`${styles.blob} ${styles.blob2}`} aria-hidden="true" />
        <div className={styles.icone} aria-hidden="true">
          <WifiOff size={40} strokeWidth={2} />
        </div>
      </div>

      <section className={styles.card} aria-labelledby="titulo-offline">
        <h1 id="titulo-offline" className={styles.titulo}>
          Sem internet
        </h1>
        <p className={styles.texto}>
          Esta tela ainda não foi aberta neste aparelho, então não ficou salva. Conecte-se à internet
          e tente de novo, ou volte para uma tela que você já abriu.
        </p>
        <TentarDeNovo className={styles.botao} />
        <Link href="/dashboard" className={styles.link}>
          Ir para o painel
        </Link>
      </section>
    </main>
  );
}
