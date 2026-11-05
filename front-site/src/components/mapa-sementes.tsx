"use client";

import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

import type { PontoMapa } from "@/lib/mapa/pontos";

/** Centro do Brasil, para quando não há ponto nenhum. */
const CENTRO_DO_BRASIL: L.LatLngTuple = [-15.78, -47.93];

/** Acima disso o balão mostra "e mais N"; a lista completa fica na página /mapa. */
const PRODUTOS_NO_BALAO = 5;

interface MapaSementesProps {
  pontos: PontoMapa[];
  /** Altura em CSS (padrão: 500px). */
  altura?: string;
  /** Comunidade cujo balão abre sozinho (botão "Ver no mapa" do card). */
  destaque?: string;
}

/**
 * Balão do pino. Montado com nós DOM e textContent, NUNCA com HTML
 * concatenado: nome da comunidade e dos produtos vêm do cadastro feito no app,
 * e um nome como `<img src=x onerror=...>` virava script no site público.
 */
function balao(ponto: PontoMapa): HTMLElement {
  const raiz = document.createElement("div");

  const titulo = document.createElement("strong");
  titulo.textContent = ponto.comunidade;
  raiz.append(titulo, document.createElement("br"));

  const regiao = document.createElement("span");
  regiao.textContent =
    ponto.precisao === "municipio" ? `Região de ${ponto.localizacao}` : `Estado: ${ponto.localizacao}`;
  raiz.append(regiao, document.createElement("br"));

  const produtos = document.createElement("span");
  produtos.style.color = "#475569";
  const total = ponto.produtos.length;
  const primeiros = ponto.produtos.slice(0, PRODUTOS_NO_BALAO).join(", ");
  produtos.textContent =
    total > PRODUTOS_NO_BALAO
      ? `${total} produtos: ${primeiros} e mais ${total - PRODUTOS_NO_BALAO}`
      : `${total} ${total === 1 ? "produto" : "produtos"}: ${primeiros}`;
  raiz.append(produtos);

  return raiz;
}

/**
 * Mapa de sementes (RF-08), com Leaflet + OpenStreetMap: sem chave de API nem
 * conta de faturamento.
 *
 * O pino é um círculo na sede do município da comunidade: a localização é
 * aproximada de propósito (ver lib/mapa/coordenadas.ts).
 *
 * O mapa é criado direto pelo Leaflet num useEffect (e não pelo react-leaflet)
 * para controlar a criação e a destruição: no modo estrito do React o
 * componente monta duas vezes e o Leaflet reclamaria que o elemento "já está
 * em uso".
 */
export default function MapaSementes({ pontos, altura = "500px", destaque }: MapaSementesProps) {
  const elemento = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!elemento.current) return;

    const mapa = L.map(elemento.current, { scrollWheelZoom: false });

    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 12, // mais perto que isso não acrescenta nada a um ponto aproximado
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(mapa);

    const marcadores = pontos.map((ponto) =>
      L.circleMarker([ponto.latitude, ponto.longitude], {
        radius: 8 + Math.min(ponto.produtos.length, 10),
        color: "#15803d",
        fillColor: "#22c55e",
        fillOpacity: 0.6,
      })
        .addTo(mapa)
        .bindPopup(() => balao(ponto))
    );

    const emFoco = pontos.findIndex((p) => p.comunidadeId === destaque);
    if (emFoco >= 0) {
      mapa.setView([pontos[emFoco].latitude, pontos[emFoco].longitude], 9);
      marcadores[emFoco].openPopup();
    } else if (pontos.length > 1) {
      mapa.fitBounds(L.latLngBounds(pontos.map((p) => [p.latitude, p.longitude])), {
        padding: [40, 40],
        maxZoom: 9,
      });
    } else if (pontos.length === 1) {
      mapa.setView([pontos[0].latitude, pontos[0].longitude], 9);
    } else {
      mapa.setView(CENTRO_DO_BRASIL, 4);
    }

    return () => {
      mapa.remove();
    };
  }, [pontos, destaque]);

  return (
    <div
      ref={elemento}
      role="region"
      aria-label="Mapa das comunidades com bancos de sementes"
      className="z-0 w-full rounded-xl border border-border"
      style={{ height: altura }}
    />
  );
}
