"use client";

import { useEffect, useId, useRef, useState } from "react";
import dynamic from "next/dynamic";
import { Loader2, MapPin, X } from "lucide-react";

import AvisoMapa from "@/components/aviso-mapa";
import { carregarPontos } from "@/lib/mapa/carregar-pontos";
import type { PontoMapa } from "@/lib/mapa/pontos";

function Carregando() {
  return (
    <div className="h-80 flex items-center justify-center bg-primary-light/40 rounded-xl">
      <Loader2 className="w-5 h-5 animate-spin text-primary" role="img" aria-label="Carregando mapa" />
    </div>
  );
}

// ssr: false porque o Leaflet depende de window/document
const MapaSementes = dynamic(() => import("@/components/mapa-sementes"), {
  ssr: false,
  loading: Carregando,
});

interface BotaoVerNoMapaProps {
  comunidadeId: string;
  comunidade: string;
}

/**
 * Botão "Ver no mapa" do card (RF-08): abre uma janela com o mapa na região
 * aproximada da comunidade.
 *
 * Nada do mapa é baixado antes do clique: nem o Leaflet, nem os pontos, nem as
 * imagens do OpenStreetMap. Assim a home não fica pesada e quem só olha o
 * catálogo não tem o IP enviado ao OpenStreetMap. A janela só existe no DOM
 * enquanto está aberta.
 */
export default function BotaoVerNoMapa({ comunidadeId, comunidade }: BotaoVerNoMapaProps) {
  const [aberto, setAberto] = useState(false);

  return (
    <>
      <button
        type="button"
        onClick={() => setAberto(true)}
        aria-label={`Ver no mapa a comunidade ${comunidade}`}
        className="inline-flex items-center gap-1 text-[11px] font-medium text-primary-dark underline-offset-2 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded"
      >
        <MapPin className="w-3 h-3" aria-hidden="true" />
        Ver no mapa
      </button>

      {aberto && (
        <JanelaMapa comunidadeId={comunidadeId} comunidade={comunidade} onFechar={() => setAberto(false)} />
      )}
    </>
  );
}

function JanelaMapa({
  comunidadeId,
  comunidade,
  onFechar,
}: BotaoVerNoMapaProps & { onFechar: () => void }) {
  const janela = useRef<HTMLDialogElement>(null);
  const idTitulo = useId();
  const [pontos, setPontos] = useState<PontoMapa[] | null>(null);
  const [erro, setErro] = useState(false);

  const ponto = pontos?.find((p) => p.comunidadeId === comunidadeId);

  useEffect(() => {
    // showModal: foco preso na janela, Esc fecha e o resto da página fica inerte
    // (o modo estrito do React roda este efeito duas vezes em desenvolvimento)
    if (janela.current && !janela.current.open) janela.current.showModal();
    carregarPontos()
      .then(setPontos)
      .catch(() => setErro(true));
  }, []);

  return (
    <dialog
      ref={janela}
      aria-labelledby={idTitulo}
      onClose={onFechar}
      className="m-auto w-[calc(100%-2rem)] max-w-2xl rounded-2xl bg-white p-6 shadow-xl backdrop:bg-black/50"
    >
      <div className="flex items-start justify-between gap-4 mb-3">
        <div>
          <h2 id={idTitulo} className="text-lg font-semibold text-foreground">
            {comunidade}
          </h2>
          {ponto && <p className="text-sm text-slate-600">Região aproximada: {ponto.localizacao}</p>}
        </div>
        <button
          type="button"
          onClick={() => janela.current?.close()}
          aria-label="Fechar mapa"
          className="rounded-md p-1 text-slate-600 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        >
          <X className="w-5 h-5" aria-hidden="true" />
        </button>
      </div>

      {erro ? (
        <p role="alert" className="text-sm text-red-700">
          Não foi possível carregar o mapa. Tente novamente em instantes.
        </p>
      ) : pontos === null ? (
        <Carregando />
      ) : ponto ? (
        <MapaSementes pontos={[ponto]} altura="320px" destaque={comunidadeId} />
      ) : (
        <p className="text-sm text-slate-600">A localização desta comunidade ainda não está cadastrada.</p>
      )}

      <AvisoMapa className="mt-3" />
    </dialog>
  );
}
