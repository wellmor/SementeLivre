'use client';

/**
 * admin/comunidades/page.tsx — Aprovação das comunidades solicitadas pelos proprietários.
 *
 *   - Lista as comunidades com status PENDENTE_APROVACAO de GET /api/comunidades
 *     (a rota devolve todas; a tela filtra), da solicitação mais antiga para a mais nova.
 *   - Aprovar (PATCH /aprovar) deixa a comunidade ATIVA, e ela passa a aparecer para os
 *     proprietários; rejeitar (PATCH /rejeitar) a deixa REJEITADA. As duas pedem confirmação.
 *   - 409: outro admin já decidiu; a tela avisa e recarrega a lista.
 *   - Só admin chega aqui (admin/layout.tsx); o backend confere de novo (@PreAuthorize).
 */

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { AlertCircle, CalendarClock, Check, MapPin, ShieldCheck, Users, X } from 'lucide-react';
import { aprovarComunidade, ComunidadeDTO, listarComunidades, rejeitarComunidade } from '@/lib/propriedadeApi';
import { errorMessage, isApiError } from '@/lib/api';
import { StatusComunidade } from '@/types/property';
import { useToast } from '@/components/feedback/Toast';
import { EmptyState } from '@/components/feedback/EmptyState';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { Button } from '@/components/ui/button';
import styles from './comunidades.module.css';

type Decisao = 'aprovar' | 'rejeitar';

interface AcaoPendente {
  decisao: Decisao;
  comunidade: ComunidadeDTO;
}

function pendentes(lista: ComunidadeDTO[]): ComunidadeDTO[] {
  return lista
    .filter((c) => c.status === StatusComunidade.PENDENTE_APROVACAO)
    .sort((a, b) => (a.dataSolicitacao ?? '').localeCompare(b.dataSolicitacao ?? ''));
}

function formatarData(iso: string | null): string | null {
  if (!iso) return null;
  const data = new Date(iso);
  return Number.isNaN(data.getTime()) ? null : data.toLocaleDateString('pt-BR');
}

function localidade(c: ComunidadeDTO): string | null {
  if (c.logradouroMunicipio && c.logradouroUf) return `${c.logradouroMunicipio}/${c.logradouroUf}`;
  return c.logradouroMunicipio || c.logradouroUf || null;
}

function resumo(total: number): string {
  if (total === 0) return 'Nenhuma comunidade aguardando aprovação.';
  if (total === 1) return '1 comunidade aguardando aprovação.';
  return `${total} comunidades aguardando aprovação.`;
}

