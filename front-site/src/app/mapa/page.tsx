"use client";

import { useEffect, useState } from "react";
import dynamic from "next/dynamic";
import { Loader2, MapPin } from "lucide-react";

import PublicHeader from "@/components/public-header";
import { montarPontos, type PontoMapa } from "@/lib/mapa/pontos";
import type { Comunidade, Species } from "@/lib/types";

// ssr: false porque o Leaflet usa window/document, que nao existem no servidor.
// Este e o formato do next/dynamic nesta versao do Next (ver guia de lazy loading).
const MapaSementes = dynamic(() => import("@/components/mapa-sementes"), {
  ssr: false,
  loading: () => (
    <div className="h-[500px] flex items-center justify-center bg-green-50 rounded-xl">
      <Loader2 className="w-6 h-6 animate-spin text-green-600" />
    </div>
  ),
});

export default function MapaPage() {
  const [pontos, setPontos] = useState<PontoMapa[]>([]);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    // PONTO DE INTEGRACAO: hoje le o catalogo em memoria do proprio site.
    // Quando o backend expuser o catalogo publico (issue #93) e as comunidades,
    // troque estas duas chamadas pelas do backend. O resto da tela nao muda.
    Promise.all([
      fetch("/api/comunidades").then((r) => r.json()),
      fetch("/api/catalog").then((r) => r.json()),
    ])
      .then(([comunidades, especies]: [Comunidade[], Species[]]) => {
        setPontos(montarPontos(comunidades, especies));
      })
      .finally(() => setCarregando(false));
  }, []);

  return (
    <div className="min-h-screen bg-white">
      <PublicHeader />

      <main className="max-w-7xl mx-auto px-6 py-10">
        <div className="flex items-center gap-2 mb-2">
          <MapPin className="w-5 h-5 text-green-600" />
          <h1 className="text-2xl font-bold text-green-900">Mapa de Sementes</h1>
        </div>

        <p className="text-gray-600 mb-6 max-w-2xl">
          Comunidades com bancos de sementes ativos. A localizacao e aproximada,
          por municipio ou estado: o mapa nao mostra o endereco das propriedades.
        </p>

        {carregando ? (
          <div className="h-[500px] flex items-center justify-center bg-green-50 rounded-xl">
            <Loader2 className="w-6 h-6 animate-spin text-green-600" />
          </div>
        ) : (
          <>
            <MapaSementes pontos={pontos} />

            <p className="text-sm text-gray-500 mt-3">
              {pontos.length} comunidade(s) no mapa.
            </p>
          </>
        )}
      </main>
    </div>
  );
}
