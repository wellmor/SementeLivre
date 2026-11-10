/**
 * Dados para usar sem internet (RNF-04, #234): a última resposta de cada GET da API.
 *
 * - Fica no localStorage, ligado a UMA conta: quando outra pessoa entra no mesmo
 *   aparelho, o que era da anterior é apagado antes (LGPD).
 * - Só vale enquanto a sessão poderia valer: o refresh token dura 7 dias a partir
 *   da última renovação. Passado isso, os dados são apagados.
 * - É apagado junto com a sessão (logout, sessão expirada, sessão inexistente):
 *   ver clearTokens em api.ts.
 */

const PREFIXO = 'sl_offline:';
const CHAVE_DONO = 'sl_offline_dono';
const CHAVE_VALIDADE = 'sl_offline_validade';

/** Mesmo prazo do refresh token no backend. */
const VALIDADE_MS = 7 * 24 * 60 * 60 * 1000;

/** Resposta grande demais não vale o espaço (o localStorage tem uns 5 MB no total). */
const MAX_CARACTERES = 1_000_000;

interface Salvo<T> {
  dados: T;
  salvoEm: string;
}

function armazenamento(): Storage | null {
  try {
    return typeof window === 'undefined' ? null : window.localStorage;
  } catch {
    return null; // modo privado restrito
  }
}

function chaves(local: Storage): string[] {
  const lista: string[] = [];
  for (let i = 0; i < local.length; i++) {
    const chave = local.key(i);
    if (chave?.startsWith(PREFIXO)) lista.push(chave);
  }
  return lista;
}

/** Apaga todos os dados salvos para uso offline. */
export function limparCacheOffline(): void {
  const local = armazenamento();
  if (!local) return;
  chaves(local).forEach((chave) => local.removeItem(chave));
  local.removeItem(CHAVE_DONO);
  local.removeItem(CHAVE_VALIDADE);
}

function expirado(local: Storage): boolean {
  const validade = Number(local.getItem(CHAVE_VALIDADE));
  return !validade || Date.now() > validade;
}

/** Sessão renovada (login ou refresh): os dados offline valem por mais 7 dias. */
export function renovarValidadeOffline(): void {
  armazenamento()?.setItem(CHAVE_VALIDADE, String(Date.now() + VALIDADE_MS));
}

/** Conta que está usando o app. Se mudou, apaga o que era da conta anterior. */
export function definirDonoOffline(id: string): void {
  const local = armazenamento();
  if (!local) return;
  if (local.getItem(CHAVE_DONO) !== id) {
    chaves(local).forEach((chave) => local.removeItem(chave));
    local.setItem(CHAVE_DONO, id);
  }
}

export function salvarOffline(caminho: string, dados: unknown): void {
  const local = armazenamento();
  if (!local || dados === undefined) return;
  try {
    const texto = JSON.stringify({ dados, salvoEm: new Date().toISOString() } satisfies Salvo<unknown>);
    if (texto.length > MAX_CARACTERES) return;
    local.setItem(PREFIXO + caminho, texto);
  } catch {
    // Sem espaço: segue sem guardar esta resposta
  }
}

/** Última resposta salva do caminho, se ainda dentro da validade. */
export function lerOffline<T>(caminho: string): Salvo<T> | null {
  const local = armazenamento();
  if (!local) return null;
  if (expirado(local)) {
    limparCacheOffline();
    return null;
  }
  try {
    const texto = local.getItem(PREFIXO + caminho);
    return texto ? (JSON.parse(texto) as Salvo<T>) : null;
  } catch {
    return null;
  }
}
