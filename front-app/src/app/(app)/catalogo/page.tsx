'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import {
  Search,
  Sprout,
  ChevronRight,
  PackageSearch,
} from 'lucide-react';

import {
  CatalogoDisponibilidade,
  CatalogoPage as CatalogoPageResponse,
} from '@/types/catalog';

import {
  EspecieGeral,
  EspecieGeralLabels,
  TipoProduto,
  TipoProdutoLabels,
} from '@/types/seed';

import {
  DisponibilidadeLabels,
  PesagemLabels,
} from '@/types/stock';

import { Badge } from '@/components/ui/badge';
import { EmptyState } from '@/components/feedback/EmptyState';

import styles from './catalogo.module.css';

const PAGE_SIZE = 10;

const disponibilidades: CatalogoDisponibilidade[] = [
  'PARA_VENDA',
  'PARA_TROCA',
  'PARA_DOACAO',
];

export default function CatalogoPage() {
  const [catalogo, setCatalogo] =
    useState<CatalogoPageResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState<string | null>(null);

  const [page, setPage] = useState(0);

  const [busca, setBusca] = useState('');
  const [tipo, setTipo] = useState<TipoProduto | ''>('');
  const [especie, setEspecie] = useState<EspecieGeral | ''>('');
  const [disponibilidade, setDisponibilidade] =
    useState<CatalogoDisponibilidade | ''>('');
  const [comunidade, setComunidade] = useState('');
  const [municipio, setMunicipio] = useState('');

  useEffect(() => {
    const controller = new AbortController();

    async function carregarCatalogo() {
      setLoading(true);
      setErro(null);

      try {
        const params = new URLSearchParams({
          page: String(page),
          size: String(PAGE_SIZE),
        });

        if (busca.trim()) {
          params.set('nomePopular', busca.trim());
        }

        if (tipo) {
          params.set('tipo', tipo);
        }

        if (especie) {
          params.set('especie', especie);
        }

        if (disponibilidade) {
          params.set('disponibilidade', disponibilidade);
        }

        if (comunidade.trim()) {
          params.set('comunidade', comunidade.trim());
        }

        if (municipio.trim()) {
          params.set('municipio', municipio.trim());
        }

        const response = await fetch(
          `/api/catalogo?${params.toString()}`,
          {
            signal: controller.signal,
          }
        );

        if (!response.ok) {
          throw new Error('Erro ao carregar catálogo.');
        }

        const data: CatalogoPageResponse = await response.json();

        setCatalogo(data);
      } catch (error) {
        if (error instanceof Error && error.name === 'AbortError') {
          return;
        }

        setCatalogo(null);
        setErro('Não foi possível carregar o catálogo.');
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    carregarCatalogo();

    return () => controller.abort();
  }, [
    page,
    busca,
    tipo,
    especie,
    disponibilidade,
    comunidade,
    municipio,
  ]);

  const produtos = catalogo?.content ?? [];

  function limparFiltros() {
    setBusca('');
    setTipo('');
    setEspecie('');
    setDisponibilidade('');
    setComunidade('');
    setMunicipio('');
    setPage(0);
  }

  return (
    <div className={styles.page}>
      {/* Busca */}
      <div className={styles.topBar}>
        <div className={styles.searchWrap}>
          <Search
            size={15}
            strokeWidth={2}
            className={styles.searchIcon}
          />

          <input
            type="search"
            className={styles.search}
            placeholder="Buscar por nome popular..."
            aria-label="Buscar por nome popular"
            value={busca}
            onChange={(event) => {
              setBusca(event.target.value);
              setPage(0);
            }}
          />
        </div>
      </div>

      {/* Filtros */}
      <div className={styles.filters}>
        <select
          value={tipo}
          onChange={(event) => {
            setTipo(event.target.value as TipoProduto | '');
            setPage(0);
          }}
          aria-label="Filtrar por tipo"
        >
          <option value="">Todos os tipos</option>

          {Object.values(TipoProduto).map((value) => (
            <option key={value} value={value}>
              {TipoProdutoLabels[value]}
            </option>
          ))}
        </select>

        <select
          value={especie}
          onChange={(event) => {
            setEspecie(event.target.value as EspecieGeral | '');
            setPage(0);
          }}
          aria-label="Filtrar por espécie"
        >
          <option value="">Todas as espécies</option>

          {Object.values(EspecieGeral).map((value) => (
            <option key={value} value={value}>
              {EspecieGeralLabels[value]}
            </option>
          ))}
        </select>

        <select
          value={disponibilidade}
          onChange={(event) => {
            setDisponibilidade(
              event.target.value as CatalogoDisponibilidade | ''
            );
            setPage(0);
          }}
          aria-label="Filtrar por disponibilidade"
        >
          <option value="">Todas as disponibilidades</option>

          {disponibilidades.map((value) => (
            <option key={value} value={value}>
              {DisponibilidadeLabels[value]}
            </option>
          ))}
        </select>

        <input
          type="text"
          placeholder="Comunidade"
          aria-label="Filtrar por comunidade"
          value={comunidade}
          onChange={(event) => {
            setComunidade(event.target.value);
            setPage(0);
          }}
        />

        <input
          type="text"
          placeholder="Município"
          aria-label="Filtrar por município"
          value={municipio}
          onChange={(event) => {
            setMunicipio(event.target.value);
            setPage(0);
          }}
        />

        <button
          type="button"
          className={styles.chip}
          onClick={limparFiltros}
        >
          Limpar filtros
        </button>
      </div>

      {/* Carregamento */}
      {loading ? (
        <div className={styles.list}>
          {[1, 2, 3].map((item) => (
            <div
              key={item}
              className={`skeleton ${styles.skeletonCard}`}
            />
          ))}
        </div>
      ) : erro ? (
        /* Erro */
        <EmptyState
          icon={<PackageSearch size={40} strokeWidth={1.5} />}
          title="Erro ao carregar catálogo"
          description={erro}
        />
      ) : produtos.length === 0 ? (
        /* Catálogo vazio */
        <EmptyState
          icon={<PackageSearch size={40} strokeWidth={1.5} />}
          title="Nenhum produto encontrado"
          description="Nenhum produto disponível corresponde aos filtros informados."
        />
      ) : (
        <>
          {/* Produtos */}
          <ul
            className={styles.list}
            aria-label={`${catalogo?.totalElements ?? 0} produtos encontrados`}
          >
            {produtos.map((produto) => (
              <li key={produto.estoqueId}>
                <Link
                  href={`/catalogo/${produto.id}`}
                  className={styles.card}
                >
                  <div className={styles.cardImg}>
                    {produto.urlFoto ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={produto.urlFoto}
                        alt={`Foto de ${produto.nomePopular}`}
                        className={styles.img}
                      />
                    ) : (
                      <div
                        className={styles.imgPlaceholder}
                        aria-hidden="true"
                      >
                        <Sprout size={22} strokeWidth={1.5} />
                      </div>
                    )}
                  </div>

                  <div className={styles.cardContent}>
                    <p className={styles.cardName}>
                      {produto.nomePopular}
                    </p>

                    {produto.nomeCientifico && (
                      <p className={styles.cardQty}>
                        {produto.nomeCientifico}
                      </p>
                    )}

                    <p className={styles.cardQty}>
                      {produto.quantidade}{' '}
                      {PesagemLabels[produto.tipoPesagem]}
                    </p>

                    <p className={styles.cardQty}>
                      {TipoProdutoLabels[produto.tipo]}
                    </p>

                    {produto.comunidade && (
                      <p className={styles.cardQty}>
                        {produto.comunidade}
                      </p>
                    )}

                    {produto.municipio && (
                      <p className={styles.cardQty}>
                        {produto.municipio}
                        {produto.uf ? ` - ${produto.uf}` : ''}
                      </p>
                    )}

                    <Badge
                      variant="availability"
                      value={produto.disponibilidade}
                    />
                  </div>

                  <ChevronRight
                    size={16}
                    strokeWidth={2}
                    className={styles.chevron}
                  />
                </Link>
              </li>
            ))}
          </ul>

          {/* Paginação */}
          {catalogo && catalogo.totalPages > 1 && (
            <div className={styles.filters}>
              <button
                type="button"
                className={styles.chip}
                disabled={catalogo.first}
                onClick={() =>
                  setPage((atual) => Math.max(0, atual - 1))
                }
              >
                Anterior
              </button>

              <span>
                Página {catalogo.number + 1} de {catalogo.totalPages}
              </span>

              <button
                type="button"
                className={styles.chip}
                disabled={catalogo.last}
                onClick={() => setPage((atual) => atual + 1)}
              >
                Próxima
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}