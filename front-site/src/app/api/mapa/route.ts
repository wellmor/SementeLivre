import { backendIndisponivel } from "@/lib/backend";
import { buscarCatalogoCompleto } from "@/lib/catalogo-servidor";
import { montarPontos } from "@/lib/mapa/montar-pontos";

// Pontos do mapa de sementes (RF-08): um por comunidade, na sede do município.
// A tabela de coordenadas fica no servidor; o navegador só recebe os pontos.
export async function GET() {
  const ofertas = await buscarCatalogoCompleto();
  return ofertas ? Response.json(montarPontos(ofertas)) : backendIndisponivel();
}
