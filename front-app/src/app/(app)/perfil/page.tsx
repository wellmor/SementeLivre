'use client';

import React, { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/components/feedback/Toast';
import { useRouter } from 'next/navigation';
import { Lock, LogOut, Phone, MapPin, Hash, ChevronRight, User, Shield, Mail, Pencil } from 'lucide-react';
import { alterarSenha, atualizarPerfil } from '@/lib/auth';
import { ApiError } from '@/lib/api';
import {
  alterarSenhaSchema, campoDoFormulario, fetchCEP, formatCEP, formatCPF, formatTelefone,
  paraPessoaRequest, PerfilForm, perfilSchema, validarFormulario,
} from '@/lib/validators';
import { PerfilUsuario } from '@/types/user';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog } from '@/components/ui/dialog';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import styles from './perfil.module.css';

const formStack: React.CSSProperties = { display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' };
const formRow: React.CSSProperties = { display: 'flex', gap: 'var(--space-3)' };
const alertBox: React.CSSProperties = { background: 'var(--color-danger-light)', color: '#fca5a5', padding: 'var(--space-3)', borderRadius: 'var(--radius-lg)', fontSize: 'var(--font-size-sm)' };

function perfilParaForm(p: PerfilUsuario): PerfilForm {
  const e = p.endereco;
  return {
    nome: p.nome,
    telefone: formatTelefone(p.telefone ?? ''),
    email: p.email,
    cep: formatCEP(e?.cep ?? ''),
    logradouro: e?.logradouro ?? '',
    numero: e?.numero ?? '',
    complemento: e?.complemento ?? '',
    bairro: e?.bairro ?? '',
    municipio: e?.municipio ?? '',
    uf: e?.uf ?? '',
  };
}

/** Separa os erros de campo da API (que existem no formulário) da mensagem geral. */
function errosDaApi(err: unknown, campos: string[]): { campos: Record<string, string>; geral: string } {
  if (!(err instanceof ApiError)) return { campos: {}, geral: 'Ocorreu um erro inesperado. Tente novamente.' };
  const todos = Object.entries(err.fieldErrors).map(([c, msg]) => [campoDoFormulario(c), msg] as const);
  const conhecidos = todos.filter(([c]) => campos.includes(c));
  return {
    campos: Object.fromEntries(conhecidos),
    geral: conhecidos.length === todos.length && conhecidos.length > 0 ? '' : err.message,
  };
}

export default function PerfilPage() {
  const { proprietario, loading, erroPerfil, recarregarPerfil, logout } = useAuth();
  const { showToast } = useToast();
  const router = useRouter();
  const [showLogout, setShowLogout] = useState(false);
  const [showChangePwd, setShowChangePwd] = useState(false);
  const [pwdForm, setPwdForm] = useState({ atual: '', nova: '', confirmar: '' });
  const [savingPwd, setSavingPwd] = useState(false);
  const [pwdError, setPwdError] = useState('');
  const [pwdErrors, setPwdErrors] = useState<Record<string, string>>({});

  const [editando, setEditando] = useState(false);
  const [form, setForm] = useState<PerfilForm | null>(null);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState('');
  const [salvando, setSalvando] = useState(false);

  const setCampo = (field: keyof PerfilForm, value: string) =>
    setForm((prev) => (prev ? { ...prev, [field]: value } : prev));

  const iniciarEdicao = () => {
    if (!proprietario) return;
    setForm(perfilParaForm(proprietario));
    setFormErrors({});
    setFormError('');
    setEditando(true);
  };

  const handleCEP = async (raw: string) => {
    const formatted = formatCEP(raw);
    setCampo('cep', formatted);
    const cleaned = raw.replace(/\D/g, '');
    if (cleaned.length === 8) {
      const data = await fetchCEP(cleaned);
      if (data) {
        setForm((prev) => prev && ({
          ...prev, cep: formatted,
          logradouro: data.logradouro, bairro: data.bairro,
          municipio: data.localidade, uf: data.uf, complemento: data.complemento || '',
        }));
      }
    }
  };

  const handleSalvarPerfil = async () => {
    if (!form || !proprietario) return;
    setFormError('');
    const erros = validarFormulario(perfilSchema, form);
    setFormErrors(erros);
    if (Object.keys(erros).length > 0) return;
    setSalvando(true);
    try {
      await atualizarPerfil(proprietario.id, paraPessoaRequest(form));
      // Se o e-mail mudou, o token antigo deixa de valer; o cliente da API renova a sessão sozinho.
      await recarregarPerfil();
      setEditando(false);
      showToast('Perfil atualizado com sucesso!', 'success');
    } catch (err) {
      const { campos, geral } = errosDaApi(err, Object.keys(form));
      setFormErrors(campos);
      setFormError(geral);
    } finally {
      setSalvando(false);
    }
  };

  const handleLogout = async () => {
    await logout();
    router.push('/entrar');
  };

  const handleChangePwd = async () => {
    setPwdError('');
    const erros = validarFormulario(alterarSenhaSchema, pwdForm);
    setPwdErrors(erros);
    if (Object.keys(erros).length > 0) return;
    setSavingPwd(true);
    try {
      await alterarSenha(pwdForm.atual, pwdForm.nova);
      setShowChangePwd(false);
      setPwdForm({ atual: '', nova: '', confirmar: '' });
      showToast('Senha alterada com sucesso!', 'success');
    } catch (err) {
      const apiErr = err instanceof ApiError ? err : null;
      const campos: Record<string, string> = {};
      if (apiErr?.fieldErrors.senhaAtual) campos.atual = apiErr.fieldErrors.senhaAtual;
      if (apiErr?.fieldErrors.novaSenha) campos.nova = apiErr.fieldErrors.novaSenha;
      setPwdErrors(campos);
      if (Object.keys(campos).length === 0) setPwdError(apiErr?.message ?? 'Erro ao alterar a senha.');
    }
    finally { setSavingPwd(false); }
  };

  const fecharSenha = () => {
    setShowChangePwd(false);
    setPwdError('');
    setPwdErrors({});
    setPwdForm({ atual: '', nova: '', confirmar: '' });
  };

  if (loading) {
    return <div className={styles.page}><p className={styles.about}>Carregando perfil...</p></div>;
  }

  if (!proprietario) {
    return (
      <div className={styles.page}>
        <div className={styles.card}>
          <div style={formStack}>
            <div style={alertBox} role="alert">{erroPerfil || 'Não foi possível carregar o perfil.'}</div>
            <Button variant="primary" onClick={recarregarPerfil} fullWidth>Tentar novamente</Button>
          </div>
        </div>
      </div>
    );
  }

  const endereco = proprietario.endereco;
  const name = proprietario.nome || proprietario.email;
  const initial = name.charAt(0).toUpperCase();

  return (
    <div className={styles.page}>
      {/* Avatar section */}
      <div className={styles.avatarSection}>
        <div className={styles.avatarRing} aria-label={`Foto de perfil — inicial ${initial}`}>
          <div className={styles.avatar}>
            <span>{initial}</span>
          </div>
        </div>
        <div className={styles.avatarInfo}>
          <h2 className={styles.name}>{proprietario.nome}</h2>
          <p className={styles.email}>{proprietario.email}</p>
        </div>
      </div>

      {/* Dados pessoais */}
      {!editando && (
        <>
          <div className={styles.card}>
            <div className={styles.cardHeader}>
              <User size={14} strokeWidth={2.5} className={styles.cardHeaderIcon} />
              <p className={styles.cardTitle}>Dados Pessoais</p>
              <Button variant="text" size="sm" onClick={iniciarEdicao} style={{ marginLeft: 'auto' }}>
                <Pencil size={13} strokeWidth={2} /> Editar
              </Button>
            </div>
            <div className={styles.dataList}>
              <div className={styles.dataRow}>
                <div className={styles.dataLabelWrap}>
                  <Hash size={13} strokeWidth={2} className={styles.dataIcon} />
                  <span className={styles.dataLabel}>CPF</span>
                </div>
                <span className={styles.dataValue}>{formatCPF(proprietario.documento)}</span>
              </div>
              <div className={styles.dataRow}>
                <div className={styles.dataLabelWrap}>
                  <Hash size={13} strokeWidth={2} className={styles.dataIcon} />
                  <span className={styles.dataLabel}>RG</span>
                </div>
                <span className={styles.dataValue}>{proprietario.rg || '—'}</span>
              </div>
              <div className={styles.dataRow}>
                <div className={styles.dataLabelWrap}>
                  <Phone size={13} strokeWidth={2} className={styles.dataIcon} />
                  <span className={styles.dataLabel}>Telefone</span>
                </div>
                <span className={styles.dataValue}>{proprietario.telefone ? formatTelefone(proprietario.telefone) : '—'}</span>
              </div>
              <div className={styles.dataRow}>
                <div className={styles.dataLabelWrap}>
                  <Mail size={13} strokeWidth={2} className={styles.dataIcon} />
                  <span className={styles.dataLabel}>E-mail</span>
                </div>
                <span className={styles.dataValue}>{proprietario.email}</span>
              </div>
            </div>
          </div>

          <div className={styles.card}>
            <div className={styles.cardHeader}>
              <MapPin size={14} strokeWidth={2.5} className={styles.cardHeaderIcon} />
              <p className={styles.cardTitle}>Endereço</p>
            </div>
            <div className={styles.dataList}>
              <div className={styles.dataRow}>
                <div className={styles.dataLabelWrap}>
                  <MapPin size={13} strokeWidth={2} className={styles.dataIcon} />
                  <span className={styles.dataLabel}>Logradouro</span>
                </div>
                <span className={styles.dataValue}>
                  {endereco ? [endereco.logradouro, endereco.numero, endereco.complemento].filter(Boolean).join(', ') : '—'}
                </span>
              </div>
              <div className={styles.dataRow}>
                <div className={styles.dataLabelWrap}>
                  <MapPin size={13} strokeWidth={2} className={styles.dataIcon} />
                  <span className={styles.dataLabel}>Bairro</span>
                </div>
                <span className={styles.dataValue}>{endereco?.bairro || '—'}</span>
              </div>
              <div className={styles.dataRow}>
                <div className={styles.dataLabelWrap}>
                  <MapPin size={13} strokeWidth={2} className={styles.dataIcon} />
                  <span className={styles.dataLabel}>Município</span>
                </div>
                <span className={styles.dataValue}>{endereco?.municipio || '—'}/{endereco?.uf || '—'}</span>
              </div>
              <div className={styles.dataRow}>
                <div className={styles.dataLabelWrap}>
                  <Hash size={13} strokeWidth={2} className={styles.dataIcon} />
                  <span className={styles.dataLabel}>CEP</span>
                </div>
                <span className={styles.dataValue}>{endereco?.cep ? formatCEP(endereco.cep) : '—'}</span>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Editar dados pessoais e endereço */}
      {editando && form && (
        <div className={styles.card}>
          <div className={styles.cardHeader}>
            <Pencil size={14} strokeWidth={2.5} className={styles.cardHeaderIcon} />
            <p className={styles.cardTitle}>Editar Dados</p>
          </div>
          <div style={formStack}>
            {formError && <div style={alertBox} role="alert">{formError}</div>}
            <Input label="Nome completo" value={form.nome} onChange={(e) => setCampo('nome', e.target.value)} error={formErrors.nome} required />
            <Input label="Telefone" value={form.telefone} onChange={(e) => setCampo('telefone', formatTelefone(e.target.value))} error={formErrors.telefone} inputMode="tel" placeholder="(00) 00000-0000" />
            <Input label="E-mail" type="email" value={form.email} onChange={(e) => setCampo('email', e.target.value)} error={formErrors.email} autoComplete="email" required />
            <Input label="CEP" value={form.cep} onChange={(e) => handleCEP(e.target.value)} error={formErrors.cep} inputMode="numeric" placeholder="00000-000" />
            <Input label="Logradouro" value={form.logradouro} onChange={(e) => setCampo('logradouro', e.target.value)} error={formErrors.logradouro} required />
            <div style={formRow}>
              <Input label="Número" value={form.numero} onChange={(e) => setCampo('numero', e.target.value)} error={formErrors.numero} maxLength={10} />
              <Input label="Complemento" value={form.complemento} onChange={(e) => setCampo('complemento', e.target.value)} error={formErrors.complemento} placeholder="Opcional" />
            </div>
            <Input label="Bairro" value={form.bairro} onChange={(e) => setCampo('bairro', e.target.value)} error={formErrors.bairro} />
            <div style={formRow}>
              <div style={{ flex: 2 }}>
                <Input label="Município" value={form.municipio} onChange={(e) => setCampo('municipio', e.target.value)} error={formErrors.municipio} required />
              </div>
              <div style={{ flex: 1 }}>
                <Input label="UF" value={form.uf} onChange={(e) => setCampo('uf', e.target.value.toUpperCase().slice(0, 2))} error={formErrors.uf} required maxLength={2} />
              </div>
            </div>
            <div style={formRow}>
              <Button variant="ghost" onClick={() => setEditando(false)} fullWidth disabled={salvando}>Cancelar</Button>
              <Button variant="primary" onClick={handleSalvarPerfil} loading={salvando} fullWidth>Salvar</Button>
            </div>
          </div>
        </div>
      )}

      {/* Segurança */}
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <Shield size={14} strokeWidth={2.5} className={styles.cardHeaderIcon} />
          <p className={styles.cardTitle}>Segurança</p>
        </div>
        <button className={styles.actionRow} onClick={() => setShowChangePwd(true)}>
          <div className={styles.actionIcon}>
            <Lock size={15} strokeWidth={2} />
          </div>
          <span className={styles.actionLabel}>Alterar Senha</span>
          <ChevronRight size={15} className={styles.actionChevron} />
        </button>
      </div>

      {/* Sobre */}
      <div className={styles.card}>
        <p className={styles.about}>Semente Livre v1.0.0</p>
        <p className={styles.about}>IF Sudeste MG — Campus Rio Pomba</p>
        <p className={styles.about}>Gestão de bancos de produtos crioulos para produtores rurais familiares.</p>
      </div>

      {/* Logout */}
      <button className={styles.logoutBtn} onClick={() => setShowLogout(true)}>
        <LogOut size={17} strokeWidth={2} />
        Sair do Aplicativo
      </button>

      {/* Logout confirm */}
      <ConfirmDialog
        isOpen={showLogout}
        title="Sair do aplicativo"
        description="Tem certeza que deseja sair?"
        confirmLabel="Sair"
        confirmVariant="danger"
        onConfirm={handleLogout}
        onCancel={() => setShowLogout(false)}
      />

      {/* Change password dialog */}
      <Dialog
        isOpen={showChangePwd}
        onClose={fecharSenha}
        title="Alterar Senha"
      >
        <div style={formStack}>
          {pwdError && (
            <div style={alertBox} role="alert">
              {pwdError}
            </div>
          )}
          <Input label="Senha atual" type="password" value={pwdForm.atual} onChange={(e) => setPwdForm(p => ({ ...p, atual: e.target.value }))} error={pwdErrors.atual} autoComplete="current-password" required />
          <Input label="Nova senha" type="password" value={pwdForm.nova} onChange={(e) => setPwdForm(p => ({ ...p, nova: e.target.value }))} error={pwdErrors.nova} hint="Mínimo 8 caracteres, com uma letra maiúscula e um número" autoComplete="new-password" required />
          <Input label="Confirmar nova senha" type="password" value={pwdForm.confirmar} onChange={(e) => setPwdForm(p => ({ ...p, confirmar: e.target.value }))} error={pwdErrors.confirmar} autoComplete="new-password" required />
          <div style={formRow}>
            <Button variant="ghost" onClick={fecharSenha} fullWidth>Cancelar</Button>
            <Button variant="primary" onClick={handleChangePwd} loading={savingPwd} fullWidth>Salvar</Button>
          </div>
        </div>
      </Dialog>
    </div>
  );
}
