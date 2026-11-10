'use client';

import React, { useEffect, useId, useMemo, useRef, useState } from 'react';
import { useOrders } from '@/hooks/useOrders';
import { EstoqueParaPedido, useOrderStock } from '@/hooks/useOrderStock';
import { useOffline } from '@/hooks/useOffline';
import { useToast } from '@/components/feedback/Toast';
import { errorMessage, isApiError } from '@/lib/api';
import { Pedido, TipoPedido } from '@/types/order';
import { DisponibilidadeProduto, PesagemLabels } from '@/types/stock';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Toggle } from '@/components/ui/toggle';
import { Button } from '@/components/ui/button';
import { Dialog } from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { formatarMoeda } from '@/lib/formato';
import styles from './alterarPedido.module.css';

interface ItemForm {
  idProduto: string;
  nomePopular: string;
  /** Texto do campo; vira número na validação. */
  quantidade: string;
  /** Preço combinado quando o pedido foi feito; sem ele, vale o do estoque. */
  precoDoPedido?: number;
}

interface AlterarPedidoProps {
  pedido: Pedido;
  onSalvo: (pedido: Pedido) => void;
  onCancelar: () => void;
  /** O pedido mudou no servidor (ex.: confirmado em outra aba): o detalhe recarrega. */
  onDesatualizado: () => void;
}

const tipoOptions = [
  { value: TipoPedido.VENDA, label: 'Venda' },
  { value: TipoPedido.TROCA, label: 'Troca' },
  { value: TipoPedido.DOACAO, label: 'Doação' },
];

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Erro de campo do backend (fieldErrors) → campo do formulário
const CAMPOS_DA_API: Record<string, string> = {
  'comprador.nome': 'nome',
  'comprador.telefone': 'contato',
  'comprador.email': 'email',
  mensagemOpcional: 'mensagem',
};

function chaveDoItem(idProduto: string) {
  return `item-${idProduto}`;
}

function unidade(estoque?: EstoqueParaPedido) {
  return estoque ? ` ${PesagemLabels[estoque.tipoPesagem]}` : '';
}

/**
 * Formulário de alteração de pedido PENDENTE (CDU-13). Parte dos dados atuais
 * e salva tudo de uma vez. O estoque não muda ao salvar: o backend só confere
 * se o saldo cobre o pedido alterado; a baixa continua na confirmação (#183).
 *
 * Sem internet não salva: a alteração depende do saldo do servidor e não entra
 * na fila do aparelho (essa fila é só para movimentação de estoque).
 */
