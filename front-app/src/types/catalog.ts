import { EspecieGeral, FormatoProduto, TipoProduto } from './seed';
import { Pesagem } from './stock';

export type CatalogoDisponibilidade =
  | 'PARA_VENDA'
  | 'PARA_TROCA'
  | 'PARA_DOACAO';

export interface CatalogoProduto {
  id: string;
  nomePopular: string;
  nomeCientifico?: string;
  historico?: string;
  urlFoto?: string;
  tipo: TipoProduto;
  especie: EspecieGeral;
  formato: FormatoProduto;
  familiaBotanica?: string;

  estoqueId: string;
  descricao?: string;
  preco: number;
  quantidade: number;
  tipoPesagem: Pesagem;
  disponibilidade: CatalogoDisponibilidade;

  comunidadeId?: string;
  comunidade?: string;
  municipio?: string;
  uf?: string;
}

export interface CatalogoOferta {
  estoqueId: string;
  descricao?: string;
  preco: number;
  quantidade: number;
  tipoPesagem: Pesagem;
  disponibilidade: CatalogoDisponibilidade;
}

export interface CatalogoProdutoDetalhe {
  id: string;
  nomePopular: string;
  nomeCientifico?: string;
  historico?: string;
  urlFoto?: string;
  tipo: TipoProduto;
  especie: EspecieGeral;
  formato: FormatoProduto;
  familiaBotanica?: string;

  comunidadeId?: string;
  comunidade?: string;
  municipio?: string;
  uf?: string;

  ofertas: CatalogoOferta[];
}

export interface CatalogoPage {
  content: CatalogoProduto[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}