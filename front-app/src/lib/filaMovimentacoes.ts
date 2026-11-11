/**
 * Fila de movimentações de estoque registradas sem internet (RNF-04, #234).
 *
 * - Fica no localStorage do aparelho, cada item ligado à conta que o criou: só
 *   essa conta vê e envia o que é dela.
 * - O envio é em ordem (do mais antigo ao mais novo), um de cada vez, como se a
 *   pessoa tivesse registrado com internet.
 * - Sem conexão no envio: para e tenta de novo depois. Recusada pelo servidor (ex.:
 *   saída maior que o saldo): fica marcada com o motivo, até a pessoa descartar.
 * - Sem duplicar: o backend não tem chave de idempotência, e um envio pode chegar
 *   ao servidor sem a resposta voltar (conexão caiu, página recarregada no meio).
 *   Antes de cada envio o item guarda quantas movimentações iguais o histórico do
 *   estoque já tem; se a contagem subiu na próxima tentativa, aquele envio chegou
 *   e não é repetido. Sem relógio no meio (o do aparelho e o do servidor diferem).
 */

import { BASE_URL, isApiError } from './api';
import { buscarMovimentacoesApi, NovaMovimentacao, registrarMovimentacaoApi } from './estoqueApi';

const CHAVE = 'sl_fila_movimentacoes';

/** Disparado na janela quando a fila muda (para as telas se atualizarem). */
export const EVENTO_FILA = 'sl:fila-movimentacoes';

export interface MovimentacaoPendente {
  idLocal: string;
  donoId: string;
  estoqueId: string;
  dados: NovaMovimentacao;
  criadaEm: string;
  /** Motivo da recusa do servidor; sem ele, ainda aguarda envio. */
  erro?: string;
  /** Movimentações iguais no histórico antes da última tentativa de envio. */
  iguaisNoHistorico?: number;
}

export interface ResultadoSincronizacao {
  enviadas: number;
  recusadas: number;
}

function ler(): MovimentacaoPendente[] {
  try {
    return JSON.parse(localStorage.getItem(CHAVE) ?? '[]') as MovimentacaoPendente[];
  } catch {
    return [];
  }
}

function gravar(fila: MovimentacaoPendente[]): void {
  try {
    if (fila.length) localStorage.setItem(CHAVE, JSON.stringify(fila));
    else localStorage.removeItem(CHAVE);
  } catch {
    // sem localStorage: a movimentação não fica guardada (a tela avisa pelo erro)
    throw new Error('Não foi possível guardar a movimentação neste aparelho.');
  }
  window.dispatchEvent(new Event(EVENTO_FILA));
}

/** Pendências da conta (de um estoque, se informado), da mais antiga para a mais nova. */
export function listarPendentes(donoId: string, estoqueId?: string): MovimentacaoPendente[] {
  return ler().filter((item) => item.donoId === donoId && (!estoqueId || item.estoqueId === estoqueId));
}

export function enfileirarMovimentacao(
  donoId: string,
  estoqueId: string,
  dados: NovaMovimentacao
): MovimentacaoPendente {
  const item: MovimentacaoPendente = {
    idLocal: crypto.randomUUID(),
    donoId,
    estoqueId,
    dados,
    criadaEm: new Date().toISOString(),
  };
  gravar([...ler(), item]);
  return item;
}

/**
 * Avisa quando a fila muda, nesta aba (EVENTO_FILA) ou em outra aba do app
 * (evento "storage" do navegador): a sincronização pode rodar em qualquer aba.
 * Devolve a função que cancela a escuta.
 */
export function ouvirFila(aoMudar: () => void): () => void {
  const deOutraAba = (evento: StorageEvent) => {
    if (evento.key === CHAVE || evento.key === null) aoMudar();
  };
  window.addEventListener(EVENTO_FILA, aoMudar);
  window.addEventListener('storage', deOutraAba);
  return () => {
    window.removeEventListener(EVENTO_FILA, aoMudar);
    window.removeEventListener('storage', deOutraAba);
  };
}

