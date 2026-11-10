// Filtros da listagem pública (RF-04 / CDU-01). Tudo roda no navegador, sobre o
// catálogo inteiro que /api/catalog devolve, e cada filtro vira um parâmetro da
// URL para a busca filtrada poder ser compartilhada.
import type { Disponibilidade, OfertaCatalogo, TipoProduto } from "./types";
import { DISPONIBILIDADE_CONFIG, TIPO_PRODUTO_LABEL } from "./catalogo";

export type Regiao = "NORTE" | "NORDESTE" | "CENTRO_OESTE" | "SUDESTE" | "SUL";

export const REGIAO_LABEL: Record<Regiao, string> = {
  NORTE: "Norte",
  NORDESTE: "Nordeste",
  CENTRO_OESTE: "Centro-Oeste",
  SUDESTE: "Sudeste",
  SUL: "Sul",
};

// Regiões do IBGE
const REGIAO_DA_UF: Record<string, Regiao> = {
  AC: "NORTE", AP: "NORTE", AM: "NORTE", PA: "NORTE", RO: "NORTE", RR: "NORTE", TO: "NORTE",
  AL: "NORDESTE", BA: "NORDESTE", CE: "NORDESTE", MA: "NORDESTE", PB: "NORDESTE",
  PE: "NORDESTE", PI: "NORDESTE", RN: "NORDESTE", SE: "NORDESTE",
  DF: "CENTRO_OESTE", GO: "CENTRO_OESTE", MT: "CENTRO_OESTE", MS: "CENTRO_OESTE",
  ES: "SUDESTE", MG: "SUDESTE", RJ: "SUDESTE", SP: "SUDESTE",
  PR: "SUL", RS: "SUL", SC: "SUL",
};

export function regiaoDaUf(uf: string | null | undefined): Regiao | null {
  return uf ? REGIAO_DA_UF[uf.toUpperCase()] ?? null : null;
}

/** Períodos dos filtros de data, em dias ("" = qualquer data). */
export const PERIODOS = ["7", "30", "90", "365"] as const;
export type Periodo = (typeof PERIODOS)[number] | "";

export const PERIODO_LABEL: Record<(typeof PERIODOS)[number], string> = {
  "7": "Últimos 7 dias",
  "30": "Últimos 30 dias",
  "90": "Últimos 90 dias",
  "365": "Último ano",
};

export interface FiltrosCatalogo {
  busca: string;
  produtor: string;
  disponibilidade: Disponibilidade | "all";
  tipo: TipoProduto | "all";
  comunidade: string; // id, ou "all"
  regiao: Regiao | "all";
  uf: string; // sigla, ou "all"
  municipio: string; // nome, ou "all" (sempre junto com a UF)
  incluido: Periodo;
  alterado: Periodo;
}

export const FILTROS_VAZIOS: FiltrosCatalogo = {
  busca: "",
  produtor: "",
  disponibilidade: "all",
  tipo: "all",
  comunidade: "all",
  regiao: "all",
  uf: "all",
  municipio: "all",
  incluido: "",
  alterado: "",
};

export function temFiltroAtivo(f: FiltrosCatalogo): boolean {
  return (Object.keys(FILTROS_VAZIOS) as (keyof FiltrosCatalogo)[]).some(
    (k) => f[k].trim() !== FILTROS_VAZIOS[k]
  );
}

/** Comparação sem caixa nem acento: "feijao" acha "Feijão". */
export function normalizar(texto: string): string {
  return texto.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase().trim();
}

// O backend manda LocalDateTime sem fuso ("2026-11-06T10:20:30"): lido como hora local
function dataDe(valor: string | null | undefined): Date | null {
  if (!valor) return null;
  const data = new Date(valor);
  return Number.isNaN(data.getTime()) ? null : data;
}

function dentroDoPeriodo(valor: string | null | undefined, periodo: Periodo, agora: number): boolean {
  if (!periodo) return true;
  const data = dataDe(valor);
  if (!data) return false;
  return data.getTime() >= agora - Number(periodo) * 24 * 60 * 60 * 1000;
}

export function filtrarOfertas(
  ofertas: OfertaCatalogo[],
  f: FiltrosCatalogo,
  agora: number = Date.now()
): OfertaCatalogo[] {
  const busca = normalizar(f.busca);
  const produtor = normalizar(f.produtor);
  return ofertas.filter(
    (o) =>
      (!busca || normalizar(o.nomePopular).includes(busca)) &&
      (!produtor || (!!o.nomeProdutor && normalizar(o.nomeProdutor).includes(produtor))) &&
      (f.disponibilidade === "all" || o.disponibilidade === f.disponibilidade) &&
      (f.tipo === "all" || o.tipo === f.tipo) &&
      (f.comunidade === "all" || o.comunidadeId === f.comunidade) &&
      (f.regiao === "all" || regiaoDaUf(o.uf) === f.regiao) &&
      (f.uf === "all" || o.uf === f.uf) &&
      (f.municipio === "all" || o.municipio === f.municipio) &&
      dentroDoPeriodo(o.dataInclusao, f.incluido, agora) &&
      dentroDoPeriodo(o.dataUltimaAlteracao, f.alterado, agora)
  );
}

const ordenar = (a: string, b: string) => a.localeCompare(b, "pt-BR");

