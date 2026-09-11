package com.sementelivre.backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.TipoProduto;

class ProdutoValidacaoServiceTest {

    private ProdutoValidacaoService produtoValidacaoService;

    @BeforeEach
    void setUp() {
        produtoValidacaoService = new ProdutoValidacaoService();
    }

    @Test
    void deveAceitarMilhoComoCereal() {
        assertDoesNotThrow(() ->
                produtoValidacaoService.validarTipoEspecie(
                        TipoProduto.CEREAL,
                        EspecieGeral.MILHO
                )
        );
    }

    @Test
    void deveAceitarFeijaoComoLeguminosa() {
        assertDoesNotThrow(() ->
                produtoValidacaoService.validarTipoEspecie(
                        TipoProduto.LEGUMINOSA,
                        EspecieGeral.FEIJAO
                )
        );
    }

    @Test
    void deveAceitarAlfaceComoHortalica() {
        assertDoesNotThrow(() ->
                produtoValidacaoService.validarTipoEspecie(
                        TipoProduto.HORTALICA,
                        EspecieGeral.ALFACE
                )
        );
    }

    @Test
    void deveAceitarAboboraComoFrutifera() {
        assertDoesNotThrow(() ->
                produtoValidacaoService.validarTipoEspecie(
                        TipoProduto.FRUTIFERA,
                        EspecieGeral.ABOBORA
                )
        );
    }

    @Test
    void deveAceitarAboboraComoHortalica() {
        assertDoesNotThrow(() ->
                produtoValidacaoService.validarTipoEspecie(
                        TipoProduto.HORTALICA,
                        EspecieGeral.ABOBORA
                )
        );
    }

    @Test
    void deveAceitarOutrasEspecies() {
        assertDoesNotThrow(() ->
                produtoValidacaoService.validarTipoEspecie(
                        TipoProduto.OUTRAS,
                        EspecieGeral.OUTRAS
                )
        );
    }

    @Test
    void deveRejeitarMilhoComoLeguminosa() {
        assertThrows(
                IllegalArgumentException.class,
                () -> produtoValidacaoService.validarTipoEspecie(
                        TipoProduto.LEGUMINOSA,
                        EspecieGeral.MILHO
                )
        );
    }
}