export function descartarPendente(idLocal: string): void {
  gravar(ler().filter((item) => item.idLocal !== idLocal));
}

/**
 * A pessoa saiu da conta de propósito: o que ela guardou sem internet (com o motivo
 * digitado) não fica no aparelho (LGPD). Na sessão que só expira a fila fica, para
 * ser enviada quando ela entrar de novo. As pendências de outras contas ficam.
 */
export function descartarPendentesDaConta(donoId: string): void {
  try {
    gravar(ler().filter((item) => item.donoId !== donoId));
  } catch {
    // sem localStorage: não há fila guardada
  }
}

/**
 * O servidor responde? Sondagem silenciosa (fetch direto, sem o toast de "sem
 * conexão" do api.ts): a sincronização roda sozinha de tempos em tempos e não pode
 * encher a tela de avisos quando o Wi-Fi está ligado mas sem internet.
 */
async function servidorAlcancavel(): Promise<boolean> {
  try {
    await fetch(`${BASE_URL}/catalogo/produtos?size=1`, { cache: 'no-store' });
    return true;
  } catch {
    return false;
  }
}

/** Quantas movimentações iguais a esta (tipo, quantidade e motivo) o histórico recente tem. */
async function contarIguais(item: MovimentacaoPendente): Promise<number> {
  const { content } = await buscarMovimentacoesApi(item.estoqueId, 0, 50);
  return content.filter(
    (m) =>
      m.tipo === item.dados.tipo &&
      Number(m.quantidade) === Number(item.dados.quantidade) &&
      (m.descricao ?? '') === (item.dados.descricao ?? '')
  ).length;
}

function atualizarItem(idLocal: string, mudanca: Partial<MovimentacaoPendente>): void {
  gravar(ler().map((p) => (p.idLocal === idLocal ? { ...p, ...mudanca } : p)));
}

function removerItem(idLocal: string): void {
  gravar(ler().filter((p) => p.idLocal !== idLocal));
}

let emAndamento: Promise<ResultadoSincronizacao> | null = null;

/** Envia as pendências da conta que ainda não foram recusadas. Uma sincronização por vez. */
export function sincronizarFila(donoId: string): Promise<ResultadoSincronizacao> {
  if (emAndamento) return emAndamento;

  const enviar = async (): Promise<ResultadoSincronizacao> => {
    const resultado: ResultadoSincronizacao = { enviadas: 0, recusadas: 0 };
    const pendentes = listarPendentes(donoId).filter((p) => !p.erro);
    if (!pendentes.length || !(await servidorAlcancavel())) return resultado;
    for (const item of pendentes) {
      try {
        const iguais = await contarIguais(item);
        // Uma tentativa anterior chegou ao servidor sem a resposta voltar: não repete
        if (item.iguaisNoHistorico !== undefined && iguais > item.iguaisNoHistorico) {
          removerItem(item.idLocal);
          resultado.enviadas++;
          continue;
        }
        atualizarItem(item.idLocal, { iguaisNoHistorico: iguais });
        await registrarMovimentacaoApi(item.estoqueId, item.dados);
        removerItem(item.idLocal);
        resultado.enviadas++;
      } catch (err) {
        // Sem conexão ou sessão a renovar: para e tenta na próxima vez
        if (!isApiError(err) || err.status === 0 || err.status === 401) break;
        atualizarItem(item.idLocal, { erro: err.message });
        resultado.recusadas++;
      }
    }
    return resultado;
  };

  emAndamento = (async () => {
    try {
      // Entre abas também: duas enviando a mesma fila duplicariam as movimentações
      const locks = typeof navigator !== 'undefined' ? navigator.locks : undefined;
      return locks ? await locks.request('sl-fila-movimentacoes', enviar) : await enviar();
    } finally {
      emAndamento = null;
    }
  })();
  return emAndamento;
}
