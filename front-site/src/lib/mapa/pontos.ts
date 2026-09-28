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

/**
 * Ponto do mapa de UMA semente do catalogo, usado pelo botao de localizacao
 * que aparece em cada card (RF-08).
 *
 * A localizacao vem, nesta ordem:
 * 1. do proprio card, quando o produtor informou no app
 *    (Species.localizacaoAproximada);
 * 2. da comunidade dona da semente, como hoje.
 *
 * Devolve null quando nenhuma das duas e reconhecida; nesse caso o card nao
 * mostra o botao, em vez de mostrar um pino errado.
 */
export function pontoDaEspecie(
  especie: Species,
  comunidades: Comunidade[]
): PontoMapa | null {

  const comunidade = comunidades.find(
    (c) => c.id_comunidade === especie.id_comunidade
  );

  const localizacao = especie.localizacaoAproximada ?? comunidade?.localizacao ?? "";
  const coordenada = coordenadaDe(localizacao);

  if (!coordenada) {
    return null;
  }

  return {
    id: especie.id_especie,
    nome: comunidade?.nome ?? especie.nome_popular,
    localizacao,
    latitude: coordenada.latitude,
    longitude: coordenada.longitude,
    quantidadeSementes: 1,
    sementes: [especie.nome_popular],
  };
}

/** Comunidades que ficaram de fora por falta de coordenada cadastrada. */
export function localizacoesNaoReconhecidas(comunidades: Comunidade[]): string[] {
  return comunidades
    .filter((comunidade) => coordenadaDe(comunidade.localizacao) === null)
    .map((comunidade) => comunidade.localizacao);
}
