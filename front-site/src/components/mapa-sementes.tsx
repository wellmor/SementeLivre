"use client";

import { MapContainer, TileLayer, CircleMarker, Popup } from "react-leaflet";
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
 */
export default function MapaSementes({ pontos, altura = "500px" }: MapaSementesProps) {
  const centro = pontos.length > 0
    ? { latitude: pontos[0].latitude, longitude: pontos[0].longitude }
    : CENTRO_PADRAO;

  // Com poucos pontos o mapa pode abrir muito fechado; 4 mostra boa parte do Brasil.
  const zoomInicial = pontos.length > 1 ? 4 : 6;

  return (
    <MapContainer
      center={[centro.latitude, centro.longitude]}
      zoom={zoomInicial}
      scrollWheelZoom={false}
      style={{ height: altura, width: "100%", borderRadius: "0.75rem" }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {pontos.map((ponto) => (
        <CircleMarker
          key={ponto.id}
          center={[ponto.latitude, ponto.longitude]}
          radius={10 + Math.min(ponto.quantidadeSementes, 10)}
          pathOptions={{ color: "#16a34a", fillColor: "#22c55e", fillOpacity: 0.6 }}
        >
          <Popup>
            <strong>{ponto.nome}</strong>
            <br />
            {ponto.localizacao}
            <br />
            {ponto.quantidadeSementes} semente(s) disponivel(is)
            <br />
            <span style={{ color: "#4b5563" }}>{ponto.sementes.join(", ")}</span>
          </Popup>
        </CircleMarker>
      ))}
    </MapContainer>
  );
}
