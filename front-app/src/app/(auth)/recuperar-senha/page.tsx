'use client';

/**
 * recuperar-senha/page.tsx — Tela de recuperação de senha integrada ao backend REST.
 *
 * ALTERAÇÕES em relação à versão anterior:
 *   - Removida importação de `sendPasswordResetEmail` de `@/lib/auth` (era no-op local)
 *   - Adicionada importação de `recuperarSenhaApi` de `@/lib/authApi`
 *   - `handleSubmit` agora chama `recuperarSenhaApi(email)` → POST /auth/recuperar-senha
 *   - O backend retorna 200 independente de o e-mail existir (segurança),
 *     então a lógica de "setSent(true)" no catch é mantida igual ao original
 *
 * LINHAS REMOVIDAS (comparado ao arquivo anterior):
 *   - import { sendPasswordResetEmail } from '@/lib/auth';
 *   - await sendPasswordResetEmail(email);    → substituído por recuperarSenhaApi(email)
 *   - import authStyles from '../auth.module.css';    (não era usado nesta tela)
 */

import React, { useState } from 'react';
import Link from 'next/link';
import { Mail, KeyRound, AlertCircle } from 'lucide-react';
import { recuperarSenhaApi } from '@/lib/authApi';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import styles from '../entrar/entrar.module.css';
import localStyles from './recuperar.module.css';

export default function RecuperarSenhaPage() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await recuperarSenhaApi(email);
      setSent(true);
    } catch {
      // Não revelamos se o e-mail existe ou não (segurança)
      setSent(true);
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <div className={localStyles.sent}>
        <div className={localStyles.sentIcon}>
          <Mail size={36} strokeWidth={1.5} />
        </div>
        <h1 className={localStyles.sentTitle}>E-mail enviado!</h1>
        <p className={localStyles.sentDesc}>
          Se esse e-mail estiver cadastrado, você receberá um link para redefinir sua senha. Verifique sua caixa de entrada.
        </p>
        <Link href="/entrar">
          <Button variant="primary" fullWidth>Voltar para o Login</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.logo}>
        <div className={styles.logoIcon}>
          <KeyRound size={32} strokeWidth={2.5} />
        </div>
        <h1 className={styles.logoText}>Recuperar Senha</h1>
      </div>

      <form onSubmit={handleSubmit} className={styles.form} noValidate>
        <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--color-text-muted)', lineHeight: 1.6 }}>
          Digite seu e-mail e enviaremos um link para criar uma nova senha.
        </p>

        {error && (
          <div className={styles.errorBox} role="alert">
            <AlertCircle size={16} strokeWidth={2} />
            {error}
          </div>
        )}

        <Input
          label="E-mail cadastrado"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          autoComplete="email"
          inputMode="email"
          required
          placeholder="seu@email.com"
        />

        <Button type="submit" fullWidth loading={loading} size="lg">
          Enviar link de recuperação
        </Button>
      </form>

      <p className={styles.registerLink}>
        <Link href="/entrar">← Voltar para o login</Link>
      </p>
    </div>
  );
}
