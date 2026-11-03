import { BACKEND_URL, backendIndisponivel } from "@/lib/backend";

// Catálogo público do backend. O site filtra no navegador, então busca todas as
// páginas: com uma página só (size=200), as ofertas além da 200ª sumiam do site.
const POR_PAGINA = 100;
const MAX_PAGINAS = 50;

export async function GET() {
  try {
    const ofertas: unknown[] = [];
    for (let pagina = 0; pagina < MAX_PAGINAS; pagina++) {
      const res = await fetch(`${BACKEND_URL}/catalogo/produtos?size=${POR_PAGINA}&page=${pagina}`, {
        cache: "no-store",
      });
      if (!res.ok) return backendIndisponivel();
      const corpo = await res.json();
      ofertas.push(...(corpo.content ?? []));
      if (corpo.last !== false) break;
    }
    return Response.json(ofertas);
  } catch {
    return backendIndisponivel();
  }
}
