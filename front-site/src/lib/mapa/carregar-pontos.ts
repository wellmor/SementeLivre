import type { PontoMapa } from "./pontos";

let emAndamento: Promise<PontoMapa[]> | null = null;

/**
 * Pontos do mapa, buscados uma vez só por visita: os botões "Ver no mapa" de
 * vários cards dividem a mesma busca. Se falhar, a próxima chamada tenta de novo.
 */
export function carregarPontos(): Promise<PontoMapa[]> {
  emAndamento ??= fetch("/api/mapa")
    .then((r) => {
      if (!r.ok) throw new Error("mapa indisponível");
      return r.json() as Promise<PontoMapa[]>;
    })
    .catch((erro) => {
      emAndamento = null;
      throw erro;
    });
  return emAndamento;
}
