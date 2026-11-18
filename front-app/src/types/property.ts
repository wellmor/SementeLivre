export enum StatusComunidade {
  ATIVA = 'ATIVA',
  PENDENTE_APROVACAO = 'PENDENTE_APROVACAO',
  REJEITADA = 'REJEITADA',
}

export const StatusComunidadeLabels: Record<StatusComunidade, string> = {
  [StatusComunidade.ATIVA]: 'Ativa',
  [StatusComunidade.PENDENTE_APROVACAO]: 'Pendente de aprovação',
  [StatusComunidade.REJEITADA]: 'Rejeitada',
};

export interface Comunidade {
  idComunidade: string;
  nome: string;
  municipio: string;
  uf: string;
  status: StatusComunidade;
  dataSolicitacao: Date;
  dataAprovacao?: Date;
}

export interface Propriedade {
  idPropriedade: string;
  idProprietario: string;
  /** Vazio quando o produtor é isolado, sem comunidade. */
  idComunidade?: string;
  nomeComunidade?: string;
  nome: string;
  tamanhoHectares?: number;
  logradouro: string;
  numero: string;
  complemento?: string;
  bairro: string;
  municipio: string;
  uf: string;
  cep: string;
  dataCadastro: Date;
  dataUltimaAlteracao: Date;
}
