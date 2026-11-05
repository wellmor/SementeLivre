// Só no servidor: depende da tabela de coordenadas, que não vai para o navegador.
import type { OfertaCatalogo } from "../types";
import { coordenadaDe } from "./coordenadas";
import type { PontoMapa } from "./pontos";

/**
 * Agrupa as ofertas do catálogo público em um ponto por comunidade.
 *
 * O catálogo já só traz ofertas disponíveis de comunidades ativas, então toda
 * comunidade que aparece aqui tem banco ativo. Oferta sem comunidade, ou com
 * comunidade sem UF reconhecida, fica de fora: melhor sem pino que com pino errado.
 */
export function montarPontos(ofertas: OfertaCatalogo[]): PontoMapa[] {
  const porComunidade = new Map<string, { oferta: OfertaCatalogo; produtos: Set<string> }>();

  for (const oferta of ofertas) {
    if (!oferta.comunidadeId || !oferta.comunidade) continue;
    const grupo = porComunidade.get(oferta.comunidadeId);
    if (grupo) grupo.produtos.add(oferta.nomePopular);
    else porComunidade.set(oferta.comunidadeId, { oferta, produtos: new Set([oferta.nomePopular]) });
  }

  const pontos: PontoMapa[] = [];
  for (const [comunidadeId, { oferta, produtos }] of porComunidade) {
    const coordenada = coordenadaDe(oferta.municipio, oferta.uf);
    if (!coordenada) continue;

    const uf = oferta.uf!.trim().toUpperCase();
    pontos.push({
      comunidadeId,
      comunidade: oferta.comunidade!,
      localizacao: coordenada.precisao === "municipio" ? `${oferta.municipio!.trim()} - ${uf}` : uf,
      ...coordenada,
      produtos: [...produtos].sort((a, b) => a.localeCompare(b, "pt-BR")),
    });
  }

  return pontos.sort((a, b) => a.comunidade.localeCompare(b.comunidade, "pt-BR"));
}
