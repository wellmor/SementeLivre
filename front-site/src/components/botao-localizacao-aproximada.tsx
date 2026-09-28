"use client";

import { useState } from "react";
import dynamic from "next/dynamic";
import { MapPin, Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { PontoMapa } from "@/lib/mapa/pontos";

// ssr: false porque o Leaflet depende de window/document.
const MapaSementes = dynamic(() => import("@/components/mapa-sementes"), {
  ssr: false,
  loading: () => (
    <div className="h-[320px] flex items-center justify-center bg-green-50 rounded-xl">
      <Loader2 className="w-5 h-5 animate-spin text-green-600" />
    </div>
  ),
});

interface BotaoLocalizacaoAproximadaProps {
  /** Ponto da semente; quando null o botao nao aparece. */
  ponto: PontoMapa | null;
  nomeSemente: string;
}

/**
 * Botao "Localizacao" que aparece no card da semente (RF-08).
 *
 * Abre uma janela com o mapa centrado na regiao aproximada de quem cadastrou a
 * semente. O mapa so carrega quando o usuario clica: assim a home nao fica
 * pesada com um mapa por card.
 */
export default function BotaoLocalizacaoAproximada({
  ponto,
  nomeSemente,
}: BotaoLocalizacaoAproximadaProps) {

  const [aberto, setAberto] = useState(false);

  // Sem coordenada conhecida nao mostramos o botao, para nao abrir um mapa vazio
  if (!ponto) {
    return null;
  }

  return (
    <>
      <Button
        size="sm"
        variant="outline"
        className="mt-2 w-full border-green-200 text-green-700 hover:bg-green-50 gap-1.5 text-xs h-8"
        onClick={() => setAberto(true)}
      >
        <MapPin className="w-3.5 h-3.5" />
        Localizacao
      </Button>

      <Dialog open={aberto} onOpenChange={setAberto}>
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>{nomeSemente}</DialogTitle>
            <DialogDescription>
              Regiao aproximada: {ponto.localizacao}. O mapa mostra a comunidade,
              e nao o endereco da propriedade.
            </DialogDescription>
          </DialogHeader>

          {aberto && <MapaSementes pontos={[ponto]} altura="320px" />}
        </DialogContent>
      </Dialog>
    </>
  );
}
