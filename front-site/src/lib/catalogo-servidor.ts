// Só no servidor (rotas de /api), como lib/backend.ts.
import { BACKEND_URL } from "./backend";
import type { OfertaCatalogo } from "./types";

// O site filtra no navegador, então busca todas as páginas: com uma página só
// (size=200), as ofertas além da 200ª sumiam do site.
const POR_PAGINA = 100;
const MAX_PAGINAS = 50;

/** Catálogo público inteiro do backend, ou null quando o backend não responde. */
export async function buscarCatalogoCompleto(): Promise<OfertaCatalogo[] | null> {
  try {
    const ofertas: OfertaCatalogo[] = [];
    for (let pagina = 0; pagina < MAX_PAGINAS; pagina++) {
      const res = await fetch(`${BACKEND_URL}/catalogo/produtos?size=${POR_PAGINA}&page=${pagina}`, {
        cache: "no-store",
      });
      if (!res.ok) return null;
      const corpo = await res.json();
      ofertas.push(...(corpo.content ?? []));
      if (corpo.last !== false) break;
    }
    return ofertas;
  } catch {
    return null;
  }
}
