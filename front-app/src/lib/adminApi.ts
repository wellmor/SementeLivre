/**
 * adminApi.ts — Administradores (área de administração).
 *
 *  GET  /admin/administradores → AdminDTO[] (qualquer admin; SUPER_ADMIN primeiro)
 *  POST /admin/administradores → AdminDTO (só SUPER_ADMIN; 403 para os outros)
 *    409: e-mail ou documento já usado por outra conta.
 */

import { apiGet, apiPost } from './api';

export type NivelAcesso = 'SUPER_ADMIN' | 'ADMIN' | 'MODERADOR';

export const NivelAcessoLabels: Record<NivelAcesso, string> = {
  SUPER_ADMIN: 'Administrador principal',
  ADMIN: 'Administrador',
  MODERADOR: 'Moderador',
};

export interface AdminDTO {
  id: string;
  nome: string;
  email: string;
  nivelAcesso: NivelAcesso;
  dataCadastro: string | null;
}

export interface AdminCreateRequestDTO {
  nome: string;
  email: string;
  tipoDocumento: 'CPF';
  documento: string;
  /** Provisória: a pessoa troca no perfil depois de entrar. */
  senha: string;
  nivelAcesso: 'ADMIN' | 'SUPER_ADMIN';
}

export function listarAdministradores(): Promise<AdminDTO[]> {
  return apiGet<AdminDTO[]>('/admin/administradores');
}

export function cadastrarAdministrador(dados: AdminCreateRequestDTO): Promise<AdminDTO> {
  return apiPost<AdminDTO>('/admin/administradores', dados);
}
