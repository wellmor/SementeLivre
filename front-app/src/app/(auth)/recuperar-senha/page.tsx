'use client';

/**
 * recuperar-senha/page.tsx — Recuperação de senha integrada ao backend REST.
 *
 *   1. POST /auth/recuperar-senha com o e-mail. O backend responde 200 exista ou não o e-mail
 *      (não revela contas) e manda por e-mail um código válido por 15 minutos.
 *   2. Com o código, POST /auth/redefinir-senha grava a nova senha e volta para o login.
 */

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import Link from 'next/link';
import { Mail, KeyRound, AlertCircle } from 'lucide-react';
import { recuperarSenhaApi, redefinirSenhaApi } from '@/lib/authApi';
import { isApiError } from '@/lib/api';
import { aplicarErrosDaApi } from '@/lib/forms';
import { RedefinirSenhaForm, redefinirSenhaSchema } from '@/lib/validators';
import { useToast } from '@/components/feedback/Toast';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import styles from '../entrar/entrar.module.css';
import localStyles from './recuperar.module.css';

export default function RecuperarSenhaPage() {
  const router = useRouter();
  const { showToast } = useToast();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [error, setError] = useState('');

  const redefinir = useForm<RedefinirSenhaForm>({
    resolver: zodResolver(redefinirSenhaSchema),
    defaultValues: { token: '', nova: '', confirmar: '' },
  });
  const errosRedefinir = redefinir.formState.errors;

  const handleRedefinir = async (form: RedefinirSenhaForm) => {
    try {
      await redefinirSenhaApi(form.token.trim(), form.nova);
      showToast('Senha redefinida! Entre com a nova senha.', 'success');
      router.push('/entrar');
    } catch (err) {
      const geral = aplicarErrosDaApi(err, ['token', 'nova', 'confirmar'], redefinir.setError, {
        token: 'token',
        novaSenha: 'nova',
      });
      // Código inválido, expirado ou já usado: o backend responde 400 sem campo; o erro é do código.
      if (geral) redefinir.setError('token', { type: 'server', message: geral });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await recuperarSenhaApi(email.trim());
      setSent(true);
    } catch (err) {
      // Não revelamos se o e-mail existe ou não (segurança). Sem conexão já virou toast.
      if (!isApiError(err) || err.status !== 0) setSent(true);
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
          Se esse e-mail estiver cadastrado, você receberá um código para redefinir sua senha. Verifique sua caixa de entrada.
        </p>

        <form onSubmit={redefinir.handleSubmit(handleRedefinir)} className={styles.form} noValidate style={{ width: '100%', textAlign: 'left' }}>
          <Input label="Código recebido por e-mail" {...redefinir.register('token')} error={errosRedefinir.token?.message} autoComplete="one-time-code" required />
          <Input label="Nova senha" type="password" {...redefinir.register('nova')} error={errosRedefinir.nova?.message} hint="Mínimo 8 caracteres, com uma letra maiúscula e um número" autoComplete="new-password" required />
          <Input label="Confirmar nova senha" type="password" {...redefinir.register('confirmar')} error={errosRedefinir.confirmar?.message} autoComplete="new-password" required />
          <Button type="submit" fullWidth loading={redefinir.formState.isSubmitting}>Redefinir senha</Button>
        </form>

        <Link href="/entrar">
          <Button variant="ghost" fullWidth>Voltar para o Login</Button>
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
          Digite seu e-mail e enviaremos um código para criar uma nova senha.
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
          Enviar código de recuperação
        </Button>
      </form>

      <p className={styles.registerLink}>
        <Link href="/entrar">← Voltar para o login</Link>
      </p>
    </div>
  );
}
