'use client';

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ArrowLeft, ArrowDown, ArrowUp, CloudOff, History, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { EmptyState } from '@/components/feedback/EmptyState';
import { useToast } from '@/components/feedback/Toast';
import { useAuth } from '@/context/AuthContext';
import { isApiError } from '@/lib/api';
import { useSeeds } from '@/hooks/useSeeds';
import { buscarMovimentacoesApi, Movimentacao, NovaMovimentacao, registrarMovimentacaoApi } from '@/lib/estoqueApi';
import {
  descartarPendente,
  enfileirarMovimentacao,
  listarPendentes,
  MovimentacaoPendente,
  ouvirFila,
} from '@/lib/filaMovimentacoes';
import { OrigemMovimentacaoLabels, TipoMovimentacao, TipoMovimentacaoLabels } from '@/types/stock';
import styles from './movimentacoes.module.css';

const TIPOS_LANCAVEL: TipoMovimentacao[] = [
  TipoMovimentacao.ENTRADA,
  TipoMovimentacao.SAIDA_VENDA,
  TipoMovimentacao.SAIDA_TROCA,
  TipoMovimentacao.SAIDA_DOACAO,
];

export default function MovimentacoesEstoquePage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { getSeed, recarregar } = useSeeds();
  const { showToast } = useToast();
  const { user } = useAuth();
  const donoId = user?.uid;

  const [seedName, setSeedName] = useState<string>('Produto');
  const [saldoAtual, setSaldoAtual] = useState<number | null>(null);
  const [movimentacoes, setMovimentacoes] = useState<Movimentacao[]>([]);
  const [loading, setLoading] = useState(true);

  // Formulário de lançamento rápido
  const [showForm, setShowForm] = useState(false);
  const [tipo, setTipo] = useState<TipoMovimentacao>(TipoMovimentacao.ENTRADA);
  const [quantidade, setQuantidade] = useState('');
  const [descricao, setDescricao] = useState('');
  const [salvando, setSalvando] = useState(false);

  // Movimentações registradas sem internet, ainda não enviadas (#234)
  const [pendentes, setPendentes] = useState<MovimentacaoPendente[]>([]);
  const quantasPendentes = useRef(0);

  const carregar = useCallback(async () => {
    try {
      const pagina = await buscarMovimentacoesApi(id, 0, 50);
      setMovimentacoes(pagina.content);
      // O saldo vigente é o da linha mais recente: o endpoint devolve do mais
      // novo para o mais antigo, então o saldoPosterior da primeira linha é
      // o saldo de agora.
      setSaldoAtual(pagina.content[0]?.saldoPosterior ?? null);
    } catch {
      showToast('Não foi possível carregar o histórico.', 'error');
    } finally {
      setLoading(false);
    }
  }, [id, showToast]);

  useEffect(() => {
    getSeed(id).then((s) => {
      if (s) {
        setSeedName(s.nomePopular);
        setSaldoAtual(s.quantidade);
      }
    });
    carregar();
  }, [id, getSeed, carregar]);

  useEffect(() => {
    if (!donoId) return;
    const atualizar = () => {
      const lista = listarPendentes(donoId, id);
      // A fila diminuiu: algo foi enviado, então o histórico e o saldo mudaram
      if (lista.length < quantasPendentes.current) void Promise.all([carregar(), recarregar()]);
      quantasPendentes.current = lista.length;
      setPendentes(lista);
    };
    atualizar();
    return ouvirFila(atualizar);
  }, [donoId, id, carregar, recarregar]);

  const limparFormulario = () => {
    setShowForm(false);
    setQuantidade('');
    setDescricao('');
  };

  /** Sem internet: guarda no aparelho, e a sincronização envia quando a conexão voltar. */
  const guardarSemInternet = (dados: NovaMovimentacao) => {
    if (!donoId) return;
    try {
      enfileirarMovimentacao(donoId, id, dados);
      showToast('Sem internet: a movimentação foi guardada e será enviada quando a conexão voltar.', 'info');
      limparFormulario();
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'Não foi possível guardar a movimentação.', 'error');
    }
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const qtd = Number(quantidade);
    if (!qtd || qtd <= 0) {
      showToast('Informe uma quantidade maior que zero.', 'error');
      return;
    }
    const dados: NovaMovimentacao = {
      tipo,
      quantidade: qtd,
      descricao: descricao.trim() || undefined,
    };
    if (!navigator.onLine) {
      guardarSemInternet(dados);
      return;
    }
    setSalvando(true);
    try {
      await registrarMovimentacaoApi(id, dados);
      showToast('Movimentação registrada.', 'success');
      limparFormulario();
      await Promise.all([carregar(), recarregar()]);
    } catch (err) {
      if (isApiError(err) && err.status === 0) {
        guardarSemInternet(dados);
      } else {
        showToast(
          err instanceof Error || isApiError(err) ? err.message : 'Não foi possível registrar a movimentação.',
          'error'
        );
      }
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <Button variant="ghost" onClick={() => router.back()} className={styles.backBtn} aria-label="Voltar">
          <ArrowLeft size={20} aria-hidden="true" />
        </Button>
        <div className={styles.headerInfo}>
          <h1 className={styles.title}>Histórico de Estoque</h1>
          <p className={styles.subtitle}>
            {seedName}
            {saldoAtual !== null && ` · saldo ${saldoAtual} `}
          </p>
        </div>
      </div>

      <Button variant="secondary" fullWidth onClick={() => setShowForm((v) => !v)}>
        <Plus size={16} strokeWidth={2.5} />
        {showForm ? 'Cancelar' : 'Nova movimentação'}
      </Button>

      {showForm && (
        <form onSubmit={submit} className={styles.form}>
          <div className={styles.formRow}>
            <label className={styles.field}>
              <span className={styles.fieldLabel}>Tipo</span>
              <select
                value={tipo}
                onChange={(e) => setTipo(e.target.value as TipoMovimentacao)}
                className={styles.select}
              >
                {TIPOS_LANCAVEL.map((t) => (
                  <option key={t} value={t}>
                    {TipoMovimentacaoLabels[t]}
                  </option>
                ))}
              </select>
            </label>
            <label className={styles.field}>
              <span className={styles.fieldLabel}>Quantidade</span>
              <input
                type="number"
                inputMode="decimal"
                min="0.01"
                step="0.01"
                value={quantidade}
                onChange={(e) => setQuantidade(e.target.value)}
                placeholder="0"
                required
                className={styles.input}
              />
            </label>
          </div>
          <label className={styles.field}>
            <span className={styles.fieldLabel}>Motivo (opcional)</span>
            <input
              type="text"
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              placeholder="Ex.: colheita da safra 2026"
              className={styles.input}
            />
          </label>
          <Button type="submit" variant="primary" fullWidth loading={salvando}>
            Registrar
          </Button>
        </form>
      )}

      {pendentes.length > 0 && (
        <section aria-labelledby="titulo-pendentes" className={styles.pendentes}>
          <h2 id="titulo-pendentes" className={styles.pendentesTitulo}>
            <CloudOff size={16} aria-hidden="true" />
            Aguardando envio
          </h2>
          <ul className={styles.list}>
            {pendentes.map((p) => {
              const aumento = p.dados.tipo === TipoMovimentacao.ENTRADA;
              const data = new Date(p.criadaEm);
              const nome = `${TipoMovimentacaoLabels[p.dados.tipo]} de ${p.dados.quantidade}`;
              return (
                <li key={p.idLocal} className={`${styles.card} ${p.erro ? styles.cardFalha : styles.cardPendente}`}>
                  <div className={`${styles.iconWrap} ${aumento ? styles.iconPos : styles.iconNeg}`}>
                    {aumento ? <ArrowUp size={16} strokeWidth={2.5} /> : <ArrowDown size={16} strokeWidth={2.5} />}
                  </div>
                  <div className={styles.info}>
                    <p className={styles.type}>{TipoMovimentacaoLabels[p.dados.tipo]}</p>
                    <p className={styles.date}>
                      Registrada sem internet em {data.toLocaleDateString()} às{' '}
                      {data.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </p>
                    {p.dados.descricao && <p className={styles.motivo}>{p.dados.descricao}</p>}
                    {p.erro ? (
                      <p className={styles.statusFalha}>Recusada pelo servidor: {p.erro}</p>
                    ) : (
                      <p className={styles.statusPendente}>Será enviada quando a internet voltar</p>
                    )}
                    {p.erro && (
                      <button
                        type="button"
                        className={styles.descartar}
                        onClick={() => descartarPendente(p.idLocal)}
                        aria-label={`Descartar ${nome}`}
                      >
                        Descartar
                      </button>
                    )}
                  </div>
                  <div className={`${styles.qty} ${aumento ? styles.qtyPos : styles.qtyNeg}`}>
                    {aumento ? '+' : '−'}
                    {p.dados.quantidade}
                  </div>
                </li>
              );
            })}
          </ul>
        </section>
      )}

      {loading ? (
        <div className={styles.list}>
          {[1, 2, 3].map((i) => (
            <div key={i} className="skeleton" style={{ height: 70, borderRadius: 12 }} />
          ))}
        </div>
      ) : movimentacoes.length === 0 ? (
        <EmptyState
          icon={<History size={38} strokeWidth={1.5} />}
          title="Sem movimentações"
          description="Ainda não há registros de entrada ou saída para este produto."
        />
      ) : (
        <ul className={styles.list}>
          {movimentacoes.map((mov) => {
            const sinal = mov.aumento ? '+' : '−';
            const data = new Date(mov.dataMovimentacao);
            return (
              <li key={mov.id} className={styles.card}>
                <div className={`${styles.iconWrap} ${mov.aumento ? styles.iconPos : styles.iconNeg}`}>
                  {mov.aumento ? (
                    <ArrowUp size={16} strokeWidth={2.5} />
                  ) : (
                    <ArrowDown size={16} strokeWidth={2.5} />
                  )}
                </div>
                <div className={styles.info}>
                  <p className={styles.type}>
                    {TipoMovimentacaoLabels[mov.tipo]}
                    <span className={styles.origem}>{OrigemMovimentacaoLabels[mov.origem]}</span>
                  </p>
                  <p className={styles.date}>
                    {data.toLocaleDateString()} às{' '}
                    {data.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                  {mov.descricao && <p className={styles.motivo}>{mov.descricao}</p>}
                  <p className={styles.saldo}>
                    saldo {mov.saldoAnterior} → {mov.saldoPosterior}
                    {mov.usuarioNome && ` · ${mov.usuarioNome}`}
                  </p>
                </div>
                <div className={`${styles.qty} ${mov.aumento ? styles.qtyPos : styles.qtyNeg}`}>
                  {sinal}
                  {mov.quantidade}
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
