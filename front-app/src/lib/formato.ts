/** Valor em reais no padrão brasileiro: 12.5 → "R$ 12,50". */
export function formatarMoeda(valor: number): string {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

/** Número com duas casas e vírgula, sem o "R$" (colunas de tabela). */
export function formatarDecimal(valor: number): string {
  return valor.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
