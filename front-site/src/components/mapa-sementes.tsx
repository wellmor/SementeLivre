"use client";

import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

import { CENTRO_PADRAO } from "@/lib/mapa/municipios";
import type { PontoMapa } from "@/lib/mapa/pontos";

interface MapaSementesProps {
  pontos: PontoMapa[];
  /** Altura em CSS (padrao: 500px). */
  altura?: string;
}

/**
 * Mapa de sementes (RF-08).
 *
 * Usa Leaflet com OpenStreetMap: nao precisa de chave de API nem de conta de
 * faturamento. Os pinos sao circulos na coordenada da comunidade, porque a
 * localizacao e aproximada de proposito (ver lib/mapa/municipios.ts).
 *
 * O componente nao busca dados: recebe os pontos prontos. Assim, quem for
 * integrar com o backend so precisa trocar quem monta a lista de pontos, sem
 * mexer aqui.
 *
 * O mapa e criado direto pelo Leaflet dentro de um useEffect, e nao pelo
 * react-leaflet, porque assim controlamos a criacao e a destruicao do mapa.
 * Sem isso, o modo estrito do React (que monta o componente duas vezes em
 * desenvolvimento) faz o Leaflet reclamar que o elemento "ja esta em uso".
 */
export default function MapaSementes({ pontos, altura = "500px" }: MapaSementesProps) {

  const elemento = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!elemento.current) {
      return;
    }

    const centro = pontos.length > 0 ? pontos[0] : CENTRO_PADRAO;

    // Com varios pontos o mapa abre mais afastado, para caber todo mundo
    const zoom = pontos.length > 1 ? 4 : 6;

    const mapa = L.map(elemento.current).setView(
      [centro.latitude, centro.longitude],
      zoom
    );

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(mapa);

    for (const ponto of pontos) {
      L.circleMarker([ponto.latitude, ponto.longitude], {
        radius: 10 + Math.min(ponto.quantidadeSementes, 10),
        color: "#16a34a",
        fillColor: "#22c55e",
        fillOpacity: 0.6,
      })
        .addTo(mapa)
        .bindPopup(
          `<strong>${ponto.nome}</strong><br/>` +
            `${ponto.localizacao}<br/>` +
            `${ponto.quantidadeSementes} semente(s) disponivel(is)<br/>` +
            `<span style="color:#4b5563">${ponto.sementes.join(", ")}</span>`
        );
    }

    // Destroi o mapa quando o componente sai da tela, liberando o elemento
    return () => {
      mapa.remove();
    };
  }, [pontos]);

  return (
    <div
      ref={elemento}
      style={{ height: altura, width: "100%", borderRadius: "0.75rem" }}
    />
  );
}
