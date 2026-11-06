"use client";

import type { Ref } from "react";
import { Search, User, X } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import type { Disponibilidade, TipoProduto } from "@/lib/types";
import { DISPONIBILIDADE_CONFIG, TIPO_PRODUTO_LABEL } from "@/lib/catalogo";
import {
  PERIODOS,
  PERIODO_LABEL,
  REGIAO_LABEL,
  type FiltrosCatalogo,
  type Periodo,
  type Regiao,
  type opcoesDosFiltros,
} from "@/lib/filtros-catalogo";

type Opcoes = ReturnType<typeof opcoesDosFiltros>;

const CLASSE_SELECT =
  "h-9 w-full min-w-0 rounded-md border border-border bg-white px-3 text-sm text-foreground focus:outline-none focus-visible:ring-2 focus-visible:ring-ring";

interface Props {
  filtros: FiltrosCatalogo;
  opcoes: Opcoes;
  ativo: boolean;
  onChange: (parcial: Partial<FiltrosCatalogo>) => void;
  onLimpar: () => void;
  buscaRef?: Ref<HTMLInputElement>;
}

/** Busca e filtros da listagem pública (RF-04 / CDU-01). */
export default function FiltrosCatalogoPainel({ filtros, opcoes, ativo, onChange, onLimpar, buscaRef }: Props) {
  const municipioSelecionado = filtros.municipio !== "all" && filtros.uf !== "all" ? `${filtros.municipio}|${filtros.uf}` : "all";

  return (
    <form
      role="search"
      aria-label="Filtros do catálogo"
      className="flex flex-col gap-4 mb-8"
      onSubmit={(e) => e.preventDefault()}
    >
      {/* Textos: semente/muda e produtor */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div className="flex flex-col gap-1.5 min-w-0">
          <Label htmlFor="filtro-busca">Nome popular</Label>
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" aria-hidden="true" />
            <Input
              id="filtro-busca"
              ref={buscaRef}
              type="search"
              aria-label="Buscar por nome popular"
              placeholder="Buscar por nome popular..."
              value={filtros.busca}
              onChange={(e) => onChange({ busca: e.target.value })}
              className="h-9 pl-9 bg-white border-border focus-visible:ring-ring"
            />
          </div>
        </div>

        <div className="flex flex-col gap-1.5 min-w-0">
          <Label htmlFor="filtro-produtor">Produtor</Label>
          <div className="relative">
            <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" aria-hidden="true" />
            <Input
              id="filtro-produtor"
              type="search"
              list="produtores-catalogo"
              autoComplete="off"
              aria-label="Buscar por nome do produtor"
              aria-describedby="filtro-produtor-ajuda"
              placeholder="Buscar por nome do produtor..."
              value={filtros.produtor}
              onChange={(e) => onChange({ produtor: e.target.value })}
              className="h-9 pl-9 bg-white border-border focus-visible:ring-ring"
            />
            <datalist id="produtores-catalogo">
              {opcoes.produtores.map((nome) => (
                <option key={nome} value={nome} />
              ))}
            </datalist>
          </div>
          <p id="filtro-produtor-ajuda" className="text-xs text-muted-foreground">
            Só aparecem os produtores que autorizaram mostrar o nome no site.
          </p>
        </div>
      </div>

      {/* Selects: lugar e datas */}
      <div className="grid grid-cols-1 min-[480px]:grid-cols-2 md:grid-cols-3 xl:grid-cols-6 gap-3">
        <CampoSelect id="filtro-comunidade" rotulo="Comunidade">
          <select
            id="filtro-comunidade"
            aria-label="Filtrar por comunidade"
            value={filtros.comunidade}
            onChange={(e) => onChange({ comunidade: e.target.value })}
            className={CLASSE_SELECT}
          >
            <option value="all">Todas as comunidades</option>
            {opcoes.comunidades.map(([id, nome]) => (
              <option key={id} value={id}>
                {nome}
              </option>
            ))}
          </select>
        </CampoSelect>

        <CampoSelect id="filtro-regiao" rotulo="Região">
          <select
            id="filtro-regiao"
            aria-label="Filtrar por região"
            value={filtros.regiao}
            onChange={(e) => onChange({ regiao: e.target.value as Regiao | "all" })}
            className={CLASSE_SELECT}
          >
            <option value="all">Todas as regiões</option>
            {opcoes.regioes.map((r) => (
              <option key={r} value={r}>
                {REGIAO_LABEL[r]}
              </option>
            ))}
          </select>
        </CampoSelect>

        <CampoSelect id="filtro-uf" rotulo="Estado">
          <select
            id="filtro-uf"
            aria-label="Filtrar por estado"
            value={filtros.uf}
            onChange={(e) => onChange({ uf: e.target.value })}
            className={CLASSE_SELECT}
          >
            <option value="all">Todos os estados</option>
            {opcoes.ufs.map((uf) => (
              <option key={uf} value={uf}>
                {uf}
              </option>
            ))}
          </select>
        </CampoSelect>

        <CampoSelect id="filtro-municipio" rotulo="Município">
          <select
            id="filtro-municipio"
            aria-label="Filtrar por município"
            value={municipioSelecionado}
            onChange={(e) => {
              // O valor leva a UF junto ("Rio Pomba|MG"): o estado vem marcado com o município
              const [municipio, uf] = e.target.value === "all" ? ["all", filtros.uf] : e.target.value.split("|");
              onChange({ municipio, uf });
            }}
            className={CLASSE_SELECT}
          >
            <option value="all">Todos os municípios</option>
            {opcoes.municipios.map(({ municipio, uf }) => (
              <option key={`${municipio}|${uf}`} value={`${municipio}|${uf}`}>
                {municipio} - {uf}
              </option>
            ))}
          </select>
        </CampoSelect>

        <CampoSelect id="filtro-incluido" rotulo="Data de inclusão">
          <select
            id="filtro-incluido"
            aria-label="Filtrar por data de inclusão"
            value={filtros.incluido}
            onChange={(e) => onChange({ incluido: e.target.value as Periodo })}
            className={CLASSE_SELECT}
          >
            <option value="">Qualquer data</option>
            {PERIODOS.map((p) => (
              <option key={p} value={p}>
                {PERIODO_LABEL[p]}
              </option>
            ))}
          </select>
        </CampoSelect>

        <CampoSelect id="filtro-alterado" rotulo="Última alteração">
          <select
            id="filtro-alterado"
            aria-label="Filtrar por data da última alteração"
            value={filtros.alterado}
            onChange={(e) => onChange({ alterado: e.target.value as Periodo })}
            className={CLASSE_SELECT}
          >
            <option value="">Qualquer data</option>
            {PERIODOS.map((p) => (
              <option key={p} value={p}>
                {PERIODO_LABEL[p]}
              </option>
            ))}
          </select>
        </CampoSelect>
      </div>

      {/* Disponibilidade */}
      <div className="flex gap-2 flex-wrap" role="group" aria-label="Filtrar por disponibilidade">
        {(["all", ...Object.keys(DISPONIBILIDADE_CONFIG)] as (Disponibilidade | "all")[]).map((f) => (
          <Pilula
            key={f}
            ativa={filtros.disponibilidade === f}
            classeAtiva="bg-primary text-white border-primary"
            onClick={() => onChange({ disponibilidade: f })}
          >
            {f === "all" ? "Todos" : DISPONIBILIDADE_CONFIG[f].label}
          </Pilula>
        ))}
      </div>

      {/* Tipo */}
      <div className="flex gap-2 flex-wrap" role="group" aria-label="Filtrar por tipo">
        {(["all", ...Object.keys(TIPO_PRODUTO_LABEL)] as (TipoProduto | "all")[]).map((t) => (
          <Pilula
            key={t}
            ativa={filtros.tipo === t}
            classeAtiva="bg-primary-dark text-white border-primary-dark"
            onClick={() => onChange({ tipo: t })}
          >
            {t === "all" ? "Todos os tipos" : TIPO_PRODUTO_LABEL[t]}
          </Pilula>
        ))}
      </div>

      {ativo && (
        <div>
          <Button
            type="button"
            variant="outline"
            onClick={onLimpar}
            className="h-9 border-border text-primary-dark hover:bg-primary-light gap-1.5"
          >
            <X className="w-4 h-4" aria-hidden="true" />
            Limpar filtros
          </Button>
        </div>
      )}
    </form>
  );
}

function CampoSelect({ id, rotulo, children }: { id: string; rotulo: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-1.5 min-w-0">
      <Label htmlFor={id}>{rotulo}</Label>
      {children}
    </div>
  );
}

function Pilula({
  ativa,
  classeAtiva,
  onClick,
  children,
}: {
  ativa: boolean;
  classeAtiva: string;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      aria-pressed={ativa}
      onClick={onClick}
      className={`px-3 py-1.5 rounded-full text-xs font-medium border transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${
        ativa
          ? `${classeAtiva} shadow-sm`
          : "bg-white text-slate-600 border-border hover:border-primary hover:text-primary-dark"
      }`}
    >
      {children}
    </button>
  );
}
