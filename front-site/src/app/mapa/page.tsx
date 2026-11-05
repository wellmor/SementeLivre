"use client";

import { useEffect, useState } from "react";
import dynamic from "next/dynamic";
import { Loader2, MapPin } from "lucide-react";

import PublicHeader from "@/components/public-header";
import AvisoMapa from "@/components/aviso-mapa";
import { carregarPontos } from "@/lib/mapa/carregar-pontos";
import type { PontoMapa } from "@/lib/mapa/pontos";

function Carregando() {
  return (
    <div className="h-[500px] flex items-center justify-center bg-primary-light/40 rounded-xl">
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

  useEffect(() => {
    carregarPontos()
      .then(setPontos)
      .catch(() => setErro(true));
  }, []);

  return (
    <div className="min-h-screen bg-background">
      <PublicHeader />

      <main className="max-w-7xl mx-auto px-6 py-10 space-y-6">
        <div className="space-y-2">
          <h1 className="flex items-center gap-2 text-2xl font-bold text-primary-dark">
            <MapPin className="w-5 h-5 text-primary" aria-hidden="true" />
            Mapa de Sementes
          </h1>
          <p className="text-slate-600 max-w-2xl">
            Comunidades com bancos de sementes ativos na rede.
          </p>
          <AvisoMapa className="max-w-2xl" />
        </div>

        {erro ? (
          <p role="alert" className="text-sm text-red-700">
            Não foi possível carregar o mapa. Tente novamente em instantes.
          </p>
        ) : pontos === null ? (
          <Carregando />
        ) : (
          <>
            <MapaSementes pontos={pontos} />

            {/* Mesma informação em texto, para leitor de tela e para quem não usa o mapa */}
            <section aria-labelledby="titulo-lista" className="space-y-3">
              <h2 id="titulo-lista" className="text-lg font-semibold text-foreground">
                {pontos.length === 1 ? "1 comunidade no mapa" : `${pontos.length} comunidades no mapa`}
              </h2>
              <ul className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                {pontos.map((ponto) => (
                  <li key={ponto.comunidadeId} className="rounded-xl border border-border bg-white p-4">
                    <p className="font-medium text-primary-dark">🌿 {ponto.comunidade}</p>
                    <p className="text-sm text-slate-600">{ponto.localizacao}</p>
                    <p className="text-xs text-muted-foreground mt-1">{ponto.produtos.map((p) => p.nome).join(", ")}</p>
                  </li>
                ))}
              </ul>
            </section>
          </>
        )}
      </main>
    </div>
  );
}
