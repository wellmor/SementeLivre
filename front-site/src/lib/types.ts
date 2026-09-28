export interface Proprietario {
  nome: string;
  telefone: string;
  cpf: string;
}

export interface Property {
  id_propriedade: string;
  nome: string;
  endereco: string;
  area_total: number;
}

export interface Plantio {
  id_plantio: string;
  id_propriedade: string;
  id_especie: string;
  data_inicio: string;
  previsao_colheita: string;
  area_plantada: number;
  talhao: string;
  status: "ativo" | "concluido" | "cancelado";
}

export type SpeciesStatus = "exchange" | "sale" | "donation" | "unavailable";

export interface Species {
  /**
   * Localizacao aproximada informada pelo produtor no app (RF-08).
   * Formato: "Municipio - UF", por exemplo "Rio Pomba - MG".
   * Opcional: quando vem vazia, o mapa usa a localizacao da comunidade.
   */
  localizacaoAproximada?: string;
  id_especie: string;
  nome_popular: string;
  nome_cientifico: string;
  familia_botanica: string;
  descricao: string;
  foto: string;
  status: SpeciesStatus;
  id_comunidade?: string;
  tipoSemente?: TipoSemente;
  quantidadeEstoque?: number;
  pesoEstoque?: number;
  preco?: number;
  formaPrecificacao?: string;
  unidadePesagem?: UnidadePesagem;
}

export interface Estoque {
  id_estoque: string;
  id_especie: string;
  quantidade: number;
}

export interface Colheita {
  id_colheita: string;
  id_especie: string;
  data: string;
  quantidade: number;
}

// ── Multi-community types ──────────────────────────────────────────────────

export interface Comunidade {
  id_comunidade: string;
  nome: string;
  localizacao: string;
  status: "ativa" | "inativa";
}

export type StatusSolicitacao = "pendente" | "aprovada" | "rejeitada";

export interface SolicitacaoCadastro {
  id_solicitacao: string;
  nome_responsavel: string;
  email: string;
  senha: string;
  nome_comunidade: string;
  localizacao: string;
  documento_nome: string;
  documento_base64: string;
  status: StatusSolicitacao;
  data_solicitacao: string;
  observacao: string;
}

export interface ContaProdutor {
  id_conta: string;
  email: string;
  senha: string;
  nome: string;
  id_comunidade: string;
}

export type TipoSemente = "HORTALICA" | "FRUTIFERA" | "FORRAGEIRA" | "CEREAL" | "LEGUMINOSA" | "OUTRAS";
export type UnidadePesagem = "SACA" | "KG" | "GRAMA" | "MG" | "UNIDADE";
export type TipoPedido = "VENDA" | "TROCA" | "DOACAO";
export type StatusPedido = "PENDENTE" | "CONFIRMADO" | "CANCELADO";

export interface Pedido {
  id_pedido: string;
  id_especie: string;
  id_comunidade: string;
  tipoPedido: TipoPedido;
  status: StatusPedido;
  nomeRecebedor: string;
  contatoRecebedor: string;
  mensagemOpcional: string;
  quantidade: number;
  dataPedido: string;
}

export interface Notificacao {
  id_notificacao: string;
  id_comunidade: string;
  id_pedido: string;
  titulo: string;
  mensagem: string;
  lida: boolean;
  dataGeracao: string;
}
