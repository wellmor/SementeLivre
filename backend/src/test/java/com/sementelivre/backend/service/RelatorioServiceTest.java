package com.sementelivre.backend.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sementelivre.backend.dto.RelatorioRequestDTO;
import com.sementelivre.backend.dto.RelatorioResponseDTO;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Relatorio;
import com.sementelivre.backend.entity.enums.TipoRelatorio;
import com.sementelivre.backend.exception.RecursoNaoEncontradoException;
import com.sementelivre.backend.repository.RelatorioRepository;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private EntityManager entityManager;

    private RelatorioService relatorioService;

    private UUID relatorioId;
    private UUID proprietarioId;

    private Proprietario proprietario;
    private Relatorio relatorio;
    private RelatorioRequestDTO dto;

    @BeforeEach
    void setUp() {
        relatorioService = new RelatorioService(
                relatorioRepository,
                entityManager
        );

        relatorioId = UUID.randomUUID();
        proprietarioId = UUID.randomUUID();

        proprietario = new Proprietario();
        proprietario.setId(proprietarioId);

        relatorio = Relatorio.builder()
                .id(relatorioId)
                .tipo(TipoRelatorio.ESTOQUE_SEMENTES)
                .filtrosUtilizados(Map.of("especie", "FEIJAO"))
                .proprietario(proprietario)
                .build();

        dto = new RelatorioRequestDTO(
                TipoRelatorio.ESTOQUE_SEMENTES,
                Map.of("especie", "FEIJAO"),
                proprietarioId
        );
    }

    @Test
    void deveCriarRelatorio() {
        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(proprietario);
        when(relatorioRepository.saveAndFlush(any(Relatorio.class))).thenReturn(relatorio);

        RelatorioResponseDTO resposta = relatorioService.criar(dto);

        assertNotNull(resposta);
        assertEquals(TipoRelatorio.ESTOQUE_SEMENTES, resposta.tipo());
        assertEquals("FEIJAO", resposta.filtrosUtilizados().get("especie"));
        assertEquals(proprietarioId, resposta.proprietarioId());
    }

    @Test
    void naoDeveCriarRelatorioSeProprietarioNaoExistir() {
        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(null);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> relatorioService.criar(dto));

        verify(relatorioRepository, never()).saveAndFlush(any(Relatorio.class));
    }

    @Test
    void deveBuscarRelatorioPorId() {
        when(relatorioRepository.findById(relatorioId)).thenReturn(Optional.of(relatorio));

        RelatorioResponseDTO resposta = relatorioService.buscarPorId(relatorioId);

        assertEquals(relatorioId, resposta.id());
    }

    @Test
    void deveLancarErroAoBuscarRelatorioInexistente() {
        when(relatorioRepository.findById(relatorioId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> relatorioService.buscarPorId(relatorioId));
    }

    @Test
    void deveListarRelatorios() {
        when(relatorioRepository.findAll()).thenReturn(List.of(relatorio));

        List<RelatorioResponseDTO> resposta = relatorioService.listar();

        assertEquals(1, resposta.size());
    }

    @Test
    void deveAtualizarRelatorio() {
        RelatorioRequestDTO novosDados = new RelatorioRequestDTO(
                TipoRelatorio.PEDIDOS_REALIZADOS,
                null,
                proprietarioId
        );

        when(relatorioRepository.findById(relatorioId)).thenReturn(Optional.of(relatorio));
        when(entityManager.find(Proprietario.class, proprietarioId)).thenReturn(proprietario);
        when(relatorioRepository.save(relatorio)).thenReturn(relatorio);

        RelatorioResponseDTO resposta = relatorioService.atualizar(relatorioId, novosDados);

        assertEquals(TipoRelatorio.PEDIDOS_REALIZADOS, resposta.tipo());
    }

    @Test
    void deveDeletarRelatorio() {
        when(relatorioRepository.findById(relatorioId)).thenReturn(Optional.of(relatorio));

        relatorioService.deletar(relatorioId);

        verify(relatorioRepository).delete(relatorio);
    }

    @Test
    void naoDeveDeletarRelatorioInexistente() {
        when(relatorioRepository.findById(relatorioId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> relatorioService.deletar(relatorioId));

        verify(relatorioRepository, never()).delete(any(Relatorio.class));
    }
}
