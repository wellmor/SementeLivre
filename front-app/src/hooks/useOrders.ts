'use client';

import { useEffect, useState, useCallback } from 'react';
import { isApiError } from '@/lib/api';
import { listarEstoques, listarProdutos, type ProdutoDTO } from '@/lib/catalogoApi';
import {
  atualizarPedido,
  buscarPedido,
  cancelarPedido,
  confirmarPedido,
  criarNotificacao,
  criarPedido,
  listarPedidos,
  type PedidoDTO,
} from '@/lib/pedidoApi';
import { buscarProprietarioApi } from '@/lib/authApi';
import { Pedido, StatusPedido, TipoPedido } from '@/types/order';
import { useAuth } from '@/context/AuthContext';

function paraData(iso: string | null | undefined): Date {
  return iso ? new Date(iso) : new Date();
}

/**
 * O /pedidos guarda só UUIDs (proprietarioRecebedorId e produtoId dos itens),
 * mas a tela exibe nome do recebedor e do produto. Estes mapas resolvem os dois
 * lados para texto: produtos por id e pesagem por produtoId (o /produtos não
 * tem unidade — quem tem é o /estoques).
 */
type DadosDeResolucao = {
  produtos: Map<string, ProdutoDTO>;
  pesagens: Map<string, string>;
  recebedores: Map<string, string>;
};

/** Carrega, em paralelo, tudo o que é preciso para legibilizar um conjunto de pedidos. */
async function carregarResolucao(
  pedidos: PedidoDTO[],
  meuId: string,
  meuNome: string
): Promise<DadosDeResolucao> {
  const [produtos, estoques] = await Promise.all([listarProdutos(), listarEstoques()]);

  const produtosPorId = new Map(produtos.map((p) => [p.id, p]));
  const pesagens = new Map(estoques.map((e) => [e.produtoId, e.tipoPesagem]));

  const recebedores = new Map<string, string>();
  const ids = [...new Set(pedidos.map((p) => p.proprietarioRecebedorId))];
  await Promise.all(
    ids.map(async (id) => {
      if (id === meuId) {
        recebedores.set(id, meuNome);
        return;
      }
      try {
        recebedores.set(id, (await buscarProprietarioApi(id)).nome);
      } catch {
        recebedores.set(id, 'Proprietário');
      }
    })
  );

  return { produtos: produtosPorId, pesagens, recebedores };
}

function paraPedido(
  dto: PedidoDTO,
  dados: DadosDeResolucao,
  meuId: string,
  meuNome: string
): Pedido {
  const idRecebedor = dto.proprietarioRecebedorId;

  const itens = dto.itens.map((item) => ({
    idItem: item.id,
    idProduto: item.produtoId,
    nomePopular: dados.produtos.get(item.produtoId)?.nomePopular ?? 'Produto',
    quantidade: item.quantidade,
    tipoPesagem: dados.pesagens.get(item.produtoId) ?? '',
    precoUnitario: item.precoUnitario ?? undefined,
  }));

  return {
    idPedido: dto.id,
    idProprietario: idRecebedor,
    tipoPedido: dto.tipoPedido as TipoPedido,
    status: dto.status as StatusPedido,
    nomeRecebedor: dados.recebedores.get(idRecebedor) ?? (idRecebedor === meuId ? meuNome : 'Proprietário'),
    mensagemOpcional: dto.mensagemOpcional ?? undefined,
    dataPedido: paraData(dto.dataPedido),
    itens,
    totalValor:
      dto.tipoPedido === 'VENDA'
        ? itens.reduce((soma, i) => soma + (i.precoUnitario ?? 0) * i.quantidade, 0)
        : undefined,
  };
}

export function useOrders() {
  const { user } = useAuth();
  const [orders, setOrders] = useState<Pedido[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const recarregar = useCallback(async () => {
    if (!user) {
      setOrders([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const pedidos = await listarPedidos(user.uid);
      const dados = await carregarResolucao(pedidos, user.uid, user.nome);

      setOrders(
        pedidos
          .map((dto) => paraPedido(dto, dados, user.uid, user.nome))
          .sort((a, b) => b.dataPedido.getTime() - a.dataPedido.getTime())
      );
      setError(null);
    } catch (err) {
      setError(isApiError(err) ? err.message : 'Não foi possível carregar seus pedidos.');
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    void (async () => {
      await recarregar();
    })();
  }, [recarregar]);

  const createOrder = useCallback(
    async (data: Omit<Pedido, 'idPedido'>): Promise<string> => {
      if (!user) throw new Error('Sessão expirada. Faça login novamente.');

      const pedido = await criarPedido({
        tipoPedido: data.tipoPedido,
        mensagemOpcional: data.mensagemOpcional ?? null,
        usuarioSolicitanteId: user.uid,
        proprietarioRecebedorId: data.idProprietario,
        itens: data.itens.map((item) => ({
          produtoId: item.idProduto,
          quantidade: item.quantidade,
          precoUnitario: item.precoUnitario ?? 0,
        })),
      });

      // A notificação é best-effort: o pedido já foi gravado, então uma falha
      // aqui não pode fazer o formulário parecer que falhou.
      await criarNotificacao({
        titulo: 'Novo pedido registrado',
        mensagem: `Pedido de ${data.tipoPedido.toLowerCase()} para ${data.nomeRecebedor} registrado.`,
        proprietarioId: data.idProprietario,
        pedidoRelacionadoId: pedido.id,
      }).catch(() => undefined);

      await recarregar();
      return pedido.id;
    },
    [user, recarregar]
  );

  const updateOrder = useCallback(
    async (id: string, data: Partial<Pedido>) => {
      const atual = orders.find((o) => o.idPedido === id);
      if (!atual) throw new Error('Pedido não encontrado.');

      await atualizarPedido(id, {
        tipoPedido: data.tipoPedido ?? atual.tipoPedido,
        mensagemOpcional: data.mensagemOpcional ?? atual.mensagemOpcional ?? null,
        itens: (data.itens ?? atual.itens).map((item) => ({
          produtoId: item.idProduto,
          quantidade: item.quantidade,
          precoUnitario: item.precoUnitario ?? 0,
        })),
      });

      await recarregar();
    },
    [orders, recarregar]
  );

  const cancelOrder = useCallback(
    async (id: string) => {
      await cancelarPedido(id);
      await recarregar();
    },
    [recarregar]
  );

  const confirmOrder = useCallback(
    async (id: string) => {
      await confirmarPedido(id);
      await recarregar();
    },
    [recarregar]
  );

  const getOrder = useCallback(
    async (id: string): Promise<Pedido | null> => {
      if (!user) return null;
      try {
        const dto = await buscarPedido(id);
        const dados = await carregarResolucao([dto], user.uid, user.nome);
        return paraPedido(dto, dados, user.uid, user.nome);
      } catch {
        return orders.find((o) => o.idPedido === id) ?? null;
      }
    },
    [user, orders]
  );

  return { orders, loading, error, createOrder, updateOrder, cancelOrder, confirmOrder, getOrder, recarregar };
}
