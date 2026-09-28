/**
 * forms.ts — Helpers de formulário (react-hook-form).
 *
 * O backend manda erros de validação (400) e de duplicidade (409) em fieldErrors
 * ("campo: mensagem"); api.ts já os entrega indexados pelo campo do DTO.
 */

import { FieldValues, Path, UseFormRegisterReturn, UseFormSetError } from 'react-hook-form';
import { isApiError } from './api';

/** Campo registrado com máscara: formata o valor digitado antes de o react-hook-form ler. */
export function comMascara<N extends string>(
  field: UseFormRegisterReturn<N>,
  formatar: (valor: string) => string
): UseFormRegisterReturn<N> {
  return {
    ...field,
    onChange: (e) => {
      e.target.value = formatar(e.target.value);
      return field.onChange(e);
    },
  };
}

/** Campo do DTO → campo do formulário, quando os nomes diferem (ex.: documento → cpf). */
export type MapaDeCampos = Record<string, string>;

function campoDoFormulario(campoApi: string, mapa: MapaDeCampos): string {
  return mapa[campoApi] ?? campoApi.replace(/^endereco\./, '');
}

/**
 * Aplica os fieldErrors da API nos campos do formulário com setError.
 * Retorna a mensagem geral para o alerta do formulário, ou null quando não há o que
 * mostrar ali: tudo foi para os campos, ou o erro já virou toast (sem conexão / sessão expirada).
 */
export function aplicarErrosDaApi<T extends FieldValues>(
  err: unknown,
  campos: readonly string[],
  setError: UseFormSetError<T>,
  mapa: MapaDeCampos = {}
): string | null {
  if (!isApiError(err)) return 'Ocorreu um erro inesperado. Tente novamente.';
  if (err.status === 0 || err.code === 'auth/session-expired') return null;

  const erros = Object.entries(err.fieldErrors);
  let aplicados = 0;
  for (const [campoApi, mensagem] of erros) {
    const campo = campoDoFormulario(campoApi, mapa);
    if (campos.includes(campo)) {
      setError(campo as Path<T>, { type: 'server', message: mensagem }, { shouldFocus: aplicados === 0 });
      aplicados++;
    }
  }
  return erros.length > 0 && aplicados === erros.length ? null : err.message;
}
