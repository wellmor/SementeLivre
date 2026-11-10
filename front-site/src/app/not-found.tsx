import type { Metadata } from "next";
import Link from "next/link";
import { ArrowRight, MapPin, Sprout } from "lucide-react";

import PublicHeader from "@/components/public-header";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export const metadata: Metadata = {
  title: "Página não encontrada | Semente Livre",
};

/** 404 do site: qualquer endereço que não existe cai aqui (Next devolve status 404). */
export default function NaoEncontrada() {
  return (
    <div className="min-h-screen bg-background flex flex-col">
      <PublicHeader />

      <main className="relative flex-1 overflow-hidden bg-gradient-to-br from-primary to-primary-dark text-white">
        {/* Mesmos círculos do topo da home */}
        <div className="absolute -top-20 -right-20 w-96 h-96 rounded-full bg-white/10" aria-hidden="true" />
        <div className="absolute -bottom-24 -left-16 w-80 h-80 rounded-full bg-primary-dark/30" aria-hidden="true" />

        <div className="relative max-w-2xl mx-auto px-6 py-20 sm:py-28 flex flex-col items-center text-center gap-6">
          <div className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center">
            <Sprout className="w-8 h-8 text-white" aria-hidden="true" />
          </div>

          <p className="text-7xl sm:text-8xl font-extrabold tracking-tight leading-none" aria-hidden="true">
            404
          </p>

          <div className="space-y-3">
            <h1 className="text-3xl sm:text-4xl font-bold tracking-tight">Página não encontrada</h1>
            <p className="text-primary-light text-lg leading-relaxed max-w-md mx-auto">
              Esta semente ainda não brotou por aqui. O endereço pode estar errado ou a página pode
              ter mudado de lugar.
            </p>
          </div>

          <div className="flex flex-col sm:flex-row gap-3 mt-2 w-full sm:w-auto">
            <Link
              href="/#produtos"
              className={cn(
                buttonVariants({ size: "lg" }),
                "bg-white text-primary-dark hover:bg-primary-light font-semibold gap-2 shadow-lg px-5"
              )}
            >
              Ver produtos disponíveis
              <ArrowRight className="w-4 h-4" aria-hidden="true" />
            </Link>
            <Link
              href="/mapa"
              className={cn(
                buttonVariants({ size: "lg", variant: "outline" }),
                "border-white/60 bg-transparent text-white hover:bg-white/10 hover:text-white font-semibold gap-2 px-5"
              )}
            >
              <MapPin className="w-4 h-4" aria-hidden="true" />
              Abrir o mapa
            </Link>
          </div>
        </div>
      </main>

      <footer className="bg-foreground text-white/70 text-xs py-5 text-center">
        © {new Date().getFullYear()} Semente Livre. Todos os direitos reservados.
      </footer>
    </div>
  );
}