export function AlterarPedido({ pedido, onSalvo, onCancelar, onDesatualizado }: AlterarPedidoProps) {
  const { updateOrder } = useOrders();
  const { stock, loading: loadingStock, error: stockError, reload: reloadStock } = useOrderStock();
  const { isOffline } = useOffline();
  const { showToast } = useToast();

  const [tipo, setTipo] = useState<TipoPedido>(pedido.tipoPedido);
  const [nome, setNome] = useState(pedido.recebedor?.nome ?? '');
  const [contato, setContato] = useState(pedido.recebedor?.telefone ?? '');
  const [email, setEmail] = useState(pedido.recebedor?.email ?? '');
  const [mensagem, setMensagem] = useState(pedido.mensagemOpcional ?? '');
  const [itens, setItens] = useState<ItemForm[]>(() =>
    pedido.itens.map((item) => ({
      idProduto: item.idProduto,
      nomePopular: item.nomePopular,
      quantidade: String(item.quantidade),
      precoDoPedido: item.precoUnitario,
    }))
  );
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [erroGeral, setErroGeral] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  const [showSeedPicker, setShowSeedPicker] = useState(false);
  const [selectedSeed, setSelectedSeed] = useState<EstoqueParaPedido | null>(null);
  const [pickQty, setPickQty] = useState('');
  const [pickError, setPickError] = useState<string | undefined>();

  // O campo de e-mail só aparece em pedido que já tinha um (feito pelo site)
  const mostrarEmail = !!pedido.recebedor?.email;

  const formRef = useRef<HTMLFormElement>(null);
  const tituloRef = useRef<HTMLHeadingElement>(null);
  const tituloId = useId();
  const avisoOfflineId = useId();
  const erroItensId = useId();

  // Ao abrir, o foco vai para o título do formulário (leitor de tela anuncia a troca)
  useEffect(() => {
    tituloRef.current?.focus();
  }, []);

  const estoquePorProduto = useMemo(() => new Map(stock.map((s) => [s.idProduto, s])), [stock]);
  const estoqueCarregado = !loadingStock && !stockError;
  const availableSeeds = stock.filter((s) => s.disponibilidade !== DisponibilidadeProduto.INDISPONIVEL && s.quantidade > 0);

  // Venda mantém o preço combinado; item novo (ou que era troca/doação) usa o do estoque
  const precoDoItem = (item: ItemForm): number | undefined =>
    tipo === TipoPedido.VENDA ? item.precoDoPedido ?? estoquePorProduto.get(item.idProduto)?.preco : undefined;

  const total = itens.reduce((soma, item) => soma + (precoDoItem(item) ?? 0) * (Number(item.quantidade) || 0), 0);

  const limparErro = (campo: string) => {
    setErrors((prev) => {
      if (!prev[campo]) return prev;
      const resto = { ...prev };
      delete resto[campo];
      return resto;
    });
  };

  const alterarQuantidade = (idProduto: string, valor: string) => {
    setItens((prev) => prev.map((item) => (item.idProduto === idProduto ? { ...item, quantidade: valor } : item)));
    limparErro(chaveDoItem(idProduto));
  };

  const removerItem = (idProduto: string) => {
    setItens((prev) => prev.filter((item) => item.idProduto !== idProduto));
    limparErro(chaveDoItem(idProduto));
  };

  const fecharPicker = () => {
    setShowSeedPicker(false);
    setSelectedSeed(null);
    setPickQty('');
    setPickError(undefined);
  };

  const adicionarItem = () => {
    if (!selectedSeed) return;
    const qtd = Number(pickQty);
    if (!pickQty || !qtd || qtd <= 0) {
      setPickError('Informe uma quantidade maior que zero.');
      return;
    }
    if (qtd > selectedSeed.quantidade) {
      setPickError('Quantidade maior que o estoque disponível.');
      return;
    }
    setItens((prev) => {
      const existente = prev.find((item) => item.idProduto === selectedSeed.idProduto);
      if (existente) {
        return prev.map((item) => (item.idProduto === selectedSeed.idProduto ? { ...item, quantidade: pickQty } : item));
      }
      return [...prev, { idProduto: selectedSeed.idProduto, nomePopular: selectedSeed.nomePopular, quantidade: pickQty }];
    });
    limparErro('itens');
    limparErro(chaveDoItem(selectedSeed.idProduto));
    fecharPicker();
  };

  const validar = (): Record<string, string> => {
    const e: Record<string, string> = {};
    const nomeLimpo = nome.trim();
    if (!nomeLimpo) e.nome = 'Nome é obrigatório';
    else if (nomeLimpo.length > 150) e.nome = 'Nome deve ter no máximo 150 caracteres';
    if (contato.trim().length > 20) e.contato = 'Contato deve ter no máximo 20 caracteres';
    const emailLimpo = email.trim();
    if (emailLimpo && (!EMAIL_VALIDO.test(emailLimpo) || emailLimpo.length > 254)) e.email = 'E-mail inválido';
    if (itens.length === 0) e.itens = 'Adicione ao menos um item';

    itens.forEach((item) => {
      const qtd = Number(item.quantidade);
      const estoque = estoquePorProduto.get(item.idProduto);
      const chave = chaveDoItem(item.idProduto);
      if (!item.quantidade.trim() || !qtd || qtd <= 0) {
        e[chave] = 'Informe uma quantidade maior que zero';
      } else if (estoqueCarregado && !estoque) {
        e[chave] = 'Este produto não está mais no seu estoque. Remova o item.';
      } else if (estoque?.disponibilidade === DisponibilidadeProduto.INDISPONIVEL) {
        e[chave] = 'Produto marcado como indisponível no estoque.';
      } else if (estoque && qtd > estoque.quantidade) {
        e[chave] = `Quantidade maior que o estoque disponível (${estoque.quantidade}${unidade(estoque)}).`;
      }
    });
    return e;
  };

  // Leva o foco ao primeiro campo com erro, depois que a tela redesenha
  const focarPrimeiroErro = () => {
    requestAnimationFrame(() => {
      formRef.current?.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus();
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErroGeral(null);
    const novosErros = validar();
    setErrors(novosErros);
    if (Object.keys(novosErros).length > 0) {
      focarPrimeiroErro();
      return;
    }
    if (!navigator.onLine) {
      setErroGeral('Sem internet: o pedido não foi alterado. Conecte-se e salve de novo.');
      return;
    }

    setSalvando(true);
    try {
      const atualizado = await updateOrder(pedido.idPedido, {
        tipoPedido: tipo,
        nomeRecebedor: nome,
        contatoRecebedor: contato,
        emailRecebedor: email,
        mensagemOpcional: mensagem,
        itens: itens.map((item) => ({
          idProduto: item.idProduto,
          quantidade: Number(item.quantidade),
          precoUnitario: precoDoItem(item),
        })),
      });
      showToast('Pedido alterado com sucesso!', 'success');
      onSalvo(atualizado);
    } catch (err) {
      if (isApiError(err) && err.status === 0) {
        // api.ts já avisou por toast; aqui fica o que aconteceu com o formulário
        setErroGeral('Sem internet: o pedido não foi alterado. Conecte-se e salve de novo.');
      } else if (isApiError(err) && (err.status === 409 || err.status === 404)) {
        // Confirmado, cancelado (ex.: recusado pela fila) ou excluído enquanto era editado
        showToast(errorMessage(err, 'Este pedido não pode mais ser alterado.'), 'error');
        onDesatualizado();
      } else if (isApiError(err) && err.status === 422) {
        // Ex.: estoque insuficiente; o saldo pode ter mudado desde que a tela abriu
        setErroGeral(err.message);
        reloadStock();
      } else if (isApiError(err) && err.status === 400 && Object.keys(err.fieldErrors).length > 0) {
        const doFormulario: Record<string, string> = {};
        const outros: string[] = [];
        Object.entries(err.fieldErrors).forEach(([campo, msg]) => {
          if (CAMPOS_DA_API[campo]) doFormulario[CAMPOS_DA_API[campo]] = msg;
          else outros.push(msg);
        });
        setErrors(doFormulario);
        setErroGeral(outros.length ? outros.join(' ') : null);
        focarPrimeiroErro();
      } else {
        setErroGeral(errorMessage(err, 'Erro ao alterar o pedido.'));
      }
    } finally {
      setSalvando(false);
    }
  };

  return (
    <>
      <form ref={formRef} onSubmit={handleSubmit} className={styles.form} noValidate aria-labelledby={tituloId}>
        <div className={styles.cabecalho}>
          <h2 id={tituloId} ref={tituloRef} tabIndex={-1} className={styles.titulo}>
            Alterar pedido #{pedido.idPedido.slice(-6).toUpperCase()}
          </h2>
          <p className={styles.explicacao}>
            O estoque não muda ao salvar: ele só é baixado quando você confirmar o pedido.
          </p>
        </div>

        {/* Região sempre no DOM para o leitor de tela anunciar quando a internet cai */}
        <div role="status" aria-live="polite">
          {isOffline && (
            <p id={avisoOfflineId} className={styles.avisoOffline}>
              Sem internet: não é possível salvar a alteração agora. O que você preencheu continua aqui;
              salve quando a conexão voltar.
            </p>
          )}
        </div>

        <div className={styles.card}>
          <p className={styles.sectionTitle}>Tipo de Pedido</p>
          <Toggle options={tipoOptions} value={tipo} onChange={(v) => setTipo(v as TipoPedido)} />
        </div>

        <div className={styles.card}>
          <p className={styles.sectionTitle}>Recebedor</p>
          <Input
            label="Nome completo"
            value={nome}
            onChange={(e) => { setNome(e.target.value); limparErro('nome'); }}
            error={errors.nome}
            maxLength={150}
            autoComplete="off"
            required
          />
          <Input
            label="Contato (telefone ou e-mail)"
            value={contato}
            onChange={(e) => { setContato(e.target.value); limparErro('contato'); }}
            error={errors.contato}
            maxLength={20}
            autoComplete="off"
          />
          {mostrarEmail && (
            <Input
              label="E-mail de quem pediu"
              type="email"
              inputMode="email"
              value={email}
              onChange={(e) => { setEmail(e.target.value); limparErro('email'); }}
              error={errors.email}
              hint="Recebe os avisos do pedido por e-mail."
              maxLength={254}
              autoComplete="off"
            />
          )}
        </div>

        <div className={styles.card}>
          <div className={styles.sectionHeader}>
            <p className={styles.sectionTitle}>Produtos</p>
            <Button type="button" variant="secondary" size="sm" onClick={() => setShowSeedPicker(true)}>+ Adicionar</Button>
          </div>
          {errors.itens && <span id={erroItensId} className={styles.error} role="alert">⚠ {errors.itens}</span>}
          {stockError && (
            <span className={styles.error} role="alert">⚠ {stockError} O servidor confere o saldo ao salvar.</span>
          )}
          {itens.length > 0 && (
            <ul className={styles.itensList} aria-label="Itens do pedido">
              {itens.map((item) => {
                const estoque = estoquePorProduto.get(item.idProduto);
                const preco = precoDoItem(item);
                const qtd = Number(item.quantidade) || 0;
                return (
                  <li key={item.idProduto} className={styles.itemRow}>
                    <div className={styles.itemTopo}>
                      <span className={styles.itemName}>{item.nomePopular}</span>
                      {preco !== undefined && qtd > 0 && (
                        <span className={styles.itemPrice}>{formatarMoeda(preco * qtd)}</span>
                      )}
                      <button
                        type="button"
                        className={styles.removeItem}
                        onClick={() => removerItem(item.idProduto)}
                        aria-label={`Remover ${item.nomePopular}`}
                      >
                        ✕
                      </button>
                    </div>
                    <Input
                      label="Quantidade"
                      aria-label={`Quantidade de ${item.nomePopular}`}
                      value={item.quantidade}
                      onChange={(e) => alterarQuantidade(item.idProduto, e.target.value)}
                      error={errors[chaveDoItem(item.idProduto)]}
                      hint={estoque ? `Estoque: ${estoque.quantidade}${unidade(estoque)}` : undefined}
                      type="number"
                      inputMode="decimal"
                      min="0"
                      step="any"
                    />
                  </li>
                );
              })}
            </ul>
          )}
          {tipo === TipoPedido.VENDA && itens.length > 0 && (
            <p className={styles.total}>Total: <strong>{formatarMoeda(total)}</strong></p>
          )}
        </div>

        <div className={styles.card}>
          <Textarea
            label="Observações (opcional)"
            value={mensagem}
            onChange={(e) => { setMensagem(e.target.value); limparErro('mensagem'); }}
            error={errors.mensagem}
            placeholder="Informações adicionais sobre o pedido..."
          />
        </div>

        {erroGeral && (
          <p className={styles.erroGeral} role="alert">⚠ {erroGeral}</p>
        )}

        <div className={styles.actions}>
          <Button type="button" variant="ghost" fullWidth onClick={onCancelar} disabled={salvando}>
            Descartar alterações
          </Button>
          <Button
            type="submit"
            variant="primary"
            fullWidth
            loading={salvando}
            disabled={isOffline}
            aria-describedby={isOffline ? avisoOfflineId : undefined}
          >
            Salvar Alterações
          </Button>
        </div>
      </form>

      {/* Fora do form: Enter no campo do diálogo não envia a alteração */}
      <Dialog isOpen={showSeedPicker} onClose={fecharPicker} title="Selecionar Produto">
        <div className={styles.seedList}>
          {loadingStock && <p className={styles.seedStock}>Carregando estoque...</p>}
          {stockError && <span className={styles.error} role="alert">⚠ {stockError}</span>}
          {!loadingStock && !stockError && availableSeeds.length === 0 && (
            <p className={styles.seedStock}>Nenhum produto com estoque disponível.</p>
          )}
          {availableSeeds.map((s) => (
            <button
              key={s.idEstoque}
              type="button"
              className={`${styles.seedOption} ${selectedSeed?.idEstoque === s.idEstoque ? styles.seedSelected : ''}`}
              onClick={() => { setSelectedSeed(s); setPickError(undefined); }}
              aria-pressed={selectedSeed?.idEstoque === s.idEstoque}
            >
              <span className={styles.seedName}>{s.nomePopular}</span>
              <span className={styles.seedStock}>Estoque: {s.quantidade} {PesagemLabels[s.tipoPesagem]}</span>
              <Badge variant="availability" value={s.disponibilidade} />
            </button>
          ))}
        </div>
        {selectedSeed && (
          <div className={styles.qtyRow}>
            <Input
              label={`Quantidade (máx: ${selectedSeed.quantidade})`}
              value={pickQty}
              onChange={(e) => { setPickQty(e.target.value); setPickError(undefined); }}
              error={pickError}
              type="number"
              inputMode="decimal"
              min="0"
              step="any"
              max={String(selectedSeed.quantidade)}
            />
            <Button type="button" onClick={adicionarItem} variant="primary">Adicionar</Button>
          </div>
        )}
      </Dialog>
    </>
  );
}
