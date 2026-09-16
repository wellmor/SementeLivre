package com.sementelivre.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sementelivre.backend.dto.RelatorioRequestDTO;
import com.sementelivre.backend.dto.RelatorioResponseDTO;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.Relatorio;
import com.sementelivre.backend.exception.RecursoNaoEncontradoException;
import com.sementelivre.backend.repository.RelatorioRepository;

import jakarta.persistence.EntityManager;

@Service
public class RelatorioService
        implements CrudService<RelatorioRequestDTO, RelatorioResponseDTO, UUID> {

    private final RelatorioRepository relatorioRepository;
    private final EntityManager entityManager;

    public RelatorioService(
            RelatorioRepository relatorioRepository,
            EntityManager entityManager) {

        this.relatorioRepository = relatorioRepository;
        this.entityManager = entityManager;
    }

    // CREATE
    @Override
    public RelatorioResponseDTO criar(RelatorioRequestDTO dto) {

        Relatorio relatorio = Relatorio.builder()
                .tipo(dto.tipo())
                .filtrosUtilizados(dto.filtrosUtilizados())
                .proprietario(buscarProprietario(dto.proprietarioId()))
                .build();

        // saveAndFlush grava na hora, assim a dataGeracao ja volta preenchida
        Relatorio salvo = relatorioRepository.saveAndFlush(relatorio);

        return toResponseDTO(salvo);
    }

    // READ - por ID
    @Override
    public RelatorioResponseDTO buscarPorId(UUID id) {
        Relatorio relatorio = buscarEntidadePorId(id);

        return toResponseDTO(relatorio);
    }

    // READ - todos
    @Override
    public List<RelatorioResponseDTO> listar() {
        return relatorioRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // UPDATE
    @Override
    public RelatorioResponseDTO atualizar(UUID id, RelatorioRequestDTO dto) {

        Relatorio relatorio = buscarEntidadePorId(id);

        relatorio.setTipo(dto.tipo());
        relatorio.setFiltrosUtilizados(dto.filtrosUtilizados());
        relatorio.setProprietario(buscarProprietario(dto.proprietarioId()));

        Relatorio atualizado = relatorioRepository.save(relatorio);

        return toResponseDTO(atualizado);
    }

    // DELETE
    @Override
    public void deletar(UUID id) {
        Relatorio relatorio = buscarEntidadePorId(id);

        relatorioRepository.delete(relatorio);
    }

    private Relatorio buscarEntidadePorId(UUID id) {
        return relatorioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Relatório não encontrado: " + id
                ));
    }

    // Proprietario ainda nao tem repository (e placeholder),
    // entao buscamos direto pelo EntityManager
    private Proprietario buscarProprietario(UUID proprietarioId) {
        Proprietario proprietario = entityManager.find(Proprietario.class, proprietarioId);

        if (proprietario == null) {
            throw new RecursoNaoEncontradoException("Proprietário não encontrado: " + proprietarioId);
        }

        return proprietario;
    }

    private RelatorioResponseDTO toResponseDTO(Relatorio relatorio) {
        return new RelatorioResponseDTO(
                relatorio.getId(),
                relatorio.getTipo(),
                relatorio.getFiltrosUtilizados(),
                relatorio.getDataGeracao(),
                relatorio.getProprietario().getId()
        );
    }
}
