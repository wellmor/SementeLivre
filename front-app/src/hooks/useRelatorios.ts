'use client';

import { useCallback, useEffect, useState } from 'react';
import { criarRelatorio, listarRelatorios, type RelatorioDTO, type TipoRelatorioDTO } from '@/lib/relatorioApi';
import { useAuth } from '@/context/AuthContext';

export interface RelatorioEmitido {
  idRelatorio: string;
  tipo: TipoRelatorioDTO;
  filtros: Record<string, unknown>;
  dataGeracao: Date;
}

function paraRelatorio(dto: RelatorioDTO): RelatorioEmitido {
  return {
    idRelatorio: dto.id,
    tipo: dto.tipo,
    filtros: dto.filtrosUtilizados ?? {},
    dataGeracao: new Date(dto.dataGeracao),
  };
}

export function useRelatorios() {
  const { user } = useAuth();
  const [relatorios, setRelatorios] = useState<RelatorioEmitido[]>([]);
  const [loading, setLoading] = useState(false);

  const recarregar = useCallback(async () => {
    if (!user) {
      setRelatorios([]);
      return;
    }
    setLoading(true);
    try {
      // O endpoint devolve todos; filtramos pelos do dono logado.
      const todos = await listarRelatorios();
      setRelatorios(
        todos
          .filter((r) => r.proprietarioId === user.uid)
          .map(paraRelatorio)
          .sort((a, b) => b.dataGeracao.getTime() - a.dataGeracao.getTime())
      );
    } catch {
      setRelatorios([]);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    void (async () => {
      await recarregar();
    })();
  }, [recarregar]);

  /** Registra no backend que o relatório foi emitido, com os filtros usados. */
  const registrarEmissao = useCallback(
    async (tipo: TipoRelatorioDTO, filtros: Record<string, unknown>): Promise<RelatorioEmitido> => {
      if (!user) throw new Error('Sessão expirada. Faça login novamente.');
      const dto = await criarRelatorio({ tipo, filtrosUtilizados: filtros, proprietarioId: user.uid });
      const registrado = paraRelatorio(dto);
      setRelatorios((atuais) => [registrado, ...atuais]);
      return registrado;
    },
    [user]
  );

  return { relatorios, loading, recarregar, registrarEmissao };
}
