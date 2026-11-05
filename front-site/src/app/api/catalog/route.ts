import { backendIndisponivel } from "@/lib/backend";
import { buscarCatalogoCompleto } from "@/lib/catalogo-servidor";

// Catálogo público do backend, com todas as páginas.
export async function GET() {
  const ofertas = await buscarCatalogoCompleto();
  return ofertas ? Response.json(ofertas) : backendIndisponivel();
}