export default function AprovarComunidadesPage() {
  const { showToast } = useToast();
  const [comunidades, setComunidades] = useState<ComunidadeDTO[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [acao, setAcao] = useState<AcaoPendente | null>(null);
  const [enviando, setEnviando] = useState(false);
  const tituloRef = useRef<HTMLHeadingElement>(null);

  // Busca a lista; `ativo` evita atualizar a tela depois que ela foi fechada.
  const buscar = useCallback(
    (ativo: () => boolean = () => true) =>
      listarComunidades()
        .then((lista) => {
          if (!ativo()) return;
          setComunidades(pendentes(lista));
          setErro(null);
        })
        .catch((err) => {
          if (ativo()) setErro(errorMessage(err, 'Não foi possível carregar as comunidades.'));
        })
        .finally(() => {
          if (ativo()) setCarregando(false);
        }),
    []
  );

  const carregar = useCallback(() => {
    setCarregando(true);
    setErro(null);
    return buscar();
  }, [buscar]);

  useEffect(() => {
    let ativo = true;
    void buscar(() => ativo);
    return () => {
      ativo = false;
    };
  }, [buscar]);

  // O botão que abriu a confirmação some com o card: o foco vai para o título da lista.
  const focarTitulo = () => setTimeout(() => tituloRef.current?.focus(), 60);

  const confirmar = async () => {
    if (!acao) return;
    const { decisao, comunidade } = acao;
    setEnviando(true);
    try {
      if (decisao === 'aprovar') await aprovarComunidade(comunidade.id);
      else await rejeitarComunidade(comunidade.id);
      setComunidades((prev) => prev.filter((c) => c.id !== comunidade.id));
      showToast(
        decisao === 'aprovar'
          ? `Comunidade "${comunidade.nome}" aprovada.`
          : `Comunidade "${comunidade.nome}" rejeitada.`,
        'success'
      );
      setAcao(null);
      focarTitulo();
    } catch (err) {
      if (isApiError(err) && err.status === 409) {
        // Outro admin decidiu antes: a lista recarrega com a situação atual
        showToast('Esta comunidade já foi analisada. A lista foi atualizada.', 'warning');
        setAcao(null);
        void carregar();
        focarTitulo();
      } else if (isApiError(err) && err.status === 403) {
        showToast('Sua conta não tem permissão para analisar comunidades.', 'error');
        setAcao(null);
      } else if (!isApiError(err) || err.status !== 0) {
        // status 0 (sem conexão) já vira toast em lib/api.ts
        showToast(errorMessage(err, 'Não foi possível concluir. Tente novamente.'), 'error');
      }
    } finally {
      setEnviando(false);
    }
  };

  const nomeAcao = acao?.comunidade.nome ?? '';

  return (
    <div className={styles.page}>
      <div className={styles.intro}>
        <div className={styles.introIcon} aria-hidden="true">
          <ShieldCheck size={20} strokeWidth={2} />
        </div>
        <div>
          <h2 ref={tituloRef} tabIndex={-1} className={styles.introTitle}>Comunidades pendentes</h2>
          <p className={styles.introText}>
            Comunidades aprovadas passam a aparecer para todos os proprietários no cadastro de propriedades.
          </p>
        </div>
      </div>

      {/* Resumo anunciado a leitores de tela quando a lista muda */}
      <p className={styles.resumo} aria-live="polite" aria-atomic="true">
        {carregando ? 'Carregando comunidades...' : erro ? '' : resumo(comunidades.length)}
      </p>

      {carregando ? (
        <div className={styles.list} aria-hidden="true">
          {[1, 2, 3].map((i) => <div key={i} className={`skeleton ${styles.skeletonCard}`} />)}
        </div>
      ) : erro ? (
        <div className={styles.erro} role="alert">
          <AlertCircle size={18} strokeWidth={2.5} aria-hidden="true" />
          <p className={styles.erroTexto}>{erro}</p>
          <Button variant="secondary" onClick={() => void carregar()}>
            Tentar novamente
          </Button>
        </div>
      ) : comunidades.length === 0 ? (
        <EmptyState
          icon={<Users size={38} strokeWidth={1.5} />}
          title="Nenhuma comunidade pendente"
          description="Quando um proprietário solicitar uma comunidade nova, ela aparece aqui para análise."
        />
      ) : (
        <ul className={styles.list} aria-label="Comunidades aguardando aprovação">
          {comunidades.map((c) => {
            const lugar = localidade(c);
            const data = formatarData(c.dataSolicitacao);
            return (
              <li key={c.id} className={styles.card}>
                <div className={styles.cardInfo}>
                  <p className={styles.cardName}>{c.nome}</p>
                  {lugar && (
                    <p className={styles.cardMeta}>
                      <MapPin size={13} strokeWidth={2} aria-hidden="true" />
                      {lugar}
                    </p>
                  )}
                  {data && (
                    <p className={styles.cardMeta}>
                      <CalendarClock size={13} strokeWidth={2} aria-hidden="true" />
                      Solicitada em {data}
                    </p>
                  )}
                </div>
                <div className={styles.cardActions}>
                  <Button
                    variant="danger"
                    fullWidth
                    onClick={() => setAcao({ decisao: 'rejeitar', comunidade: c })}
                    aria-label={`Rejeitar a comunidade ${c.nome}`}
                  >
                    <X size={15} strokeWidth={2.5} aria-hidden="true" />
                    Rejeitar
                  </Button>
                  <Button
                    variant="primary"
                    fullWidth
                    onClick={() => setAcao({ decisao: 'aprovar', comunidade: c })}
                    aria-label={`Aprovar a comunidade ${c.nome}`}
                  >
                    <Check size={15} strokeWidth={2.5} aria-hidden="true" />
                    Aprovar
                  </Button>
                </div>
              </li>
            );
          })}
        </ul>
      )}

      <ConfirmDialog
        isOpen={acao !== null}
        title={acao?.decisao === 'rejeitar' ? 'Rejeitar comunidade' : 'Aprovar comunidade'}
        description={
          acao?.decisao === 'rejeitar'
            ? `Rejeitar "${nomeAcao}"? Ela não ficará disponível para os proprietários, e a decisão não pode ser desfeita por aqui.`
            : `Aprovar "${nomeAcao}"? Ela ficará disponível para todos os proprietários no cadastro de propriedades.`
        }
        confirmLabel={acao?.decisao === 'rejeitar' ? 'Rejeitar' : 'Aprovar'}
        confirmVariant={acao?.decisao === 'rejeitar' ? 'danger' : 'primary'}
        onConfirm={() => void confirmar()}
        onCancel={() => { if (!enviando) setAcao(null); }}
        loading={enviando}
      />
    </div>
  );
}
