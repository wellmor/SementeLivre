package com.sementelivre.backend.service;

import java.util.List;

/**
 * Contrato comum para os serviços de CRUD. Padroniza a assinatura das
 * operações básicas em todos os domínios, separando o DTO de entrada
 * (request) do DTO de saída (response).
 *
 * @param <REQ> DTO recebido nas operações de criação/atualização
 * @param <RES> DTO retornado nas consultas
 * @param <ID>  tipo do identificador da entidade
 */
public interface CrudService<REQ, RES, ID> {

    RES criar(REQ dto);

    RES buscarPorId(ID id);

    List<RES> listar();

    RES atualizar(ID id, REQ dto);

    void deletar(ID id);
}
