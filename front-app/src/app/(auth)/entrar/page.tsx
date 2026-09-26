'use client';

/**
 * entrar/page.tsx — Tela de login integrada ao backend REST.
 *
 * ALTERAÇÕES em relação à versão anterior:
 *   - Removida importação de `signInAndNotify` de `@/lib/auth` (auth mock local)
 *   - Adicionada importação de `loginApi` de `@/lib/authApi`
 *   - `handleSubmit` agora chama `loginApi(email, senha)` (assíncrono real)
 *   - O bloco catch mapeia os status HTTP do backend para mensagens amigáveis:
 *       401 → "E-mail ou senha incorretos."
 *       0 / NetworkError → "Verifique sua conexão com a internet."
 *   - Importações removidas: `signInAndNotify` de `@/lib/auth`
 *
 * LINHAS REMOVIDAS (comparado ao arquivo anterior):
 *   - import { signInAndNotify } from '@/lib/auth';
 *   - signInAndNotify(email, senha);
 */

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Eye, EyeOff, AlertCircle } from 'lucide-react';
import { loginApi } from '@/lib/authApi';
import { ApiError, getAccessToken } from '@/lib/api';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { AnimatedSeedsSVG } from '@/components/icons/AnimatedSeedsSVG';
import styles from './entrar.module.css';
import authStyles from '../auth.module.css';

export default function EntrarPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [showSenha, setShowSenha] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (getAccessToken()) {
      router.replace('/dashboard');
    }
  }, [router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!email || !senha) { setError('Preencha todos os campos.'); return; }
    setLoading(true);
    try {
      await loginApi(email, senha);
      router.push('/dashboard');
    } catch (err: unknown) {
      const apiErr = err as ApiError;
      if (apiErr?.status === 401 || apiErr?.status === 403) {
        setError('E-mail ou senha incorretos.');
      } else if (!apiErr?.status || apiErr?.status === 0) {
        setError('Verifique sua conexão com a internet.');
      } else {
        setError('Ocorreu um erro. Tente novamente.');
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
          <AnimatedSeedsSVG />
        </div>
      </div>

      <div className={authStyles.formContainer}>
        <form onSubmit={handleSubmit} className={styles.form} noValidate>
          <h2 className={styles.title}>Entrar na conta</h2>

          {error && (
            <div className={styles.errorBox} role="alert" aria-live="assertive">
              <AlertCircle size={15} strokeWidth={2.5} />
              {error}
            </div>
          )}

          <Input
            label="E-mail"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
            inputMode="email"
            required
            placeholder="seu@email.com"
          />

          <Input
            label="Senha"
            type={showSenha ? 'text' : 'password'}
            value={senha}
            onChange={(e) => setSenha(e.target.value)}
            autoComplete="current-password"
            required
            placeholder="Sua senha"
            rightIcon={showSenha ? <EyeOff size={17} strokeWidth={2} /> : <Eye size={17} strokeWidth={2} />}
            onRightIconClick={() => setShowSenha(!showSenha)}
          />

          <Link href="/recuperar-senha" className={styles.forgotLink}>
            Esqueceu sua senha?
          </Link>

          <Button type="submit" fullWidth loading={loading} size="lg">
            Entrar
          </Button>
        </form>

        <p className={styles.registerLink}>
          Ainda não tem conta?{' '}
          <Link href="/cadastrar">Cadastre-se</Link>
        </p>
      </div>
    </div>
  );
}
