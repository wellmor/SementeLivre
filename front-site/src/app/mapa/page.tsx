"use client";

import { useEffect, useRef, useState } from "react";
import dynamic from "next/dynamic";
import Link from "next/link";
import { ArrowRight, Loader2, MapPin } from "lucide-react";

import PublicHeader from "@/components/public-header";
import AvisoMapa from "@/components/aviso-mapa";
import FotoProduto from "@/components/foto-produto";
import { Button, buttonVariants } from "@/components/ui/button";
import { carregarPontos } from "@/lib/mapa/carregar-pontos";
import type { PontoMapa } from "@/lib/mapa/pontos";

/** Miniaturas por card; acima disso a última casa vira "+N". */
const FOTOS_NO_CARD = 6;

function Carregando() {
  return (
    <div className="h-125 flex items-center justify-center bg-primary-light/40 rounded-xl">
      <Loader2 className="w-6 h-6 animate-spin text-primary" role="img" aria-label="Carregando mapa" />
    </div>
  );
}

// ssr: false porque o Leaflet usa window/document, que não existem no servidor
const MapaSementes = dynamic(() => import("@/components/mapa-sementes"), {
  ssr: false,
  loading: Carregando,
});

export default function MapaPage() {
  const [pontos, setPontos] = useState<PontoMapa[] | null>(null);
  const [erro, setErro] = useState(false);
  const [foco, setFoco] = useState<{ comunidadeId?: string; vez: number }>({ vez: 0 });
  const areaDoMapa = useRef<HTMLDivElement>(null);

  useEffect(() => {
    carregarPontos()
      .then(setPontos)
      .catch(() => setErro(true));
  }, []);

  function verNoMapa(comunidadeId: string) {
    setFoco((atual) => ({ comunidadeId, vez: atual.vez + 1 }));
    areaDoMapa.current?.scrollIntoView({ behavior: "smooth", block: "center" });
  }

  return (
    <div className="min-h-screen bg-background">
      <PublicHeader />

      <main className="max-w-7xl mx-auto px-6 py-10 space-y-8">
        <div className="space-y-2">
          <h1 className="flex items-center gap-2 text-2xl font-bold text-primary-dark">
            <MapPin className="w-5 h-5 text-primary" aria-hidden="true" />
            Mapa de Sementes
          </h1>
          <p className="text-slate-600 max-w-2xl">Comunidades com bancos de sementes ativos na rede.</p>
          <AvisoMapa />
        </div>

        {erro ? (
          <p role="alert" className="text-sm text-red-700">
            Não foi possível carregar o mapa. Tente novamente em instantes.
          </p>
        ) : pontos === null ? (
          <Carregando />
        ) : (
          <>
            <div ref={areaDoMapa}>
              <MapaSementes pontos={pontos} destaque={foco.comunidadeId} focoVez={foco.vez} />
            </div>

            {/* Mesma informação em texto, para leitor de tela e para quem não usa o mapa */}
            <section aria-labelledby="titulo-lista" className="space-y-4">
              <h2 id="titulo-lista" className="text-lg font-semibold text-foreground">
                {pontos.length === 1 ? "1 comunidade no mapa" : `${pontos.length} comunidades no mapa`}
              </h2>
              <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {pontos.map((ponto) => (
                  <CardComunidade key={ponto.comunidadeId} ponto={ponto} onVerNoMapa={verNoMapa} />
                ))}
              </ul>
            </section>
          </>
        )}
      </main>
    </div>
  );
}

function CardComunidade({
  ponto,
  onVerNoMapa,
}: {
  ponto: PontoMapa;
  onVerNoMapa: (comunidadeId: string) => void;
}) {
  const total = ponto.produtos.length;
  const cabem = total > FOTOS_NO_CARD ? FOTOS_NO_CARD - 1 : total;
  const resto = total - cabem;

  return (
    <li className="flex flex-col gap-4 rounded-2xl border border-border bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="font-semibold text-primary-dark leading-tight">🌿 {ponto.comunidade}</h3>
          <p className="mt-1 flex items-center gap-1 text-sm text-slate-600">
            <MapPin className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
            {ponto.localizacao}
          </p>
        </div>
        <span className="shrink-0 rounded-full bg-primary-light px-2.5 py-1 text-xs font-medium text-primary-dark">
          {total === 1 ? "1 produto" : `${total} produtos`}
        </span>
      </div>

      <ul aria-label={`Produtos de ${ponto.comunidade}`} className="grid grid-cols-6 gap-2">
        {ponto.produtos.slice(0, cabem).map((produto) => (
          <li
            key={produto.nome}
            title={produto.nome}
            className="aspect-square overflow-hidden rounded-lg bg-primary-light/60 flex items-center justify-center"
          >
            <FotoProduto src={produto.urlFoto} alt={produto.nome} iconClassName="w-5 h-5 text-primary/50" />
          </li>
        ))}
        {resto > 0 && (
          <li
            title={`Mais ${resto} produtos`}
            className="aspect-square rounded-lg bg-primary-light flex items-center justify-center text-sm font-semibold text-primary-dark"
          >
            +{resto}
          </li>
        )}
      </ul>

      <div className="mt-auto flex flex-wrap gap-2">
        <Button
          size="sm"
          variant="outline"
          className="border-border text-primary-dark hover:bg-primary-light gap-1.5"
          onClick={() => onVerNoMapa(ponto.comunidadeId)}
          aria-label={`Ver no mapa a comunidade ${ponto.comunidade}`}
        >
          <MapPin className="w-3.5 h-3.5" aria-hidden="true" />
          Ver no mapa
        </Button>
        <Link
          href={`/?comunidade=${ponto.comunidadeId}#produtos`}
          className={buttonVariants({ size: "sm", className: "gap-1.5 text-white" })}
          aria-label={`Ver produtos da comunidade ${ponto.comunidade}`}
        >
          Ver produtos
          <ArrowRight className="w-3.5 h-3.5" aria-hidden="true" />
        </Link>
      </div>
    </li>
  );
}
