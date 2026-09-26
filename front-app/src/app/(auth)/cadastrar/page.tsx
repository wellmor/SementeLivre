'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { cadastrarProprietario } from '@/lib/auth';
import { ApiError } from '@/lib/api';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { AnimatedPlantSVG } from '@/components/icons/AnimatedPlantSVG';
import {
  cadastroProprietarioSchema, campoDoFormulario, fetchCEP, formatCPF, formatTelefone, formatCEP,
  paraPessoaRequest, validarFormulario,
} from '@/lib/validators';
import styles from '../entrar/entrar.module.css';
import authStyles from '../auth.module.css';
import formStyles from './cadastrar.module.css';

export default function CadastrarPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [loadingCEP, setLoadingCEP] = useState(false);

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
    const e = validarFormulario(cadastroProprietarioSchema, form);
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!validate()) return;
    setLoading(true);
    try {
      await cadastrarProprietario({
        ...paraPessoaRequest(form),
        tipoDocumento: 'CPF',
        documento: form.cpf.replace(/\D/g, ''),
        rg: form.rg.trim(),
        senha: form.senha,
      });
      router.push('/entrar?cadastro=ok');
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        const camposComErro = Object.entries(err.fieldErrors).map(([campo, msg]) => [campoDoFormulario(campo), msg]);
        const doFormulario = camposComErro.filter(([campo]) => campo in form);
        if (doFormulario.length > 0) {
          setErrors(Object.fromEntries(doFormulario));
          if (doFormulario.length < camposComErro.length) setError(err.message);
        } else {
          setError(err.message);
        }
      } else {
        setError('Erro ao criar conta. Tente novamente.');
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
              <Input label="RG" value={form.rg} onChange={(e) => set('rg', e.target.value)} error={errors.rg} required maxLength={20} />
            </div>
            <Input label="Telefone" value={form.telefone} onChange={(e) => set('telefone', formatTelefone(e.target.value))} error={errors.telefone} inputMode="tel" placeholder="(00) 00000-0000" />
          </fieldset>

          <fieldset className={formStyles.fieldset}>
            <legend className={formStyles.legend}>Acesso</legend>
            <Input label="E-mail" type="email" value={form.email} onChange={(e) => set('email', e.target.value)} error={errors.email} autoComplete="email" required />
            <Input label="Senha" type="password" value={form.senha} onChange={(e) => set('senha', e.target.value)} error={errors.senha} autoComplete="new-password" required />
            <Input label="Confirmar Senha" type="password" value={form.confirmarSenha} onChange={(e) => set('confirmarSenha', e.target.value)} error={errors.confirmarSenha} autoComplete="new-password" required />
          </fieldset>

          <fieldset className={formStyles.fieldset}>
            <legend className={formStyles.legend}>Endereço</legend>
            <Input label="CEP" value={form.cep} onChange={(e) => handleCEP(e.target.value)} error={errors.cep} inputMode="numeric" placeholder="00000-000" hint={loadingCEP ? 'Buscando CEP...' : undefined} />
            <Input label="Logradouro" value={form.logradouro} onChange={(e) => set('logradouro', e.target.value)} error={errors.logradouro} required />
            <div className={formStyles.row}>
              <Input label="Número" value={form.numero} onChange={(e) => set('numero', e.target.value)} error={errors.numero} maxLength={10} />
              <Input label="Complemento" value={form.complemento} onChange={(e) => set('complemento', e.target.value)} error={errors.complemento} placeholder="Opcional" />
            </div>
            <Input label="Bairro" value={form.bairro} onChange={(e) => set('bairro', e.target.value)} error={errors.bairro} />
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
