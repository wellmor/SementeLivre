/** Produto de uma comunidade no mapa: nome e foto, como no card do catálogo. */
export interface ProdutoNoMapa {
  nome: string;
  urlFoto: string | null;
}

/**
 * Um ponto do mapa de sementes (RF-08). Representa uma COMUNIDADE com banco de
 * sementes ativo, nunca um produtor ou uma propriedade.
 *
 * Só leva o que o catálogo público já mostra (nome da comunidade, município, UF
 * e nome e foto dos produtos). Nenhum dado de produtor entra aqui.
 */
export interface PontoMapa {
  comunidadeId: string;
  comunidade: string;
  /** "Município - UF", ou só a UF. */
  localizacao: string;
  latitude: number;
  longitude: number;
  precisao: "municipio" | "estado";
  /** Produtos publicados, sem repetir nome, em ordem alfabética. */
  produtos: ProdutoNoMapa[];
}
