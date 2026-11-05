import type { Metadata } from 'next';
import Link from 'next/link';
import { ArrowLeft, Sprout } from 'lucide-react';
import styles from './not-found.module.css';

export const metadata: Metadata = {
  title: 'Página não encontrada | Semente Livre',
};

/**
 * 404 do app: qualquer endereço que não existe cai aqui (Next devolve status 404).
 * "Voltar ao início" vai para "/", que manda ao painel com sessão ou ao login sem ela.
 */
export default function NaoEncontrada() {
  return (
    <main className={styles.container}>
      <div className={styles.hero}>
        <div className={`${styles.blob} ${styles.blob1}`} aria-hidden="true" />
        <div className={`${styles.blob} ${styles.blob2}`} aria-hidden="true" />
        <div className={styles.icone} aria-hidden="true">
          <Sprout size={40} strokeWidth={2} />
        </div>
        <p className={styles.codigo} aria-hidden="true">
          404
        </p>
      </div>

      <section className={styles.card} aria-labelledby="titulo-404">
        <h1 id="titulo-404" className={styles.titulo}>
          Página não encontrada
        </h1>
        <p className={styles.texto}>
          Esta semente ainda não brotou por aqui. O endereço pode estar errado ou a página pode ter
          mudado de lugar.
        </p>
        <Link href="/" className={styles.botao}>
          <ArrowLeft size={18} aria-hidden="true" />
          Voltar ao início
        </Link>
      </section>
    </main>
  );
}
