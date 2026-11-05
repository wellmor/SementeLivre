import tabela from "./coordenadas-ibge.json";

/**
 * Coordenada APROXIMADA de um município ou estado (RF-08).
 *
 * O pino é sempre a sede do município (ou o centro do estado), nunca o endereço
 * da comunidade ou de uma propriedade: o mapa mostra onde fica a região, não
 * onde as famílias moram. Por isso não há geocodificação de endereço.
 *
 * Fonte: tabela de sedes municipais do IBGE, via kelvins/municipios-brasileiros
 * (MIT). A tabela só é lida no servidor (rota /api/mapa); o navegador recebe
 * apenas os pontos prontos.
 */
export interface Coordenada {
  latitude: number;
  longitude: number;
  /** "municipio": sede do município; "estado": município não reconhecido, centro da UF. */
  precisao: "municipio" | "estado";
}

const { estados, municipios } = tabela as unknown as {
  estados: Record<string, [number, number]>;
  municipios: Record<string, Record<string, [number, number]>>;
};

/** Mesma regra usada para gerar as chaves da tabela: sem acento, minúsculo, só letras e números. */
export function normalizar(texto: string): string {
  return texto
    .normalize("NFD")
    .replace(/[̀-ͯ]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, " ")
    .trim();
}

/**
 * Coordenada do município na UF; sem município reconhecido, o centro da UF.
 * Devolve null quando nem a UF é reconhecida, para não pôr um pino no lugar errado.
 */
export function coordenadaDe(municipio: string | null, uf: string | null): Coordenada | null {
  const sigla = uf?.trim().toUpperCase() ?? "";
  if (!estados[sigla]) return null;

  const sede = municipio ? municipios[sigla]?.[normalizar(municipio)] : undefined;
  const [latitude, longitude] = sede ?? estados[sigla];
  return { latitude, longitude, precisao: sede ? "municipio" : "estado" };
}
