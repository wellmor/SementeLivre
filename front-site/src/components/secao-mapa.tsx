"use client";

import { useEffect, useRef, useState } from "react";
import dynamic from "next/dynamic";
import { Loader2 } from "lucide-react";

import AvisoMapa from "@/components/aviso-mapa";
import { carregarPontos } from "@/lib/mapa/carregar-pontos";
import type { PontoMapa } from "@/lib/mapa/pontos";

function Carregando() {
  return (
    <div className="h-105 flex items-center justify-center bg-primary-light/40 rounded-xl">
      <Loader2 className="w-6 h-6 animate-spin text-primary" role="img" aria-label="Carregando mapa" />
    </div>
  );
}

// ssr: false porque o Leaflet usa window/document, que não existem no servidor
const MapaSementes = dynamic(() => import("@/components/mapa-sementes"), {
  ssr: false,
  loading: Carregando,
});

/**
 * Mapa das comunidades no fim da home (RF-08), só o mapa (a lista fica em /mapa).
 *
 * Só carrega quando a seção chega perto da tela: quem não rola até aqui não
 * baixa o Leaflet, os pontos nem as imagens do OpenStreetMap.
 */
export default function SecaoMapa() {
  const secao = useRef<HTMLElement>(null);
  const [visivel, setVisivel] = useState(false);
  const [pontos, setPontos] = useState<PontoMapa[] | null>(null);
  const [erro, setErro] = useState(false);

  useEffect(() => {
    if (!secao.current) return;
    const observador = new IntersectionObserver(
      ([entrada]) => {
        if (!entrada.isIntersecting) return;
        observador.disconnect();
        setVisivel(true);
        carregarPontos()
          .then(setPontos)
          .catch(() => setErro(true));
      },
      { rootMargin: "300px" }
    );
    observador.observe(secao.current);
    return () => observador.disconnect();
  }, []);

  return (
    <section ref={secao} id="mapa" aria-labelledby="titulo-mapa" className="py-14 px-6">
      <div className="max-w-7xl mx-auto space-y-6">
        <div>
          <h2 id="titulo-mapa" className="text-2xl font-bold text-foreground mb-1">
            Onde estão as comunidades
          </h2>
          <AvisoMapa className="text-sm" />
        </div>

        {erro ? (
          <p role="alert" className="text-sm text-red-700">
            Não foi possível carregar o mapa. Tente novamente em instantes.
          </p>
        ) : !visivel || pontos === null ? (
          <Carregando />
        ) : (
          <MapaSementes pontos={pontos} altura="420px" />
        )}
      </div>
    </section>
  );
}
