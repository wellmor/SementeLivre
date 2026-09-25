'use client';

import { use, useEffect, useState } from 'react';
import Link from 'next/link';
import {
  ArrowLeft,
  Sprout,
  MapPin,
  Package,
  Tag,
  FlaskConical,
  BookOpen,
} from 'lucide-react';

import { CatalogoProdutoDetalhe } from '@/types/catalog';
import {
  EspecieGeralLabels,
  FormatoProdutoLabels,
  TipoProdutoLabels,
} from '@/types/seed';
import {
  DisponibilidadeLabels,
  PesagemLabels,
} from '@/types/stock';

import { Badge } from '@/components/ui/badge';
import { EmptyState } from '@/components/feedback/EmptyState';

import styles from './produto.module.css';

interface PageProps {
  params: Promise<{
    id: string;
  }>;
}

export default function CatalogoProdutoPage({ params }: PageProps) {
  const { id } = use(params);

  const [produto, setProduto] =
    useState<CatalogoProdutoDetalhe | null>(null);

  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();

    async function carregarProduto() {
      setLoading(true);
      setErro(null);

      try {
        const response = await fetch(`/api/catalogo/${id}`, {
          signal: controller.signal,
        });

        if (!response.ok) {
          if (response.status === 404) {
            throw new Error('Produto não encontrado.');
          }

          throw new Error('Erro ao carregar produto.');
        }

        const data: CatalogoProdutoDetalhe = await response.json();

        setProduto(data);
      } catch (error) {
        if (error instanceof Error && error.name === 'AbortError') {
          return;
        }

        setProduto(null);

        if (error instanceof Error) {
          setErro(error.message);
        } else {
          setErro('Não foi possível carregar o produto.');
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    carregarProduto();

    return () => controller.abort();
  }, [id]);

  if (loading) {
    return (
      <div className={styles.page}>
        <div className={`skeleton ${styles.heroSkeleton}`} />
        <div className={`skeleton ${styles.contentSkeleton}`} />
        <div className={`skeleton ${styles.contentSkeleton}`} />
      </div>
    );
  }

  if (erro || !produto) {
    return (
      <div className={styles.page}>
        <Link href="/catalogo" className={styles.back}>
          <ArrowLeft size={18} />
          Voltar ao catálogo
        </Link>

        <EmptyState
          icon={<Sprout size={40} strokeWidth={1.5} />}
          title="Produto não encontrado"
          description={
            erro ?? 'Não foi possível encontrar este produto no catálogo.'
          }
        />
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <Link href="/catalogo" className={styles.back}>
        <ArrowLeft size={18} />
        Voltar ao catálogo
      </Link>

      <section className={styles.product}>
        <div className={styles.imageWrap}>
          {produto.urlFoto ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={produto.urlFoto}
              alt={`Foto de ${produto.nomePopular}`}
              className={styles.image}
            />
          ) : (
            <div className={styles.imagePlaceholder}>
              <Sprout size={52} strokeWidth={1.4} />
            </div>
          )}
        </div>

        <div className={styles.productInfo}>
          <h1 className={styles.title}>{produto.nomePopular}</h1>

          {produto.nomeCientifico && (
            <p className={styles.scientificName}>
              <FlaskConical size={16} />
              <em>{produto.nomeCientifico}</em>
            </p>
          )}

          <div className={styles.tags}>
            <span className={styles.tag}>
              {TipoProdutoLabels[produto.tipo]}
            </span>

            <span className={styles.tag}>
              {EspecieGeralLabels[produto.especie]}
            </span>

            <span className={styles.tag}>
              {FormatoProdutoLabels[produto.formato]}
            </span>
          </div>

          {produto.familiaBotanica && (
            <p className={styles.info}>
              <BookOpen size={16} />
              Família botânica: {produto.familiaBotanica}
            </p>
          )}

          {produto.comunidade && (
            <p className={styles.info}>
              <MapPin size={16} />
              {produto.comunidade}
              {produto.municipio ? ` — ${produto.municipio}` : ''}
              {produto.uf ? `/${produto.uf}` : ''}
            </p>
          )}
        </div>
      </section>

      {produto.historico && (
        <section className={styles.section}>
          <h2>Histórico</h2>
          <p>{produto.historico}</p>
        </section>
      )}

      <section className={styles.section}>
        <h2>Ofertas disponíveis</h2>

        {produto.ofertas.length === 0 ? (
          <p className={styles.muted}>
            Nenhuma oferta disponível no momento.
          </p>
        ) : (
          <div className={styles.offers}>
            {produto.ofertas.map((oferta) => (
              <article
                key={oferta.estoqueId}
                className={styles.offer}
              >
                <div className={styles.offerHeader}>
                  <Badge
                    variant="availability"
                    value={oferta.disponibilidade}
                  />

                  {oferta.preco != null && (
                    <strong className={styles.price}>
                      R$ {oferta.preco.toFixed(2)}
                    </strong>
                  )}
                </div>

                <div className={styles.offerDetails}>
                  <p>
                    <Package size={16} />
                    {oferta.quantidade}{' '}
                    {PesagemLabels[oferta.tipoPesagem]}
                  </p>

                  <p>
                    <Tag size={16} />
                    {DisponibilidadeLabels[oferta.disponibilidade]}
                  </p>
                </div>

                {oferta.descricao && (
                  <p className={styles.description}>
                    {oferta.descricao}
                  </p>
                )}
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}