/**
 * Um ponto do mapa de sementes (RF-08). Representa uma COMUNIDADE com banco de
 * sementes ativo, nunca um produtor ou uma propriedade.
 *
 * Só leva o que o catálogo público já mostra (nome da comunidade, município, UF
 * e nomes dos produtos). Nenhum dado de produtor entra aqui.
 */
export interface PontoMapa {
  comunidadeId: string;
  comunidade: string;
  /** "Município - UF", ou só a UF. */
  localizacao: string;
  latitude: number;
  longitude: number;
  precisao: "municipio" | "estado";
  /** Nomes populares dos produtos publicados, sem repetição, em ordem alfabética. */
  produtos: string[];
}
