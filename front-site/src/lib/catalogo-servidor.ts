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
      // sort=id: sem ordem fixa o Postgres devolve as páginas em ordem instável, e
      // ofertas apareciam duas vezes enquanto outras sumiam (com mais de 100 ofertas)
      const res = await fetch(`${BACKEND_URL}/catalogo/produtos?size=${POR_PAGINA}&page=${pagina}&sort=id,asc`, {
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