/** Opções dos selects, só com o que existe no catálogo (como já era com a comunidade). */
export function opcoesDosFiltros(ofertas: OfertaCatalogo[], f: Pick<FiltrosCatalogo, "regiao" | "uf">) {
  const comunidades = Array.from(
    new Map(
      ofertas
        .filter((o) => o.comunidadeId && o.comunidade)
        .map((o) => [o.comunidadeId as string, o.comunidade as string])
    )
  ).sort((a, b) => ordenar(a[1], b[1]));

  const ufs = Array.from(new Set(ofertas.map((o) => o.uf).filter((uf): uf is string => !!uf))).sort(ordenar);
  const regioes = (Object.keys(REGIAO_LABEL) as Regiao[]).filter((r) => ufs.some((uf) => regiaoDaUf(uf) === r));
  const ufsDaRegiao = f.regiao === "all" ? ufs : ufs.filter((uf) => regiaoDaUf(uf) === f.regiao);

  // Município vai com a UF: o mesmo nome existe em mais de um estado
  const municipios = Array.from(
    new Map(
      ofertas
        .filter((o) => o.municipio && o.uf && ufsDaRegiao.includes(o.uf) && (f.uf === "all" || o.uf === f.uf))
        .map((o) => [`${o.municipio}|${o.uf}`, { municipio: o.municipio as string, uf: o.uf as string }])
    ).values()
  ).sort((a, b) => ordenar(a.municipio, b.municipio) || ordenar(a.uf, b.uf));

  const produtores = Array.from(
    new Set(ofertas.map((o) => o.nomeProdutor).filter((n): n is string => !!n))
  ).sort(ordenar);

  return { comunidades, regioes, ufs: ufsDaRegiao, municipios, produtores };
}

// ── URL ──

const PARAMETRO: Record<keyof FiltrosCatalogo, string> = {
  busca: "q",
  produtor: "produtor",
  disponibilidade: "disponibilidade",
  tipo: "tipo",
  comunidade: "comunidade",
  regiao: "regiao",
  uf: "uf",
  municipio: "municipio",
  incluido: "incluido",
  alterado: "alterado",
};

/**
 * Filtros vindos da URL. Valor que não existe no catálogo (comunidade, estado,
 * município) ou fora das opções é ignorado, como o ?comunidade= do /mapa já fazia.
 */
export function filtrosDaUrl(params: URLSearchParams, ofertas: OfertaCatalogo[]): FiltrosCatalogo {
  const ler = (k: keyof FiltrosCatalogo) => params.get(PARAMETRO[k])?.trim() ?? "";
  const f: FiltrosCatalogo = { ...FILTROS_VAZIOS, busca: ler("busca"), produtor: ler("produtor") };

  const disponibilidade = ler("disponibilidade");
  if (Object.hasOwn(DISPONIBILIDADE_CONFIG, disponibilidade)) f.disponibilidade = disponibilidade as Disponibilidade;
  const tipo = ler("tipo");
  if (Object.hasOwn(TIPO_PRODUTO_LABEL, tipo)) f.tipo = tipo as TipoProduto;

  const comunidade = ler("comunidade");
  if (comunidade && ofertas.some((o) => o.comunidadeId === comunidade)) f.comunidade = comunidade;

  const regiao = ler("regiao");
  if (Object.hasOwn(REGIAO_LABEL, regiao)) f.regiao = regiao as Regiao;
  const uf = ler("uf").toUpperCase();
  if (uf && ofertas.some((o) => o.uf === uf)) f.uf = uf;
  // Link com ?municipio= sem ?uf=: o estado vem do próprio catálogo
  const municipio = ler("municipio");
  const doMunicipio = municipio && ofertas.find((o) => o.municipio === municipio && o.uf && (f.uf === "all" || o.uf === f.uf));
  if (doMunicipio) {
    f.municipio = municipio;
    f.uf = doMunicipio.uf as string;
  }

  for (const k of ["incluido", "alterado"] as const) {
    const periodo = ler(k);
    if ((PERIODOS as readonly string[]).includes(periodo)) f[k] = periodo as Periodo;
  }
  return filtrosCoerentes(f, ofertas);
}

/**
 * Mantém região, estado e município de acordo entre si: trocar a região tira o
 * estado de outra região, e o município só fica com o estado dele marcado.
 */
export function filtrosCoerentes(f: FiltrosCatalogo, ofertas: OfertaCatalogo[]): FiltrosCatalogo {
  const uf = f.regiao !== "all" && f.uf !== "all" && regiaoDaUf(f.uf) !== f.regiao ? "all" : f.uf;
  const municipio =
    f.municipio !== "all" && uf !== "all" && ofertas.some((o) => o.municipio === f.municipio && o.uf === uf)
      ? f.municipio
      : "all";
  return uf === f.uf && municipio === f.municipio ? f : { ...f, uf, municipio };
}

/** Query string com só os filtros ativos ("" sem nenhum). */
export function queryDosFiltros(f: FiltrosCatalogo): string {
  const params = new URLSearchParams();
  for (const k of Object.keys(PARAMETRO) as (keyof FiltrosCatalogo)[]) {
    const valor = f[k].trim();
    if (valor && valor !== FILTROS_VAZIOS[k]) params.set(PARAMETRO[k], valor);
  }
  const query = params.toString();
  return query ? `?${query}` : "";
}

/** Data curta no padrão brasileiro: "06/11/2026". */
export function formatarData(valor: string | null | undefined): string | null {
  const data = dataDe(valor);
  return data ? data.toLocaleDateString("pt-BR") : null;
}
