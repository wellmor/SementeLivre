export enum TipoPedido {
  VENDA = 'VENDA',
  TROCA = 'TROCA',
  DOACAO = 'DOACAO',
}

export const TipoPedidoLabels: Record<TipoPedido, string> = {
  [TipoPedido.VENDA]: 'Venda',
  [TipoPedido.TROCA]: 'Troca',
  [TipoPedido.DOACAO]: 'Doação',
};

export enum StatusPedido {
  PENDENTE = 'PENDENTE',
  CONFIRMADO = 'CONFIRMADO',
  CANCELADO = 'CANCELADO',
}

export const StatusPedidoLabels: Record<StatusPedido, string> = {
  [StatusPedido.PENDENTE]: 'Pendente',
  [StatusPedido.CONFIRMADO]: 'Confirmado',
  [StatusPedido.CANCELADO]: 'Cancelado',
};

export interface ItemPedido {
  idItem: string;
  idProduto: string;
  nomePopular: string;
  quantidade: number;
  tipoPesagem?: string;
  precoUnitario?: number;
}

export interface Pedido {
  idPedido: string;
  idProprietario: string;
  tipoPedido: TipoPedido;
  status: StatusPedido;
  nomeRecebedor: string;
  contatoRecebedor?: string;
  /** Dados do recebedor como vieram do backend (o formulário de alteração parte deles). */
  recebedor?: { nome: string; telefone?: string; email?: string };
  mensagemOpcional?: string;
  dataPedido: Date;
  itens: ItemPedido[];
  totalValor?: number;
}

/** Filtros do histórico de pedidos (GET /pedidos). Datas em ISO (yyyy-MM-dd). */
export interface FiltroPedidos {
  dataInicio?: string;
  dataFim?: string;
  tipoPedido?: TipoPedido;
  produtoId?: string;
  status?: StatusPedido;
}

/** Dados do formulário de novo pedido enviados ao backend. */
export interface NovoPedido {
  tipoPedido: TipoPedido;
  nomeRecebedor: string;
  contatoRecebedor?: string;
  mensagemOpcional?: string;
  itens: { idProduto: string; quantidade: number; precoUnitario?: number }[];
}

/** Dados do formulário de alteração (CDU-13). O e-mail é o de quem pediu pelo site. */
export interface AlteracaoPedido extends NovoPedido {
  emailRecebedor?: string;
}
