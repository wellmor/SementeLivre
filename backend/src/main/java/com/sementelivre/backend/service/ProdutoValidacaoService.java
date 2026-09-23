package com.sementelivre.backend.service;

import org.springframework.stereotype.Service;

import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.TipoProduto;

@Service
public class ProdutoValidacaoService {

    public void validarTipoEspecie(TipoProduto tipo, EspecieGeral especie) {

        if (tipo == null || especie == null) {
            return;
        }

        boolean combinacaoValida = switch (especie) {

            case FEIJAO ->
                    tipo == TipoProduto.LEGUMINOSA;

            case MILHO, ARROZ ->
                    tipo == TipoProduto.CEREAL;

            case ABOBORA ->
                    tipo == TipoProduto.FRUTIFERA
                    || tipo == TipoProduto.HORTALICA;

            case ALFACE, CEBOLA, ALHO ->
                    tipo == TipoProduto.HORTALICA;

            case OUTRAS ->
                    true;
        };

        if (!combinacaoValida) {
            throw new IllegalArgumentException(
                    "Combinação inválida entre tipo " + tipo
                    + " e espécie " + especie
            );
        }
    }
}