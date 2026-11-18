'use client';

/**
 * admin/administradores/page.tsx — Administradores do sistema.
 *
 *   - Lista os admins de GET /admin/administradores (o principal primeiro).
 *   - Só o administrador principal (SUPER_ADMIN) vê o botão de adicionar: o novo
 *     admin entra com uma senha provisória, que ele troca no perfil.
 *   - O backend confere de novo (403 para quem não é SUPER_ADMIN; 409 para
 *     e-mail ou CPF de outra conta).
 */

import React, { useCallback, useEffect, useState } from 'react';
import { AlertCircle, CalendarClock, Mail, ShieldCheck, UserPlus } from 'lucide-react';
import {
  AdminDTO, cadastrarAdministrador, listarAdministradores, NivelAcessoLabels,
} from '@/lib/adminApi';
import { errorMessage, isApiError } from '@/lib/api';
import { formatCPF, validateCPF } from '@/lib/validators';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/components/feedback/Toast';
import { Button } from '@/components/ui/button';
import { Dialog } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Select } from '@/components/ui/select';
import styles from '../comunidades/comunidades.module.css';

const FORM_VAZIO = { nome: '', email: '', cpf: '', senha: '', nivel: 'ADMIN' as 'ADMIN' | 'SUPER_ADMIN' };

function formatarData(iso: string | null): string | null {
  if (!iso) return null;
  const data = new Date(iso);
  return Number.isNaN(data.getTime()) ? null : data.toLocaleDateString('pt-BR');
}

function resumo(total: number): string {
  return total === 1 ? '1 administrador.' : `${total} administradores.`;
}

