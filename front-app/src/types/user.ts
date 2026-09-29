export type TipoDocumento = 'CPF' | 'CNPJ';

/** Endereço, no formato do LogradouroDTO do backend. */
export interface Logradouro {
  logradouro: string;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  municipio: string;
  uf: string;
  cep?: string | null;
}

export interface Pessoa {
  id: string;
  tipoDocumento: TipoDocumento;
  documento: string;
  nome: string;
  telefone: string | null;
  email: string;
  endereco: Logradouro | null;
  dataCadastro: string;
  dataUltimaAlteracao: string;
}

export interface Proprietario extends Pessoa {
  rg: string;
  exibirNoSitePublico: boolean;
}

/** Resposta de GET /auth/me (conta logada). rg e exibirNoSitePublico vêm nulos para admin. */
export interface PerfilUsuario extends Pessoa {
  tipoPessoa: 'PROPRIETARIO' | 'ADMIN';
  roles: string[];
  rg: string | null;
  exibirNoSitePublico: boolean | null;
}

/** Corpo de POST /auth/cadastrar. */
export interface ProprietarioCadastroRequest {
  nome: string;
  tipoDocumento: TipoDocumento;
  documento: string;
  rg: string;
  telefone?: string;
  email: string;
  senha: string;
  endereco: Logradouro;
}

/** Corpo de PUT /api/proprietarios/{id}. */
export interface PessoaUpdateRequest {
  nome: string;
  telefone?: string;
  email: string;
  endereco: Logradouro;
}
