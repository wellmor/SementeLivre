import { z } from 'zod';

export function validateCPF(cpf: string): boolean {
  const cleaned = cpf.replace(/\D/g, '');
  if (cleaned.length !== 11) return false;
  if (/^(\d)\1+$/.test(cleaned)) return false;

  let sum = 0;
  for (let i = 0; i < 9; i++) sum += parseInt(cleaned[i]) * (10 - i);
  let remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  if (remainder !== parseInt(cleaned[9])) return false;

  sum = 0;
  for (let i = 0; i < 10; i++) sum += parseInt(cleaned[i]) * (11 - i);
  remainder = (sum * 10) % 11;
  if (remainder === 10 || remainder === 11) remainder = 0;
  return remainder === parseInt(cleaned[10]);
}

// Regras espelhando o backend (DTOs + DocumentoValidator + tamanhos das colunas).
// Campos opcionais no backend (telefone, número, complemento, bairro, CEP) seguem opcionais,
// mas quando preenchidos precisam ter formato válido.

const UFS = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
];

const apenasDigitos = (v: string) => v.replace(/\D/g, '');

export const nomeSchema = z
  .string()
  .trim()
  .min(1, 'Nome é obrigatório')
  .max(150, 'Nome deve ter no máximo 150 caracteres');

export const cpfSchema = z
  .string()
  .min(1, 'CPF é obrigatório')
  .refine((v) => validateCPF(v), { message: 'CPF inválido' });

export const rgSchema = z
  .string()
  .trim()
  .min(1, 'RG é obrigatório')
  .max(20, 'RG deve ter no máximo 20 caracteres');

export const emailSchema = z
  .string()
  .trim()
  .min(1, 'E-mail é obrigatório')
  .max(255, 'E-mail deve ter no máximo 255 caracteres')
  .email('E-mail inválido');

export const senhaSchema = z
  .string()
  .min(8, 'Senha deve ter no mínimo 8 caracteres')
  .max(72, 'Senha deve ter no máximo 72 caracteres')
  .regex(/[A-Z]/, 'Senha deve ter ao menos uma letra maiúscula')
  .regex(/[0-9]/, 'Senha deve ter ao menos um número');

export const telefoneSchema = z
  .string()
  .refine((v) => [0, 10, 11].includes(apenasDigitos(v).length), { message: 'Telefone inválido' });

export const enderecoSchema = z.object({
  cep: z.string().refine((v) => [0, 8].includes(apenasDigitos(v).length), { message: 'CEP inválido' }),
  logradouro: z.string().trim().min(1, 'Logradouro é obrigatório').max(255, 'Logradouro muito longo'),
  numero: z.string().trim().max(10, 'Número deve ter no máximo 10 caracteres'),
  complemento: z.string().trim().max(100, 'Complemento deve ter no máximo 100 caracteres'),
  bairro: z.string().trim().max(100, 'Bairro deve ter no máximo 100 caracteres'),
  municipio: z.string().trim().min(1, 'Município é obrigatório').max(100, 'Município muito longo'),
  uf: z.string().refine((v) => UFS.includes(v), { message: 'UF inválida' }),
});

/** Dados editáveis do proprietário (perfil). */
export const perfilSchema = z
  .object({
    nome: nomeSchema,
    telefone: telefoneSchema,
    email: emailSchema,
  })
  .extend(enderecoSchema.shape);

export const cadastroProprietarioSchema = perfilSchema
  .extend({
    cpf: cpfSchema,
    rg: rgSchema,
    senha: senhaSchema,
    confirmarSenha: z.string(),
  })
  .refine((v) => v.senha === v.confirmarSenha, {
    message: 'Senhas não coincidem',
    path: ['confirmarSenha'],
  });

export const alterarSenhaSchema = z
  .object({
    atual: z.string().min(1, 'Senha atual é obrigatória'),
    nova: senhaSchema,
    confirmar: z.string(),
  })
  .refine((v) => v.nova === v.confirmar, {
    message: 'As senhas não coincidem',
    path: ['confirmar'],
  });

export type PerfilForm = z.infer<typeof perfilSchema>;
export type CadastroProprietarioForm = z.infer<typeof cadastroProprietarioSchema>;

/** Valida com o schema e devolve o primeiro erro de cada campo ({} se estiver tudo certo). */
export function validarFormulario(schema: z.ZodType, dados: unknown): Record<string, string> {
  const result = schema.safeParse(dados);
  const erros: Record<string, string> = {};
  if (!result.success) {
    for (const issue of result.error.issues) {
      const campo = issue.path.join('.');
      erros[campo] ??= issue.message;
    }
  }
  return erros;
}

/** Converte o nome do campo nos erros da API ("documento", "endereco.uf") para o do formulário. */
export function campoDoFormulario(campoApi: string): string {
  if (campoApi === 'documento') return 'cpf';
  return campoApi.replace(/^endereco\./, '');
}

/** Monta o corpo de dados pessoais + endereço no formato da API (só dígitos em telefone e CEP). */
export function paraPessoaRequest(form: PerfilForm) {
  const opcional = (v: string) => v.trim() || undefined;
  return {
    nome: form.nome.trim(),
    telefone: opcional(apenasDigitos(form.telefone)),
    email: form.email.trim(),
    endereco: {
      logradouro: form.logradouro.trim(),
      numero: opcional(form.numero),
      complemento: opcional(form.complemento),
      bairro: opcional(form.bairro),
      municipio: form.municipio.trim(),
      uf: form.uf,
      cep: opcional(apenasDigitos(form.cep)),
    },
  };
}

export interface ViaCEPResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean;
}

export async function fetchCEP(cep: string): Promise<ViaCEPResponse | null> {
  const cleaned = cep.replace(/\D/g, '');
  if (cleaned.length !== 8) return null;
  try {
    const res = await fetch(`https://viacep.com.br/ws/${cleaned}/json/`);
    const data: ViaCEPResponse = await res.json();
    if (data.erro) return null;
    return data;
  } catch {
    return null;
  }
}

export function formatCPF(value: string): string {
  return value
    .replace(/\D/g, '')
    .slice(0, 11)
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
}

export function formatTelefone(value: string): string {
  const d = value.replace(/\D/g, '').slice(0, 11);
  if (d.length <= 10) {
    return d.replace(/(\d{2})(\d{4})(\d{0,4})/, '($1) $2-$3');
  }
  return d.replace(/(\d{2})(\d{5})(\d{0,4})/, '($1) $2-$3');
}

export function formatCEP(value: string): string {
  return value
    .replace(/\D/g, '')
    .slice(0, 8)
    .replace(/(\d{5})(\d)/, '$1-$2');
}
