/**
 * Coordenadas aproximadas por municipio/estado (RF-08).
 *
 * O requisito pede localizacao APROXIMADA, por comunidade ou municipio, e nao
 * o endereco exato das propriedades. Por isso o mapa usa estes pontos fixos em
 * vez de geocodificar endereco, o que tambem protege a privacidade das
 * familias.
 *
 * Fonte das coordenadas: IBGE (sede dos municipios) e centro aproximado dos
 * estados. Para acrescentar um lugar novo, basta adicionar uma linha aqui.
 */

export interface Coordenada {
  latitude: number;
  longitude: number;
}

/** Chave: "municipio-uf" ou "uf", tudo em minusculo e sem acento. */
const COORDENADAS: Record<string, Coordenada> = {
  // Municipios
  "rio pomba-mg": { latitude: -21.2764, longitude: -43.1789 },
  "juiz de fora-mg": { latitude: -21.7642, longitude: -43.3503 },
  "ubá-mg": { latitude: -21.12, longitude: -42.9428 },
  "uba-mg": { latitude: -21.12, longitude: -42.9428 },
  "recife-pe": { latitude: -8.0476, longitude: -34.877 },
  "salvador-ba": { latitude: -12.9777, longitude: -38.5016 },
  "maceió-al": { latitude: -9.6498, longitude: -35.7089 },
  "maceio-al": { latitude: -9.6498, longitude: -35.7089 },

  // Estados (usados quando so se sabe a UF)
  mg: { latitude: -18.5122, longitude: -44.555 },
  pe: { latitude: -8.8137, longitude: -36.9541 },
  ba: { latitude: -12.5797, longitude: -41.7007 },
  al: { latitude: -9.5713, longitude: -36.782 },
  rj: { latitude: -22.9099, longitude: -43.2095 },
  sp: { latitude: -23.5505, longitude: -46.6333 },
  es: { latitude: -19.1834, longitude: -40.3089 },
  go: { latitude: -15.827, longitude: -49.8362 },
};

/** Centro do Brasil: usado quando nada e reconhecido. */
export const CENTRO_PADRAO: Coordenada = { latitude: -15.7801, longitude: -47.9292 };

function normalizar(texto: string): string {
  return texto
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "");
}

/**
 * Descobre a coordenada a partir de um texto de localizacao.
 *
 * Aceita os formatos que hoje aparecem no sistema:
 * - "Rio Pomba - MG"  (municipio e UF)
 * - "Pernambuco - PE" (estado e UF)
 * - "MG"              (so a UF)
 *
 * Devolve null quando nao reconhece, para o chamador decidir o que fazer.
 */
export function coordenadaDe(localizacao: string | null | undefined): Coordenada | null {
  if (!localizacao) {
    return null;
  }

  const partes = localizacao.split("-").map((parte) => normalizar(parte));

  if (partes.length >= 2) {
    const municipio = partes[0];
    const uf = partes[partes.length - 1];

    const porMunicipio = COORDENADAS[`${municipio}-${uf}`];
    if (porMunicipio) {
      return porMunicipio;
    }

    const porUf = COORDENADAS[uf];
    if (porUf) {
      return porUf;
    }
  }

  return COORDENADAS[normalizar(localizacao)] ?? null;
}
