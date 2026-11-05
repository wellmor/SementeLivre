// Só no servidor: depende da tabela de coordenadas, que não vai para o navegador.
import type { OfertaCatalogo } from "../types";
import { coordenadaDe } from "./coordenadas";
import type { PontoMapa, ProdutoNoMapa } from "./pontos";

/**
 * Agrupa as ofertas do catálogo público em um ponto por comunidade.
 *
 * O catálogo já só traz ofertas disponíveis de comunidades ativas, então toda
 * comunidade que aparece aqui tem banco ativo. Oferta sem comunidade, ou com
 * comunidade sem UF reconhecida, fica de fora: melhor sem pino que com pino errado.
 */
export function montarPontos(ofertas: OfertaCatalogo[]): PontoMapa[] {
  // Por comunidade: a primeira oferta (para município/UF) e os produtos por nome
  const porComunidade = new Map<string, { oferta: OfertaCatalogo; produtos: Map<string, ProdutoNoMapa> }>();

  for (const oferta of ofertas) {
    if (!oferta.comunidadeId || !oferta.comunidade) continue;
    let grupo = porComunidade.get(oferta.comunidadeId);
    if (!grupo) {
      grupo = { oferta, produtos: new Map() };
      porComunidade.set(oferta.comunidadeId, grupo);
    }
    // Mesmo produto em mais de uma oferta: fica uma vez, com a primeira foto que tiver
    const atual = grupo.produtos.get(oferta.nomePopular);
    if (!atual || (!atual.urlFoto && oferta.urlFoto)) {
      grupo.produtos.set(oferta.nomePopular, { nome: oferta.nomePopular, urlFoto: oferta.urlFoto });
    }
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
      produtos: [...produtos.values()].sort((a, b) => a.nome.localeCompare(b.nome, "pt-BR")),
    });
  }

  return pontos.sort((a, b) => a.comunidade.localeCompare(b.comunidade, "pt-BR"));
}
