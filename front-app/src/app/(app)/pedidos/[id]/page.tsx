'use client';

import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useOrders } from '@/hooks/useOrders';
import { useOffline } from '@/hooks/useOffline';
import { useToast } from '@/components/feedback/Toast';
import { useNotifications } from '@/context/NotificationContext';
import { errorMessage } from '@/lib/api';
import { Pedido, StatusPedido } from '@/types/order';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { EmptyState } from '@/components/feedback/EmptyState';
import styles from './pedido.module.css';
import { formatarMoeda } from '@/lib/formato';
import { AlterarPedido } from './AlterarPedido';

const BOTAO_ALTERAR_ID = 'botao-alterar-pedido';
const AVISO_ALTERAR_OFFLINE_ID = 'aviso-alterar-pedido-offline';

export default function PedidoDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { getOrder, cancelOrder, confirmOrder } = useOrders();
  const { showToast } = useToast();
  const { refresh: refreshNotifications } = useNotifications();
  const { isOffline } = useOffline();

  const [order, setOrder] = useState<Pedido | null>(null);
  const [loading, setLoading] = useState(true);
  const [showCancel, setShowCancel] = useState(false);
  const [acting, setActing] = useState(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [editando, setEditando] = useState(false);
  // Ao sair do formulário de alteração, o foco volta para o botão que o abriu
  const devolverFoco = useRef(false);

  const carregar = useCallback(() => {
    return getOrder(id)
      .then((o) => { setOrder(o); setLoadError(null); })
      .catch((err) => setLoadError(errorMessage(err, 'Erro ao carregar pedido.')))
      .finally(() => setLoading(false));
  }, [id, getOrder]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  useEffect(() => {
    if (editando || !devolverFoco.current) return;
    devolverFoco.current = false;
    document.getElementById(BOTAO_ALTERAR_ID)?.focus();
  }, [editando]);

  if (loading) return <div className="skeleton" style={{ height: 300, borderRadius: 16 }} />;
  if (loadError) return <EmptyState icon="⚠️" title={loadError} actionLabel="Voltar" onAction={() => router.push('/pedidos')} />;
  if (!order) return <EmptyState icon="📦" title="Pedido não encontrado" actionLabel="Voltar" onAction={() => router.push('/pedidos')} />;

  const fecharEdicao = () => {
    devolverFoco.current = true;
    setEditando(false);
  };

  if (editando) {
    return (
      <AlterarPedido
        pedido={order}
        // A resposta do PUT já é o pedido salvo: o detalhe mostra os dados novos
        onSalvo={(atualizado) => { setOrder(atualizado); fecharEdicao(); }}
        onCancelar={fecharEdicao}
        onDesatualizado={() => { setEditando(false); void carregar(); }}
      />
    );
  }

  // O status exibido vem sempre da resposta do backend, nunca de um palpite local
  const handleCancel = async () => {
    setActing(true);
    try {
      setOrder(await cancelOrder(id));
      showToast('Pedido cancelado e retirado da fila.', 'success');
      setShowCancel(false);
      // O cancelamento tambem gera notificacao no backend (CDU-26)
      refreshNotifications();
    } catch (err) { showToast(errorMessage(err, 'Erro ao cancelar.'), 'error'); }
    finally { setActing(false); }
  };

  const handleConfirm = async () => {
    setActing(true);
    try {
      setOrder(await confirmOrder(id));
      showToast('Pedido confirmado! O estoque foi atualizado.', 'success');
      // A confirmacao gera uma notificacao no backend (CDU-26)
      refreshNotifications();
    } catch (err) { showToast(errorMessage(err, 'Erro ao confirmar.'), 'error'); }
    finally { setActing(false); }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.header}>
          <span className={styles.id}>#{order.idPedido.slice(-6).toUpperCase()}</span>
          <Badge variant="orderStatus" value={order.status} />
        </div>
        <div className={styles.row}><span className={styles.label}>Tipo:</span><Badge variant="orderType" value={order.tipoPedido} /></div>
        <div className={styles.row}><span className={styles.label}>Data:</span><span>{order.dataPedido.toLocaleDateString('pt-BR', { dateStyle: 'long' })}</span></div>
        <div className={styles.row}><span className={styles.label}>Recebedor:</span><span className={styles.value}>{order.nomeRecebedor}</span></div>
        {order.contatoRecebedor && <div className={styles.row}><span className={styles.label}>Contato:</span><span>{order.contatoRecebedor}</span></div>}
      </div>

      <div className={styles.card}>
        <p className={styles.sectionTitle}>Itens do Pedido</p>
        <ul className={styles.itensList}>
          {order.itens.map((item) => (
            <li key={item.idItem} className={styles.item}>
              <span className={styles.itemName}>{item.nomePopular}</span>
              <span className={styles.itemQty}>{item.quantidade}{item.tipoPesagem ? ` ${item.tipoPesagem}` : ''}</span>
              {item.precoUnitario && <span className={styles.itemPrice}>{formatarMoeda(item.precoUnitario * item.quantidade)}</span>}
            </li>
          ))}
        </ul>
        {!!order.totalValor && (
          <p className={styles.total}>Total: <strong>{formatarMoeda(order.totalValor)}</strong></p>
        )}
      </div>

      {order.mensagemOpcional && (
        <div className={styles.card}>
          <p className={styles.sectionTitle}>Observações</p>
          <p className={styles.obs}>{order.mensagemOpcional}</p>
        </div>
      )}

      {order.status === StatusPedido.PENDENTE && (
        <>
          <p className={styles.obs} role="note">
            Pedido na fila: o estoque só é baixado quando você confirmar. Pedidos que não couberem
            no que sobrar são recusados e quem pediu é avisado.
          </p>
          {/* CDU-13: só o pedido na fila pode ser alterado; o confirmado já baixou o estoque */}
          <Button
            id={BOTAO_ALTERAR_ID}
            variant="secondary"
            fullWidth
            onClick={() => setEditando(true)}
            disabled={isOffline || acting}
            aria-describedby={isOffline ? AVISO_ALTERAR_OFFLINE_ID : undefined}
          >
            Alterar Pedido
          </Button>
          {isOffline && (
            <p id={AVISO_ALTERAR_OFFLINE_ID} className={styles.obs}>
              Sem internet: para alterar o pedido, conecte-se primeiro.
            </p>
          )}
          <div className={styles.actions}>
            <Button variant="ghost" fullWidth onClick={() => setShowCancel(true)}>Cancelar Pedido</Button>
            <Button variant="primary" fullWidth onClick={handleConfirm} loading={acting}>Confirmar Pedido</Button>
          </div>
        </>
      )}

      <ConfirmDialog
        isOpen={showCancel}
        title="Cancelar Pedido"
        description="O pedido sai da fila e o estoque não muda. Confirmar cancelamento?"
        confirmLabel="Cancelar Pedido"
        confirmVariant="danger"
        onConfirm={handleCancel}
        onCancel={() => setShowCancel(false)}
        loading={acting}
      />
    </div>
  );
}
