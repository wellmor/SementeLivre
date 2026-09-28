package com.sementelivre.backend.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sementelivre.backend.entity.Propriedade;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.repository.EstoqueRepository;
import com.sementelivre.backend.entity.repository.PedidoRepository;
import com.sementelivre.backend.exception.DependenciaVinculadaException;
import com.sementelivre.backend.repository.ComunidadeRepository;
import com.sementelivre.backend.repository.LogradouroRepository;
import com.sementelivre.backend.repository.PropriedadeRepository;
import com.sementelivre.backend.repository.ProprietarioRepository;

@ExtendWith(MockitoExtension.class)
class PropriedadeServiceTest {

    @Mock private PropriedadeRepository propriedadeRepository;
    @Mock private ComunidadeRepository comunidadeRepository;
    @Mock private ProprietarioRepository proprietarioRepository;
    @Mock private LogradouroRepository logradouroRepository;
    @Mock private EstoqueRepository estoqueRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private Proprietario proprietario;

    private PropriedadeService propriedadeService;
    private UUID propriedadeId;
    private UUID proprietarioId;
    private Propriedade propriedade;

    @BeforeEach
    void setUp() {
        propriedadeService = new PropriedadeService(propriedadeRepository, comunidadeRepository,
                proprietarioRepository, logradouroRepository, estoqueRepository, pedidoRepository);
        propriedadeId = UUID.randomUUID();
        proprietarioId = UUID.randomUUID();
        propriedade = Propriedade.builder().id(propriedadeId).nome("Sítio Boa Vista").proprietario(proprietario).build();
    }

    @Test
    void deletarComEstoqueEPedidosListaAmbasAsDependencias() {
        when(propriedadeRepository.findById(propriedadeId)).thenReturn(Optional.of(propriedade));
        when(proprietario.getId()).thenReturn(proprietarioId);
        when(estoqueRepository.countByProprietarioId(proprietarioId)).thenReturn(3L);
        when(pedidoRepository.countByProprietarioRecebedorId(proprietarioId)).thenReturn(2L);

        DependenciaVinculadaException ex = assertThrows(DependenciaVinculadaException.class,
                () -> propriedadeService.deletar(propriedadeId));

        assertTrue(ex.getMessage().contains("3 estoque(s)"));
        assertTrue(ex.getMessage().contains("2 pedido(s)"));
        verify(propriedadeRepository, never()).delete(any());
    }

    @Test
    void deletarSemDependenciasExclui() {
        when(propriedadeRepository.findById(propriedadeId)).thenReturn(Optional.of(propriedade));
        when(proprietario.getId()).thenReturn(proprietarioId);
        when(estoqueRepository.countByProprietarioId(proprietarioId)).thenReturn(0L);
        when(pedidoRepository.countByProprietarioRecebedorId(proprietarioId)).thenReturn(0L);

        propriedadeService.deletar(propriedadeId);

        verify(propriedadeRepository).delete(propriedade);
    }
}