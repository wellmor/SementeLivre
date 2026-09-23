package com.sementelivre.backend.service;

import com.sementelivre.backend.dto.ComunidadeRequestDTO;
import com.sementelivre.backend.dto.ComunidadeResponseDTO;
import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.enums.StatusComunidade;
import com.sementelivre.backend.exception.ResourceNotFoundException;
import com.sementelivre.backend.repository.ComunidadeRepository;
import com.sementelivre.backend.repository.LogradouroRepository;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ComunidadeService implements CrudService<ComunidadeRequestDTO, ComunidadeResponseDTO, UUID> {

    private final ComunidadeRepository comunidadeRepository;
    private final LogradouroRepository logradouroRepository;

    public ComunidadeService(ComunidadeRepository comunidadeRepository, LogradouroRepository logradouroRepository) {
        this.comunidadeRepository = comunidadeRepository;
        this.logradouroRepository = logradouroRepository;
    }

    //Criar
    @Override
    @CacheEvict(value = "comunidades", allEntries = true)
    public ComunidadeResponseDTO criar(ComunidadeRequestDTO dto) {
        Logradouro logradouro = logradouroRepository.findById(dto.logradouroId())
                .orElseThrow(() -> new ResourceNotFoundException("Logradouro não encontrado com o id: " + dto.logradouroId()));

        Comunidade comunidade = Comunidade.builder()
                .nome(dto.nome())
                .logradouro(logradouro)
                .status(StatusComunidade.PENDENTE_APROVACAO)//Coloquei o status padrão de criação como pendente aprovação
                .dataSolicitacao(LocalDateTime.now())
                .build();

        Comunidade comunidadeSalva = comunidadeRepository.save(comunidade);

        return toResponseDTO(comunidadeSalva);
    }

    @Override
    public ComunidadeResponseDTO buscarPorId(UUID id) {

        Comunidade comunidade =  buscarEntidadePorId(id);

        return toResponseDTO(comunidade);
    }

    @Override
    @Cacheable(value = "comunidades")
    public List<ComunidadeResponseDTO> listar() {
        return comunidadeRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @CacheEvict(value = "comunidades", allEntries = true)
    public ComunidadeResponseDTO atualizar(UUID id, ComunidadeRequestDTO dto) {
        Comunidade comunidade = buscarEntidadePorId(id);

        Logradouro logradouro = comunidade.getLogradouro();

        //Verificação para caso o logradouro tenha sido atualizado também, se ele for atualizado ele entra no if, caso contrário ele ignora
        if(!logradouro.getId().equals(dto.logradouroId())){
            logradouro = logradouroRepository.findById(dto.logradouroId())
                    .orElseThrow(() -> new ResourceNotFoundException("Logradouro não encontrado: " + dto.logradouroId()));
        }

        //Preenchendo comunidade:
        comunidade.setNome(dto.nome());
        comunidade.setLogradouro(logradouro);

        Comunidade comunidadeAtualizada = comunidadeRepository.save(comunidade);
        return toResponseDTO(comunidadeAtualizada);
    }

    @Override
    @CacheEvict(value = "comunidades", allEntries = true)
    public void deletar(UUID id) {

        Comunidade comunidade = buscarEntidadePorId(id);

        comunidadeRepository.delete(comunidade);
    }

    //Transformando de entidade comunidade para ComunidadeResponseDTO
    private ComunidadeResponseDTO toResponseDTO(Comunidade comunidade){

        return new ComunidadeResponseDTO(
                comunidade.getId(),
                comunidade.getNome(),
                comunidade.getStatus(),
                comunidade.getDataSolicitacao(),
                comunidade.getDataAprovacao(),
                comunidade.getLogradouro().getUf(),
                comunidade.getLogradouro().getMunicipio()
        );
    }

    //Metodos de Aprovação e Rejeição de Comunidades
    @CacheEvict(value = "comunidades", allEntries = true)
    public ComunidadeResponseDTO aprovar(UUID id){
        Comunidade comunidade = buscarEntidadePorId(id);
        comunidade.setStatus(StatusComunidade.ATIVA);
        comunidade.setDataAprovacao(LocalDateTime.now());

        Comunidade comunidadeAprovada = comunidadeRepository.save(comunidade);
        return toResponseDTO(comunidadeAprovada);
    }
    
    @CacheEvict(value = "comunidades", allEntries = true)
    public ComunidadeResponseDTO rejeitar(UUID id){
        Comunidade comunidade = buscarEntidadePorId(id);
        comunidade.setStatus(StatusComunidade.REJEITADA);

        Comunidade comunidadeRejeitada = comunidadeRepository.save(comunidade);
        return toResponseDTO(comunidadeRejeitada);
    }

    //Metodo para buscar uma Entidade Comunidade
    private Comunidade buscarEntidadePorId(UUID id){
        return comunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidade não encontrada, com id: " + id));
    }

}