'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useOrders } from '@/hooks/useOrders';
import { useSeeds } from '@/hooks/useSeeds';
import { useAuth } from '@/context/AuthContext';
import { useToast } from '@/components/feedback/Toast';
import { StatusPedido, TipoPedido } from '@/types/order';
import { Estoque, DisponibilidadeProduto } from '@/types/stock';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Toggle } from '@/components/ui/toggle';
import { Button } from '@/components/ui/button';
import { Dialog } from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import styles from './novoPedido.module.css';

interface ItemForm { seed: Estoque; quantidade: number; }

const tipoOptions = [
  { value: TipoPedido.VENDA, label: 'Venda' },
  { value: TipoPedido.TROCA, label: 'Troca' },
  { value: TipoPedido.DOACAO, label: 'Doação' },
];

export default function NovoPedidoPage() {
  const { user } = useAuth();
  const { createOrder } = useOrders();
  const { seeds } = useSeeds();
  const { showToast } = useToast();
  const router = useRouter();

  const [tipo, setTipo] = useState<TipoPedido>(TipoPedido.VENDA);
  const [nomeRecebedor, setNomeRecebedor] = useState('');
  const [contato, setContato] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [itens, setItens] = useState<ItemForm[]>([]);
  const [showSeedPicker, setShowSeedPicker] = useState(false);
  const [pickQty, setPickQty] = useState('');
  const [selectedSeed, setSelectedSeed] = useState<Estoque | null>(null);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const availableSeeds = seeds.filter(s => s.disponibilidade !== DisponibilidadeProduto.INDISPONIVEL);
  const total = itens.reduce((sum, i) => sum + (i.seed.preco || 0) * i.quantidade, 0);

  const addItem = () => {
    if (!selectedSeed || !pickQty || Number(pickQty) <= 0) return;
    if (Number(pickQty) > selectedSeed.quantidade) { showToast('Quantidade maior que o estoque disponível.', 'error'); return; }
    setItens(prev => {
      const exists = prev.find(i => i.seed.idEstoque === selectedSeed.idEstoque);
      if (exists) return prev.map(i => i.seed.idEstoque === selectedSeed.idEstoque ? { ...i, quantidade: Number(pickQty) } : i);
      return [...prev, { seed: selectedSeed, quantidade: Number(pickQty) }];
    });
    setShowSeedPicker(false);
    setPickQty('');
    setSelectedSeed(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const e2: Record<string, string> = {};
    if (!nomeRecebedor) e2.nomeRecebedor = 'Nome é obrigatório';
    if (itens.length === 0) e2.itens = 'Adicione ao menos um item';
    setErrors(e2);
    if (Object.keys(e2).length > 0) return;
    if (!user) return;
    setLoading(true);
    try {
      await createOrder({
        idProprietario: user.uid,
        tipoPedido: tipo,
        // O status nasce PENDENTE no backend (/pedidos); não é enviado no cadastro.
        status: StatusPedido.PENDENTE,
        nomeRecebedor,
        contatoRecebedor: contato,
        mensagemOpcional: mensagem,
        dataPedido: new Date(),
        itens: itens.map(i => ({
          idItem: i.seed.idEstoque,
          idProduto: i.seed.idProduto,
          nomePopular: i.seed.nomePopular,
          quantidade: i.quantidade,
          tipoPesagem: i.seed.tipoPesagem,
          precoUnitario: i.seed.preco,
        })),
        totalValor: tipo === TipoPedido.VENDA ? total : undefined,
      });
      showToast('Pedido registrado com sucesso!', 'success');
      router.push('/pedidos');
    } catch { showToast('Erro ao registrar pedido.', 'error'); }
    finally { setLoading(false); }
  };

  return (
    <form onSubmit={handleSubmit} className={styles.form} noValidate>
      <div className={styles.card}>
        <p className={styles.sectionTitle}>Tipo de Pedido</p>
        <Toggle options={tipoOptions} value={tipo} onChange={(v) => setTipo(v as TipoPedido)} />
      </div>

      <div className={styles.card}>
        <p className={styles.sectionTitle}>Recebedor</p>
        <Input label="Nome completo" value={nomeRecebedor} onChange={(e) => setNomeRecebedor(e.target.value)} error={errors.nomeRecebedor} required />
        <Input label="Contato (telefone ou e-mail)" value={contato} onChange={(e) => setContato(e.target.value)} />
      </div>

      <div className={styles.card}>
        <div className={styles.sectionHeader}>
          <p className={styles.sectionTitle}>Sementes</p>
          <Button type="button" variant="secondary" size="sm" onClick={() => setShowSeedPicker(true)}>+ Adicionar</Button>
        </div>
        {errors.itens && <span className={styles.error} role="alert">⚠ {errors.itens}</span>}
        {itens.length > 0 && (
          <ul className={styles.itensList}>
            {itens.map((item) => (
              <li key={item.seed.idEstoque} className={styles.itemRow}>
                <span className={styles.itemName}>{item.seed.nomePopular}</span>
                <span className={styles.itemQty}>{item.quantidade} {item.seed.tipoPesagem}</span>
                {tipo === TipoPedido.VENDA && item.seed.preco && (
                  <span className={styles.itemPrice}>R$ {(item.seed.preco * item.quantidade).toFixed(2)}</span>
                )}
                <button type="button" className={styles.removeItem} onClick={() => setItens(p => p.filter(i => i.seed.idEstoque !== item.seed.idEstoque))} aria-label={`Remover ${item.seed.nomePopular}`}>✕</button>
              </li>
            ))}
          </ul>
        )}
        {tipo === TipoPedido.VENDA && itens.length > 0 && (
          <p className={styles.total}>Total: <strong>R$ {total.toFixed(2)}</strong></p>
        )}
      </div>

      <div className={styles.card}>
        <Textarea label="Observações (opcional)" value={mensagem} onChange={(e) => setMensagem(e.target.value)} placeholder="Informações adicionais sobre o pedido..." />
      </div>

      <Button type="submit" fullWidth size="lg" loading={loading}>Registrar Pedido</Button>

      {/* Seed Picker Dialog */}
      <Dialog isOpen={showSeedPicker} onClose={() => { setShowSeedPicker(false); setSelectedSeed(null); setPickQty(''); }} title="Selecionar Semente">
        <div className={styles.seedList}>
          {availableSeeds.map((s) => (
            <button
              key={s.idEstoque}
              type="button"
              className={`${styles.seedOption} ${selectedSeed?.idEstoque === s.idEstoque ? styles.seedSelected : ''}`}
              onClick={() => setSelectedSeed(s)}
            >
              <span className={styles.seedName}>{s.nomePopular}</span>
              <span className={styles.seedStock}>Estoque: {s.quantidade} {s.tipoPesagem}</span>
              <Badge variant="availability" value={s.disponibilidade} />
            </button>
          ))}
        </div>
        {selectedSeed && (
          <div className={styles.qtyRow}>
            <Input label={`Quantidade (máx: ${selectedSeed.quantidade})`} value={pickQty} onChange={(e) => setPickQty(e.target.value)} type="number" inputMode="decimal" min="1" max={String(selectedSeed.quantidade)} />
            <Button type="button" onClick={addItem} variant="primary">Adicionar</Button>
          </div>
        )}
      </Dialog>
    </form>
  );
}
