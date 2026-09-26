'use client';

/**
 * cadastrar/page.tsx — Tela de cadastro integrada ao backend REST.
 *
 * ALTERAÇÕES em relação à versão anterior:
 *   - Removida importação de `createUserAndNotify` de `@/lib/auth`
 *   - Removida importação de `dbSet` de `@/lib/db`
 *     (o backend persiste tudo no PostgreSQL; não precisamos mais do localStorage)
 *   - Adicionadas importações de `cadastrarApi`, `loginApi` de `@/lib/authApi`
 *   - `handleSubmit` agora:
 *       1. Chama `cadastrarApi({ nome, email, senha })` → POST /auth/cadastrar
 *       2. Em seguida chama `loginApi(email, senha)` → POST /auth/login (auto-login)
 *       3. Redireciona para /dashboard ao invés de /entrar
 *   - Bloco catch mapeia erros HTTP:
 *       409 → "Este e-mail já está cadastrado."
 *       0   → "Verifique sua conexão."
 *
 * LINHAS REMOVIDAS (comparado ao arquivo anterior):
 *   - import { createUserAndNotify } from '@/lib/auth';
 *   - import { dbSet } from '@/lib/db';
 *   - const { user: newUser } = createUserAndNotify(form.email, form.senha);
 *   - dbSet('proprietarios', { id: newUser.uid, ... });    ← todo o bloco dbSet
 *   - router.push('/entrar');    → substituído por router.push('/dashboard')
 *
 * NOTA: Os campos de endereço (CEP, logradouro, etc.) são mantidos no formulário
 * para coleta de dados, porém o endpoint POST /auth/cadastrar do backend atual
 * recebe apenas { nome, email, senha }. Os dados de endereço poderão ser enviados
 * posteriormente via PATCH /proprietarios/{id} quando o endpoint for expandido.
 */

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { cadastrarApi, loginApi } from '@/lib/authApi';
import { ApiError, getAccessToken } from '@/lib/api';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { AnimatedPlantSVG } from '@/components/icons/AnimatedPlantSVG';
import { fetchCEP, formatCPF, formatTelefone, formatCEP, validateCPF } from '@/lib/validators';
import styles from '../entrar/entrar.module.css';
import authStyles from '../auth.module.css';
import formStyles from './cadastrar.module.css';

