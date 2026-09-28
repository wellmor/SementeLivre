import type { Comunidade, Species } from "@/lib/types";
import { coordenadaDe } from "./municipios";

/**
 * Um ponto no mapa de sementes (RF-08). Representa uma comunidade, e nao uma
 * propriedade especifica.
 */
export interface PontoMapa {
  id: string;
  nome: string;
  localizacao: string;
  latitude: number;
  longitude: number;
  /** Quantas sementes/mudas essa comunidade tem publicadas no catalogo. */
  quantidadeSementes: number;
  /** Nomes das sementes, para o balaozinho do mapa. */
  sementes: string[];
}

/** Sementes que estao indisponiveis nao contam como banco ativo. */
function estaDisponivel(especie: Species): boolean {
  return especie.status !== "unavailable";
}

/**
 * Monta os pontos do mapa juntando as comunidades com as sementes do catalogo.
 *
 * Comunidade sem nenhuma semente disponivel fica de fora: o RF-08 pede as
 * propriedades "com bancos de sementes ativos". Comunidade cuja localizacao nao
 * foi reconhecida tambem fica de fora, para nao aparecer um pino no lugar
 * errado (ver municipios.ts para cadastrar o lugar).
 */
export function montarPontos(comunidades: Comunidade[], especies: Species[]): PontoMapa[] {
  const pontos: PontoMapa[] = [];

  for (const comunidade of comunidades) {
    const daComunidade = especies.filter(
      (especie) => especie.id_comunidade === comunidade.id_comunidade && estaDisponivel(especie)
    );

    if (daComunidade.length === 0) {
      continue;
    }

    const coordenada = coordenadaDe(comunidade.localizacao);

    if (!coordenada) {
      continue;
    }

    pontos.push({
      id: comunidade.id_comunidade,
      nome: comunidade.nome,
      localizacao: comunidade.localizacao,
      latitude: coordenada.latitude,
      longitude: coordenada.longitude,
      quantidadeSementes: daComunidade.length,
      sementes: daComunidade.map((especie) => especie.nome_popular),
    });
  }

  return pontos;
}

/** Comunidades que ficaram de fora por falta de coordenada cadastrada. */
export function localizacoesNaoReconhecidas(comunidades: Comunidade[]): string[] {
  return comunidades
    .filter((comunidade) => coordenadaDe(comunidade.localizacao) === null)
    .map((comunidade) => comunidade.localizacao);
}
