package com.sementelivre.backend.service;

import com.sementelivre.backend.dto.PropriedadeRequestDTO;
import com.sementelivre.backend.dto.PropriedadeResponseDTO;
import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Propriedade;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.exception.ResourceNotFoundException;
import com.sementelivre.backend.repository.ComunidadeRepository;
import com.sementelivre.backend.repository.LogradouroRepository;
import com.sementelivre.backend.repository.PropriedadeRepository;
import com.sementelivre.backend.repository.ProprietarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PropriedadeService implements CrudService<PropriedadeRequestDTO, PropriedadeResponseDTO, UUID>{

    private final PropriedadeRepository propriedadeRepository;
    private final ComunidadeRepository comunidadeRepository;
    private final ProprietarioRepository proprietarioRepository;
    private final LogradouroRepository logradouroRepository;

    public PropriedadeService(PropriedadeRepository propriedadeRepository, ComunidadeRepository comunidadeRepository, ProprietarioRepository proprietarioRepository, LogradouroRepository logradouroRepository){
        this.propriedadeRepository = propriedadeRepository;
        this.comunidadeRepository = comunidadeRepository;
        this.proprietarioRepository = proprietarioRepository;
        this.logradouroRepository = logradouroRepository;
    }


    @Override
    public PropriedadeResponseDTO criar(PropriedadeRequestDTO dto) {
        Comunidade comunidade = buscaComunidadePorId(dto.comunidadeId());
        Proprietario proprietario = buscaProprietarioPorId(dto.proprietarioId());
        Logradouro logradouro = buscarLogradouroPorId(dto.logradouroId());

        Propriedade propriedade = Propriedade.builder()
                .nome(dto.nome())
                .tamanhoHectares(dto.tamanhoHectares())
                .dataCadastro(LocalDateTime.now())
                .dataUltimaAlteracao(LocalDateTime.now())
                .comunidade(comunidade)
                .proprietario(proprietario)
                .logradouro(logradouro)
                .build();

        Propriedade propriedadeSalva = propriedadeRepository.save(propriedade);

        return toResponseDTO(propriedadeSalva);
    }

    @Override
    public PropriedadeResponseDTO buscarPorId(UUID id) {
        Propriedade propriedade = buscaPropriedadePorId(id);

        return toResponseDTO(propriedade);
    }

    @Override
    public List<PropriedadeResponseDTO> listar() {
        return propriedadeRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public PropriedadeResponseDTO atualizar(UUID id, PropriedadeRequestDTO dto) {
        Propriedade propriedade = buscaPropriedadePorId(id);
        Comunidade comunidade = propriedade.getComunidade();
        Proprietario proprietario = propriedade.getProprietario();
        Logradouro logradouro = propriedade.getLogradouro();

        //Metodo de verificação se houve mudança nos Objetos ao atualizar
        if(!comunidade.getId().equals(dto.comunidadeId())){
            comunidade = comunidadeRepository.findById((dto.comunidadeId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Comunidade não encontrada: " + dto.comunidadeId()));
        }

        if(!proprietario.getId().equals(dto.proprietarioId())){
            proprietario = proprietarioRepository.findById(dto.proprietarioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proprietário não encontrado: " + dto.proprietarioId()));
        }

        if(!logradouro.getId().equals(dto.logradouroId())){
            logradouro = logradouroRepository.findById(dto.logradouroId())
                    .orElseThrow(() -> new ResourceNotFoundException("Logradouro não encontrado: " + dto.logradouroId()));
        }

        //Preenchendo Propriedade:
        propriedade.setNome(dto.nome());
        propriedade.setTamanhoHectares(dto.tamanhoHectares());
        propriedade.setDataUltimaAlteracao(LocalDateTime.now());
        propriedade.setLogradouro(logradouro);
        propriedade.setProprietario(proprietario);
        propriedade.setComunidade(comunidade);

        Propriedade propriedadeAtualizada = propriedadeRepository.save(propriedade);
        return toResponseDTO(propriedadeAtualizada);
    }

    @Override
    public void deletar(UUID id) {
        Propriedade propriedade = buscaPropriedadePorId(id);
        propriedadeRepository.delete(propriedade);
    }


    //Transformando da Entidade propriedade para PropriedadeResponseDTO
    private  PropriedadeResponseDTO toResponseDTO(Propriedade propriedade){

        return new PropriedadeResponseDTO(
                propriedade.getId(),
                propriedade.getNome(),
                propriedade.getTamanhoHectares(),
                propriedade.getDataCadastro(),
                propriedade.getDataUltimaAlteracao(),
                propriedade.getProprietario().getId(),
                propriedade.getProprietario().getNome(),
                propriedade.getComunidade().getId(),
                propriedade.getComunidade().getNome(),
                propriedade.getLogradouro().getUf(),
                propriedade.getLogradouro().getMunicipio()
        );
    }





    //Metodos de busca de entidades, para organizar o código:

    private Propriedade buscaPropriedadePorId(UUID id){
        Propriedade propriedade = propriedadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propriedade não encontrada com id: " + id));
        return propriedade;
    }

    private Comunidade buscaComunidadePorId(UUID id){
        Comunidade comunidade = comunidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comunidade não encontrada com id: " + id));

                return comunidade;
    }

    private Proprietario buscaProprietarioPorId(UUID id){
        Proprietario proprietario = proprietarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proprietário não encontrado com id: " + id));

        return proprietario;
    }

    private Logradouro buscarLogradouroPorId(UUID id){
        Logradouro logradouro = logradouroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Logradouro não encontrado com o id: " + id));

        return logradouro;
    }
}