export default function CadastrarPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [loadingCEP, setLoadingCEP] = useState(false);

  useEffect(() => {
    if (getAccessToken()) {
      router.replace('/dashboard');
    }
  }, [router]);

  const [form, setForm] = useState({
    nome: '', rg: '', cpf: '', telefone: '', email: '', senha: '', confirmarSenha: '',
    cep: '', logradouro: '', numero: '', complemento: '', bairro: '', municipio: '', uf: '',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  const set = (field: string, value: string) =>
    setForm((prev) => ({ ...prev, [field]: value }));

  const handleCEP = async (raw: string) => {
    const formatted = formatCEP(raw);
    set('cep', formatted);
    const cleaned = raw.replace(/\D/g, '');
    if (cleaned.length === 8) {
      setLoadingCEP(true);
      const data = await fetchCEP(cleaned);
      if (data) {
        setForm((prev) => ({
          ...prev, cep: formatted,
          logradouro: data.logradouro, bairro: data.bairro,
          municipio: data.localidade, uf: data.uf, complemento: data.complemento || '',
        }));
      }
      setLoadingCEP(false);
    }
  };

  const validate = () => {
    const e: Record<string, string> = {};
    if (!form.nome) e.nome = 'Nome é obrigatório';
    if (!form.cpf || !validateCPF(form.cpf)) e.cpf = 'CPF inválido';
    if (!form.email) e.email = 'E-mail é obrigatório';
    if (!form.senha || form.senha.length < 8) e.senha = 'Senha deve ter ao menos 8 caracteres';
    if (!/[A-Z]/.test(form.senha)) e.senha = 'Senha deve ter ao menos uma maiúscula';
    if (!/[0-9]/.test(form.senha)) e.senha = 'Senha deve ter ao menos um número';
    if (form.senha !== form.confirmarSenha) e.confirmarSenha = 'Senhas não coincidem';
    if (!form.cep) e.cep = 'CEP é obrigatório';
    if (!form.logradouro) e.logradouro = 'Logradouro é obrigatório';
    if (!form.numero) e.numero = 'Número é obrigatório';
    if (!form.municipio) e.municipio = 'Município é obrigatório';
    if (!form.uf) e.uf = 'UF é obrigatória';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!validate()) return;
    setLoading(true);
    try {
      // 1. Cria o usuário no backend
      await cadastrarApi({ nome: form.nome, email: form.email, senha: form.senha });
      // 2. Auto-login após cadastro bem-sucedido
      await loginApi(form.email, form.senha);
      router.push('/dashboard');
    } catch (err: unknown) {
      const apiErr = err as ApiError;
      if (apiErr?.status === 409) {
        setError('Este e-mail já está cadastrado.');
      } else if (!apiErr?.status || apiErr?.status === 0) {
        setError('Verifique sua conexão com a internet.');
      } else {
        setError(apiErr?.message ?? 'Erro ao criar conta. Tente novamente.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={authStyles.container}>
      <div className={authStyles.hero}>
        <div className={`${authStyles.blob} ${authStyles.blob1}`}></div>
        <div className={`${authStyles.blob} ${authStyles.blob2}`}></div>
        
        <div className={authStyles.heroHeader}>
          <h1 className={authStyles.heroTitle}>Semente Livre</h1>
          <p className={authStyles.heroSub}>IF Sudeste MG</p>
        </div>

        <div className={authStyles.svgWrapper}>
          <AnimatedPlantSVG />
        </div>
      </div>

      <div className={authStyles.formContainer}>
        <form onSubmit={handleSubmit} className={formStyles.form} noValidate>
          <h2 className={styles.title}>Criar Conta</h2>
          {error && <div className={styles.errorBox} role="alert">⚠ {error}</div>}

          <fieldset className={formStyles.fieldset}>
            <legend className={formStyles.legend}>Dados Pessoais</legend>
            <Input label="Nome completo" value={form.nome} onChange={(e) => set('nome', e.target.value)} error={errors.nome} required />
            <div className={formStyles.row}>
              <Input label="CPF" value={form.cpf} onChange={(e) => set('cpf', formatCPF(e.target.value))} error={errors.cpf} inputMode="numeric" required placeholder="000.000.000-00" />
              <Input label="RG" value={form.rg} onChange={(e) => set('rg', e.target.value)} inputMode="numeric" placeholder="Opcional" />
            </div>
            <Input label="Telefone" value={form.telefone} onChange={(e) => set('telefone', formatTelefone(e.target.value))} inputMode="tel" placeholder="(00) 00000-0000" />
          </fieldset>

          <fieldset className={formStyles.fieldset}>
            <legend className={formStyles.legend}>Acesso</legend>
            <Input label="E-mail" type="email" value={form.email} onChange={(e) => set('email', e.target.value)} error={errors.email} autoComplete="email" required />
            <Input label="Senha" type="password" value={form.senha} onChange={(e) => set('senha', e.target.value)} error={errors.senha} autoComplete="new-password" required />
            <Input label="Confirmar Senha" type="password" value={form.confirmarSenha} onChange={(e) => set('confirmarSenha', e.target.value)} error={errors.confirmarSenha} autoComplete="new-password" required />
          </fieldset>

          <fieldset className={formStyles.fieldset}>
            <legend className={formStyles.legend}>Endereço</legend>
            <Input label="CEP" value={form.cep} onChange={(e) => handleCEP(e.target.value)} error={errors.cep} inputMode="numeric" required placeholder="00000-000" hint={loadingCEP ? 'Buscando CEP...' : undefined} />
            <Input label="Logradouro" value={form.logradouro} onChange={(e) => set('logradouro', e.target.value)} error={errors.logradouro} required />
            <div className={formStyles.row}>
              <Input label="Número" value={form.numero} onChange={(e) => set('numero', e.target.value)} error={errors.numero} required />
              <Input label="Complemento" value={form.complemento} onChange={(e) => set('complemento', e.target.value)} placeholder="Opcional" />
            </div>
            <Input label="Bairro" value={form.bairro} onChange={(e) => set('bairro', e.target.value)} />
            <div className={formStyles.row}>
              <div style={{ flex: 2 }}>
                <Input label="Município" value={form.municipio} onChange={(e) => set('municipio', e.target.value)} error={errors.municipio} required />
              </div>
              <div style={{ flex: 1 }}>
                <Input label="UF" value={form.uf} onChange={(e) => set('uf', e.target.value.toUpperCase().slice(0, 2))} error={errors.uf} required maxLength={2} />
              </div>
            </div>
          </fieldset>

          <Button type="submit" fullWidth loading={loading} size="lg">
            Criar Conta
          </Button>
        </form>

        <p className={styles.registerLink}>
          Já tem conta? <Link href="/entrar">Entrar</Link>
        </p>
      </div>
    </div>
  );
}