export default function AdministradoresPage() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const [admins, setAdmins] = useState<AdminDTO[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const [aberto, setAberto] = useState(false);
  const [form, setForm] = useState(FORM_VAZIO);
  const [erros, setErros] = useState<Record<string, string>>({});
  const [enviando, setEnviando] = useState(false);

  const souPrincipal = admins.some((a) => a.id === user?.uid && a.nivelAcesso === 'SUPER_ADMIN');

  const buscar = useCallback(
    (ativo: () => boolean = () => true) =>
      listarAdministradores()
        .then((lista) => {
          if (!ativo()) return;
          setAdmins(lista);
          setErro(null);
        })
        .catch((err) => {
          if (ativo()) setErro(errorMessage(err, 'Não foi possível carregar os administradores.'));
        })
        .finally(() => {
          if (ativo()) setCarregando(false);
        }),
    []
  );

  useEffect(() => {
    let ativo = true;
    void buscar(() => ativo);
    return () => {
      ativo = false;
    };
  }, [buscar]);

  const set = (campo: keyof typeof FORM_VAZIO, valor: string) => setForm((f) => ({ ...f, [campo]: valor }));

  const abrir = () => {
    setForm(FORM_VAZIO);
    setErros({});
    setAberto(true);
  };

  const validar = () => {
    const e: Record<string, string> = {};
    if (!form.nome.trim()) e.nome = 'Informe o nome';
    if (!/^\S+@\S+\.\S+$/.test(form.email.trim())) e.email = 'Informe um e-mail válido';
    if (!validateCPF(form.cpf)) e.cpf = 'CPF inválido';
    if (form.senha.length < 8 || form.senha.length > 72) e.senha = 'A senha provisória deve ter entre 8 e 72 caracteres';
    setErros(e);
    return Object.keys(e).length === 0;
  };

  const salvar = async (ev: React.FormEvent) => {
    ev.preventDefault();
    if (!validar()) return;
    setEnviando(true);
    try {
      const novo = await cadastrarAdministrador({
        nome: form.nome.trim(),
        email: form.email.trim(),
        tipoDocumento: 'CPF',
        documento: form.cpf.replace(/\D/g, ''),
        senha: form.senha,
        nivelAcesso: form.nivel,
      });
      showToast(`${novo.nome} já pode entrar com o e-mail ${novo.email} e a senha provisória.`, 'success');
      setAberto(false);
      setCarregando(true);
      void buscar();
    } catch (err) {
      if (isApiError(err) && err.status === 403) {
        showToast('Só o administrador principal pode adicionar administradores.', 'error');
        setAberto(false);
      } else if (!isApiError(err) || err.status !== 0) {
        // 409 traz "E-mail já cadastrado" / "Documento já cadastrado"; status 0 já vira toast em lib/api.ts
        showToast(errorMessage(err, 'Não foi possível adicionar o administrador.'), 'error');
      }
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.intro}>
        <div className={styles.introIcon} aria-hidden="true">
          <ShieldCheck size={20} strokeWidth={2} />
        </div>
        <div>
          <h2 className={styles.introTitle}>Administradores</h2>
          <p className={styles.introText}>
            {souPrincipal
              ? 'Como administrador principal, você pode adicionar outras pessoas à administração.'
              : 'Só o administrador principal pode adicionar outros administradores.'}
          </p>
        </div>
      </div>

      {souPrincipal && (
        <Button variant="primary" fullWidth onClick={abrir}>
          <UserPlus size={16} strokeWidth={2.5} aria-hidden="true" />
          Adicionar administrador
        </Button>
      )}

      <p className={styles.resumo} aria-live="polite" aria-atomic="true">
        {carregando ? 'Carregando administradores...' : erro ? '' : resumo(admins.length)}
      </p>

      {carregando ? (
        <div className={styles.list} aria-hidden="true">
          {[1, 2].map((i) => <div key={i} className={`skeleton ${styles.skeletonCard}`} />)}
        </div>
      ) : erro ? (
        <div className={styles.erro} role="alert">
          <AlertCircle size={18} strokeWidth={2.5} aria-hidden="true" />
          <p className={styles.erroTexto}>{erro}</p>
          <Button variant="secondary" onClick={() => { setCarregando(true); void buscar(); }}>
            Tentar novamente
          </Button>
        </div>
      ) : (
        <ul className={styles.list} aria-label="Administradores">
          {admins.map((a) => {
            const data = formatarData(a.dataCadastro);
            return (
              <li key={a.id} className={styles.card}>
                <div className={styles.cardInfo}>
                  <p className={styles.cardName}>
                    {a.nome}
                    {a.id === user?.uid && ' (você)'}
                  </p>
                  <p className={styles.cardMeta}>
                    <ShieldCheck size={13} strokeWidth={2} aria-hidden="true" />
                    {NivelAcessoLabels[a.nivelAcesso] ?? a.nivelAcesso}
                  </p>
                  <p className={styles.cardMeta}>
                    <Mail size={13} strokeWidth={2} aria-hidden="true" />
                    {a.email}
                  </p>
                  {data && (
                    <p className={styles.cardMeta}>
                      <CalendarClock size={13} strokeWidth={2} aria-hidden="true" />
                      Desde {data}
                    </p>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}

      <Dialog isOpen={aberto} onClose={() => { if (!enviando) setAberto(false); }} title="Adicionar administrador">
        <form onSubmit={salvar} noValidate style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          <Input label="Nome" value={form.nome} onChange={(e) => set('nome', e.target.value)} error={erros.nome} required />
          <Input label="E-mail" type="email" value={form.email} onChange={(e) => set('email', e.target.value)} error={erros.email} autoComplete="off" required />
          <Input label="CPF" value={form.cpf} onChange={(e) => set('cpf', formatCPF(e.target.value))} error={erros.cpf} inputMode="numeric" placeholder="000.000.000-00" required />
          <Input
            label="Senha provisória"
            type="password"
            value={form.senha}
            onChange={(e) => set('senha', e.target.value)}
            error={erros.senha}
            hint="Passe para a pessoa; ela troca no perfil depois de entrar."
            autoComplete="new-password"
            required
          />
          <Select
            label="Permissão"
            value={form.nivel}
            onChange={(e) => set('nivel', e.target.value)}
            options={[
              { value: 'ADMIN', label: 'Administrador' },
              { value: 'SUPER_ADMIN', label: 'Administrador principal (também adiciona administradores)' },
            ]}
          />
          <div style={{ display: 'flex', gap: 'var(--space-3)' }}>
            <Button type="button" variant="ghost" fullWidth onClick={() => setAberto(false)} disabled={enviando}>
              Cancelar
            </Button>
            <Button type="submit" variant="primary" fullWidth loading={enviando}>
              Adicionar
            </Button>
          </div>
        </form>
      </Dialog>
    </div>
  );
}
