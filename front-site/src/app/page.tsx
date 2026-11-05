"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "react-toastify";
import PublicHeader from "@/components/public-header";
import FotoProduto from "@/components/foto-produto";
import CarrosselComunidades from "@/components/carrossel-comunidades";
import BotaoVerNoMapa from "@/components/botao-ver-no-mapa";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Lock,
  Loader2,
  Search,
  Leaf,
  FlaskConical,
  BookOpen,
  Sprout,
  ArrowRight,
  MessageCircle,
} from "lucide-react";
import type { Disponibilidade, OfertaCatalogo, TipoProduto } from "@/lib/types";
import { DISPONIBILIDADE_CONFIG, TIPO_PRODUTO_LABEL, unidade } from "@/lib/catalogo";
import { formatarMoeda } from "@/lib/utils";
import { FOTOS_COMUNIDADES } from "@/lib/divulgacao";

type FilterStatus = Disponibilidade | "all";
type FilterTipo = TipoProduto | "all";

// Cards por vez na listagem; o resto aparece no "Mostrar mais"
const POR_PAGINA = 24;

export default function MarketplacePage() {
  const router = useRouter();
  const [ofertas, setOfertas] = useState<OfertaCatalogo[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<FilterStatus>("all");
  const [filterTipo, setFilterTipo] = useState<FilterTipo>("all");
  const [filterComunidade, setFilterComunidade] = useState("all");
  // Cards mostrados aos poucos ("Mostrar mais"); a contagem volta ao início quando o filtro muda
  const chaveDoFiltro = JSON.stringify([search, filter, filterTipo, filterComunidade]);
  const [exibicao, setExibicao] = useState({ chave: chaveDoFiltro, quantidade: POR_PAGINA });
  const quantidadeVisivel = exibicao.chave === chaveDoFiltro ? exibicao.quantidade : POR_PAGINA;

  useEffect(() => {
    fetch("/api/catalog")
      .then((r) => {
        if (!r.ok) throw new Error();
        return r.json();
      })
      .then((lista: OfertaCatalogo[]) =>
        setOfertas([...lista].sort((a, b) => a.nomePopular.localeCompare(b.nomePopular, "pt-BR")))
      )
      .catch(() => toast.error("Erro ao carregar produtos."))
      .finally(() => setLoading(false));
  }, []);

  // Só as comunidades que têm alguma oferta no catálogo
  const comunidades = Array.from(
    new Map(
      ofertas
        .filter((o) => o.comunidadeId && o.comunidade)
        .map((o) => [o.comunidadeId as string, o.comunidade as string])
    )
  ).sort((a, b) => a[1].localeCompare(b[1], "pt-BR"));

  const filtered = ofertas.filter((o) => {
    const matchSearch = o.nomePopular
      .toLowerCase()
      .includes(search.toLowerCase());
    const matchFilter = filter === "all" || o.disponibilidade === filter;
    const matchTipo = filterTipo === "all" || o.tipo === filterTipo;
    const matchComunidade =
      filterComunidade === "all" || o.comunidadeId === filterComunidade;
    return matchSearch && matchFilter && matchTipo && matchComunidade;
  });

  const stats = {
    total: ofertas.length,
    sale: ofertas.filter((o) => o.disponibilidade === "PARA_VENDA").length,
    exchange: ofertas.filter((o) => o.disponibilidade === "PARA_TROCA").length,
    donation: ofertas.filter((o) => o.disponibilidade === "PARA_DOACAO").length,
  };

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <PublicHeader />

      {/* ── Hero ── */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary to-primary-dark text-white">
        {/* Blobs */}
        <div className="absolute -top-20 -right-20 w-96 h-96 rounded-full bg-white/10" />
        <div className="absolute -bottom-24 -left-16 w-80 h-80 rounded-full bg-primary-dark/30" />

        <div className="relative max-w-7xl mx-auto px-6 py-20 flex flex-col items-center text-center gap-6">
          <div className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center">
            <Sprout className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight max-w-2xl leading-tight">
            Banco Comunitário de Sementes
          </h1>
          <p className="text-primary-light text-lg max-w-xl leading-relaxed">
            Variedades crioulas e orgânicas cultivadas por{" "}
            <strong className="text-white">comunidades quilombolas</strong>.
            Preservando a biodiversidade, cultivando o futuro.
          </p>

          {/* Stats row */}
          <div className="flex gap-4 flex-wrap justify-center mt-2">
            <StatPill value={stats.total} label="Produtos" />
            <StatPill value={stats.sale} label="À Venda" />
            <StatPill value={stats.exchange} label="Para Troca" />
            <StatPill value={stats.donation} label="Para Doação" />
          </div>

          <div className="flex flex-col sm:flex-row gap-3 mt-2">
            <Button
              size="lg"
              className="bg-white text-primary-dark hover:bg-primary-light font-semibold gap-2 shadow-lg"
              onClick={() =>
                document
                  .getElementById("produtos")
                  ?.scrollIntoView({ behavior: "smooth" })
              }
            >
              Ver produtos disponíveis
              <ArrowRight className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </section>

      {/* ── Seed catalog ── */}
      <section
        id="produtos"
        className="flex-1 max-w-7xl mx-auto w-full px-6 py-14"
      >
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-foreground mb-1">
            Produtos Disponíveis
          </h2>
          <p className="text-muted-foreground text-sm" role="status">
            {filtered.length} resultado{filtered.length !== 1 ? "s" : ""}
          </p>
        </div>

        {/* Search + filters */}
        <div className="flex flex-col gap-3 mb-8">
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" aria-hidden="true" />
              <Input
                type="search"
                aria-label="Buscar por nome popular"
                placeholder="Buscar por nome popular..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="pl-9 border-border focus-visible:ring-ring"
              />
            </div>

            <select
              aria-label="Filtrar por comunidade"
              value={filterComunidade}
              onChange={(e) => setFilterComunidade(e.target.value)}
              className="rounded-md border border-border bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
            >
              <option value="all">Todas as comunidades</option>
              {comunidades.map(([id, nome]) => (
                <option key={id} value={id}>
                  {nome}
                </option>
              ))}
            </select>
          </div>

          {/* Status pills */}
          <div className="flex gap-2 flex-wrap" role="group" aria-label="Filtrar por disponibilidade">
            {(["all", ...Object.keys(DISPONIBILIDADE_CONFIG)] as FilterStatus[]).map((f) => {
              const label =
                f === "all"
                  ? "Todos"
                  : DISPONIBILIDADE_CONFIG[f as Disponibilidade].label;
              return (
                <button
                  key={f}
                  type="button"
                  aria-pressed={filter === f}
                  onClick={() => setFilter(f)}
                  className={`px-3 py-1.5 rounded-full text-xs font-medium border transition-all ${
                    filter === f
                      ? "bg-primary text-white border-primary shadow-sm"
                      : "bg-white text-slate-600 border-border hover:border-primary hover:text-primary-dark"
                  }`}
                >
                  {label}
                </button>
              );
            })}
          </div>

          {/* Tipo pills */}
          <div className="flex gap-2 flex-wrap" role="group" aria-label="Filtrar por tipo">
            {(["all", ...Object.keys(TIPO_PRODUTO_LABEL)] as FilterTipo[]).map((t) => {
              const label = t === "all" ? "Todos os tipos" : TIPO_PRODUTO_LABEL[t as TipoProduto];
              return (
                <button
                  key={t}
                  type="button"
                  aria-pressed={filterTipo === t}
                  onClick={() => setFilterTipo(t)}
                  className={`px-3 py-1.5 rounded-full text-xs font-medium border transition-all ${
                    filterTipo === t
                      ? "bg-primary-dark text-white border-primary-dark shadow-sm"
                      : "bg-white text-slate-600 border-border hover:border-primary hover:text-primary-dark"
                  }`}
                >
                  {label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Loading */}
        {loading && (
          <div className="flex justify-center py-20">
            <Loader2 className="w-8 h-8 text-primary animate-spin" role="img" aria-label="Carregando produtos" />
          </div>
        )}

        {/* Empty state */}
        {!loading && filtered.length === 0 && (
          <div className="flex flex-col items-center justify-center h-52 bg-white rounded-2xl border border-dashed border-border gap-3">
            <Leaf className="w-10 h-10 text-primary/30" />
            <p className="text-muted-foreground text-sm">
              {search
                ? `Nenhum produto encontrado para "${search}".`
                : "Nenhum produto no catálogo ainda."}
            </p>
          </div>
        )}

        {/* Grid */}
        {!loading && filtered.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-5">
            {filtered.slice(0, quantidadeVisivel).map((oferta) => {
              const cfg = DISPONIBILIDADE_CONFIG[oferta.disponibilidade];
              const descricao = oferta.descricao || oferta.historico;

              return (
                <Card
                  key={oferta.estoqueId}
                  className="group flex flex-col border-border bg-white hover:shadow-lg transition-all"
                >
                  {/* Photo */}
                  <div className="h-40 rounded-t-xl bg-gradient-to-br from-primary-light/40 to-primary-light flex items-center justify-center overflow-hidden">
                    <FotoProduto
                      src={oferta.urlFoto}
                      alt={oferta.nomePopular}
                      iconClassName="w-14 h-14 text-primary/40"
                    />
                  </div>

                  <CardHeader className="pb-1 pt-4">
                    <div className="flex items-start justify-between gap-2">
                      <CardTitle className="text-sm leading-tight">
                        {oferta.nomePopular}
                      </CardTitle>
                      <Badge
                        variant="outline"
                        className={`text-[11px] font-medium flex-shrink-0 ${cfg.className}`}
                      >
                        {cfg.label}
                      </Badge>
                    </div>
                    {oferta.comunidade && (
                      <div className="flex flex-wrap items-center justify-between gap-x-2 gap-y-0.5 mt-1">
                        <p className="text-[11px] text-primary font-medium">
                          🌿 {oferta.comunidade}
                        </p>
                        {/* Região aproximada da comunidade (RF-08) */}
                        {oferta.comunidadeId && oferta.uf && (
                          <BotaoVerNoMapa
                            comunidadeId={oferta.comunidadeId}
                            comunidade={oferta.comunidade}
                          />
                        )}
                      </div>
                    )}
                  </CardHeader>

                  <CardContent className="pb-4 space-y-1.5 flex-1 flex flex-col justify-between">
                    <div className="space-y-1.5">
                      <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                        <FlaskConical className="w-3 h-3" />
                        <span className="italic">
                          {oferta.nomeCientifico || "—"}
                        </span>
                      </div>
                      <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                        <BookOpen className="w-3 h-3" />
                        <span>{oferta.familiaBotanica || "—"}</span>
                      </div>
                      {oferta.quantidade != null && (
                        <p className="text-xs text-slate-600 font-medium">
                          📦 {oferta.quantidade} {unidade(oferta.tipoPesagem)} em estoque
                        </p>
                      )}
                      {oferta.preco != null && (
                        <p className="text-xs text-primary-dark font-semibold">
                          {formatarMoeda(oferta.preco)}/{unidade(oferta.tipoPesagem)}
                        </p>
                      )}
                      {descricao && (
                        <p className="text-xs text-muted-foreground leading-relaxed line-clamp-3 pt-1">
                          {descricao}
                        </p>
                      )}
                      {/* "A Negociar" sempre aceita proposta: o aviso de tipo fixo não vale */}
                      {!oferta.aceitaNegociacao && oferta.disponibilidade !== "A_NEGOCIAR" && (
                        <p className="flex items-center gap-1 text-[11px] text-muted-foreground pt-1">
                          <Lock className="w-3 h-3" />
                          Só {cfg.label.replace("Para ", "").toLowerCase()}, sem negociação
                        </p>
                      )}
                    </div>

                    <Button
                      size="sm"
                      variant="outline"
                      className="mt-3 w-full border-border text-primary-dark hover:bg-primary-light gap-1.5 text-xs h-8 opacity-0 group-hover:opacity-100 focus-visible:opacity-100 group-focus-within:opacity-100 transition-opacity"
                      onClick={() =>
                        router.push(`/produto/${oferta.id}?oferta=${oferta.estoqueId}`)
                      }
                    >
                      <MessageCircle className="w-3.5 h-3.5" />
                      {oferta.aceitaNegociacao || oferta.disponibilidade === "A_NEGOCIAR" ? "Quero negociar" : "Fazer pedido"}
                    </Button>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}

        {!loading && filtered.length > quantidadeVisivel && (
          <div className="mt-8 flex justify-center">
            <Button
              variant="outline"
              className="border-border text-primary-dark hover:bg-primary-light"
              onClick={() => setExibicao({ chave: chaveDoFiltro, quantidade: quantidadeVisivel + POR_PAGINA })}
            >
              Mostrar mais ({filtered.length - quantidadeVisivel} restantes)
            </Button>
          </div>
        )}
      </section>

      {/* ── Comunidades (#181) ── */}
      <section id="comunidades" aria-labelledby="titulo-comunidades" className="bg-white border-y border-border py-14 px-6">
        <div className="max-w-7xl mx-auto space-y-6">
          <div>
            <h2 id="titulo-comunidades" className="text-2xl font-bold text-foreground mb-1">
              Conheça as comunidades
            </h2>
            <p className="text-muted-foreground text-sm">
              Famílias que guardam, plantam e multiplicam as variedades da rede.
            </p>
          </div>
          <CarrosselComunidades fotos={FOTOS_COMUNIDADES} />
        </div>
      </section>

      {/* ── About section ── */}
      <section
        id="sobre"
        className="bg-primary-dark text-white py-16 px-6"
      >
        <div className="max-w-3xl mx-auto text-center space-y-5">
          <div className="text-5xl">🌿</div>
          <h2 className="text-3xl font-bold">Rede de Quilombos</h2>
          <p className="text-primary-light text-base leading-relaxed">
            Comunidades quilombolas unidas pela preservação das sementes crioulas
            e pelo fortalecimento da agricultura familiar. Nosso banco comunitário
            de sementes reúne variedades cultivadas há gerações, adaptadas ao
            nosso clima e nossa cultura.
          </p>
          <p className="text-white/80 text-sm">
            Para negociações e parcerias, entre em contato diretamente com
            nossos produtores.
          </p>
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className="bg-foreground text-white/70 text-xs py-5 text-center">
        © {new Date().getFullYear()} Semente Livre. Todos os direitos reservados.
      </footer>
    </div>
  );
}

function StatPill({ value, label }: { value: number; label: string }) {
  return (
    <div className="flex items-center gap-2 bg-white/20 backdrop-blur-sm rounded-full px-4 py-1.5 text-sm">
      <span className="font-bold text-white">{value}</span>
      <span className="text-primary-light">{label}</span>
    </div>
  );
}

