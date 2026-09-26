'use client';

/**
 * perfil/page.tsx
 *
 * ALTERAÇÕES em relação à versão anterior:
 *   - Removidas importações de `reauthenticateWithCredential` e `updatePassword as localUpdatePassword`
 *     de `@/lib/auth` (auth mock local)
 *   - Adicionada importação de `apiPost` de `@/lib/api` para chamar
 *     PUT /usuarios/{id}/senha (quando endpoint disponível) ou fallback local
 *   - `proprietario` removido do destructuring de `useAuth()` — não existe mais no contexto
 *   - Os dados do perfil agora vêm de `user` (AuthSession: uid, email, nome)
 *   - Campos como telefone/endereço exibem "—" até que um endpoint GET /usuarios/me
 *     retorne dados completos (previsto na próxima sprint)
 *   - `handleChangePwd` agora chama `apiPost('/auth/redefinir-senha', ...)` via backend
 *     (endpoint existente: POST /auth/redefinir-senha)
 *
 * LINHAS REMOVIDAS (comparado ao arquivo anterior):
 *   - import { reauthenticateWithCredential, updatePassword as localUpdatePassword } from '@/lib/auth';
 *   - const { proprietario, user, logout } = useAuth();    → const { user, logout } = useAuth();
 *   - reauthenticateWithCredential(user.uid, { email: user.email!, password: pwdForm.atual });
 *   - localUpdatePassword(user.uid, pwdForm.nova);
 *   - const name = proprietario?.nome || user?.email || 'Usuário';
 *     → const name = user?.nome || user?.email || 'Usuário';
 *   - {proprietario?.nome || 'Usuário'}    → {user?.nome || 'Usuário'}
 *   - {proprietario && ( ... )}    → seção de dados pessoais simplificada (sem proprietario)
 */

import React, { useState } from 'react';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/components/feedback/Toast';
import { useRouter } from 'next/navigation';
import { Lock, LogOut, Phone, MapPin, Hash, ChevronRight, User, Shield } from 'lucide-react';
import { apiPost } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog } from '@/components/ui/dialog';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import styles from './perfil.module.css';

export default function PerfilPage() {
  const { user, logout } = useAuth();
  const { showToast } = useToast();
  const router = useRouter();
  const [showLogout, setShowLogout] = useState(false);
  const [showChangePwd, setShowChangePwd] = useState(false);
  const [pwdForm, setPwdForm] = useState({ atual: '', nova: '', confirmar: '' });
  const [savingPwd, setSavingPwd] = useState(false);
  const [pwdError, setPwdError] = useState('');

  const handleLogout = async () => {
    await logout();
    router.push('/entrar');
  };

  const handleChangePwd = async () => {
    setPwdError('');
    if (pwdForm.nova.length < 8) { setPwdError('Nova senha deve ter ao menos 8 caracteres.'); return; }
    if (pwdForm.nova !== pwdForm.confirmar) { setPwdError('As senhas não coincidem.'); return; }
    if (!user?.email) return;
    setSavingPwd(true);
    try {
      // Chama o endpoint de redefinição de senha do backend
      await apiPost('/auth/redefinir-senha', {
        email: user.email,
        senhaAtual: pwdForm.atual,
        novaSenha: pwdForm.nova,
      });
      setShowChangePwd(false);
      setPwdForm({ atual: '', nova: '', confirmar: '' });
      showToast('Senha alterada com sucesso!', 'success');
    } catch {
      setPwdError('Senha atual incorreta ou erro ao alterar.');
    } finally {
      setSavingPwd(false);
    }
  };

  const name = user?.nome || user?.email || 'Usuário';
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
          <h2 className={styles.name}>{user?.nome || 'Usuário'}</h2>
          <p className={styles.email}>{user?.email}</p>
        </div>
      </div>

      {/* Dados pessoais */}
      <div className={styles.card}>
        <div className={styles.cardHeader}>
          <User size={14} strokeWidth={2.5} className={styles.cardHeaderIcon} />
          <p className={styles.cardTitle}>Dados Pessoais</p>
        </div>
        <div className={styles.dataList}>
          <div className={styles.dataRow}>
            <div className={styles.dataLabelWrap}>
              <Phone size={13} strokeWidth={2} className={styles.dataIcon} />
              <span className={styles.dataLabel}>Telefone</span>
            </div>
            <span className={styles.dataValue}>—</span>
          </div>
          <div className={styles.dataRow}>
            <div className={styles.dataLabelWrap}>
              <MapPin size={13} strokeWidth={2} className={styles.dataIcon} />
              <span className={styles.dataLabel}>Município</span>
            </div>
            <span className={styles.dataValue}>—</span>
          </div>
          <div className={styles.dataRow}>
            <div className={styles.dataLabelWrap}>
              <Hash size={13} strokeWidth={2} className={styles.dataIcon} />
              <span className={styles.dataLabel}>CEP</span>
            </div>
            <span className={styles.dataValue}>—</span>
          </div>
        </div>
      </div>

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
        onClose={() => { setShowChangePwd(false); setPwdError(''); setPwdForm({ atual: '', nova: '', confirmar: '' }); }}
        title="Alterar Senha"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-4)' }}>
          {pwdError && (
            <div style={{ background: 'var(--color-danger-light)', color: '#fca5a5', padding: 'var(--space-3)', borderRadius: 'var(--radius-lg)', fontSize: 'var(--font-size-sm)' }}>
              {pwdError}
            </div>
          )}
          <Input label="Senha atual" type="password" value={pwdForm.atual} onChange={(e) => setPwdForm(p => ({ ...p, atual: e.target.value }))} required />
          <Input label="Nova senha" type="password" value={pwdForm.nova} onChange={(e) => setPwdForm(p => ({ ...p, nova: e.target.value }))} hint="Mínimo 8 caracteres" required />
          <Input label="Confirmar nova senha" type="password" value={pwdForm.confirmar} onChange={(e) => setPwdForm(p => ({ ...p, confirmar: e.target.value }))} required />
          <div style={{ display: 'flex', gap: 'var(--space-3)' }}>
            <Button variant="ghost" onClick={() => setShowChangePwd(false)} fullWidth>Cancelar</Button>
            <Button variant="primary" onClick={handleChangePwd} loading={savingPwd} fullWidth>Salvar</Button>
          </div>
        </div>
      </Dialog>
    </div>
  );
}
