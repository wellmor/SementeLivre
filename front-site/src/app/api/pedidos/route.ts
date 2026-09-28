import { NextResponse } from "next/server";
import { randomUUID } from "crypto";
import { db } from "@/lib/store";

const sleep = () => new Promise((r) => setTimeout(r, 400));

export async function POST(request: Request) {
  await sleep();
  const body = await request.json();

  const especie = db.species.find((s) => s.id_especie === body.id_especie);

  const pedido = {
    id_pedido: randomUUID(),
    id_especie: String(body.id_especie ?? ""),
    id_comunidade: String(body.id_comunidade ?? ""),
    tipoPedido: body.tipoPedido,
    status: "PENDENTE" as const,
    nomeRecebedor: String(body.nomeRecebedor ?? ""),
    contatoRecebedor: String(body.contatoRecebedor ?? ""),
    mensagemOpcional: String(body.mensagemOpcional ?? ""),
    quantidade: Number(body.quantidade ?? 1),
    dataPedido: new Date().toISOString(),
  };

  db.pedidos.push(pedido);

  const notificacao = {
    id_notificacao: randomUUID(),
    id_comunidade: pedido.id_comunidade,
    id_pedido: pedido.id_pedido,
    titulo: `Novo pedido de ${pedido.nomeRecebedor}`,
    mensagem: `Pedido de ${pedido.quantidade} unidade(s) de ${especie?.nome_popular ?? pedido.id_especie} via ${pedido.tipoPedido}`,
    lida: false,
    dataGeracao: new Date().toISOString(),
  };

  db.notificacoes.push(notificacao);

  return NextResponse.json(pedido, { status: 201 });
}
