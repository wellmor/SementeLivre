"use client";

import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

import type { PontoMapa, ProdutoNoMapa } from "@/lib/mapa/pontos";

/** Centro do Brasil, para quando não há ponto nenhum. */
const CENTRO_DO_BRASIL: L.LatLngTuple = [-15.78, -47.93];

/** Miniaturas no balão; acima disso aparece "+N". A lista completa fica na página /mapa. */
const FOTOS_NO_BALAO = 8;
const TAMANHO_FOTO = "52px";

interface MapaSementesProps {
  pontos: PontoMapa[];
  /** Altura em CSS (padrão: 500px). */
  altura?: string;
  /** Comunidade cujo balão abre sozinho (botão "Ver no mapa" do card). */
  destaque?: string;
}

/** Quadrado do tamanho da miniatura, para a foto ou para o "sem foto" / "+N". */
function quadrado(): HTMLElement {
  const caixa = document.createElement("span");
  Object.assign(caixa.style, {
    width: TAMANHO_FOTO,
    height: TAMANHO_FOTO,
    borderRadius: "8px",
    overflow: "hidden",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "#dcfce7",
    color: "#166534",
    fontSize: "13px",
    fontWeight: "600",
  });
  return caixa;
}

/** Miniatura de um produto; sem foto, ou se ela não carregar, mostra a muda. */
function miniatura(produto: ProdutoNoMapa): HTMLElement {
  const caixa = quadrado();
  caixa.title = produto.nome;

  const semFoto = () => {
    caixa.replaceChildren("🌱");
    caixa.setAttribute("role", "img");
    caixa.setAttribute("aria-label", produto.nome);
  };

  if (!produto.urlFoto) {
    semFoto();
    return caixa;
  }

  const foto = document.createElement("img");
  foto.src = produto.urlFoto;
  foto.alt = produto.nome;
  foto.loading = "lazy";
  Object.assign(foto.style, { width: "100%", height: "100%", objectFit: "cover" });
  foto.onerror = semFoto;
  caixa.append(foto);
  return caixa;
}

/**
 * Balão do pino. Montado com nós DOM, textContent e atributos, NUNCA com HTML
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
  raiz.append(regiao);

  const total = ponto.produtos.length;
  const resumo = document.createElement("div");
  Object.assign(resumo.style, { color: "#475569", margin: "6px 0 4px" });
  resumo.textContent = `${total} ${total === 1 ? "produto" : "produtos"}`;
  raiz.append(resumo);

  const grade = document.createElement("div");
  Object.assign(grade.style, {
    display: "grid",
    gridTemplateColumns: `repeat(4, ${TAMANHO_FOTO})`,
    gap: "6px",
  });
  // Com mais produtos que cabem, a última casa vira "+N"
  const cabem = total > FOTOS_NO_BALAO ? FOTOS_NO_BALAO - 1 : total;
  grade.append(...ponto.produtos.slice(0, cabem).map(miniatura));
  if (total > cabem) {
    const resto = quadrado();
    resto.textContent = `+${total - cabem}`;
    resto.title = `Mais ${total - cabem} produtos`;
    grade.append(resto);
  }
  raiz.append(grade);

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
