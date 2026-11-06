package com.sementelivre.backend.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sementelivre.backend.dto.CatalogoOfertaResponseDTO;
import com.sementelivre.backend.dto.CatalogoProdutoDetalheResponseDTO;
import com.sementelivre.backend.dto.CatalogoProdutoResponseDTO;
import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Estoque;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Produto;
import com.sementelivre.backend.entity.Propriedade;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.enums.Disponibilidade;
import com.sementelivre.backend.entity.enums.EspecieGeral;
import com.sementelivre.backend.entity.enums.StatusComunidade;
import com.sementelivre.backend.entity.enums.TipoProduto;
import com.sementelivre.backend.repository.EstoqueRepository;
import com.sementelivre.backend.repository.PropriedadeRepository;
import com.sementelivre.backend.exception.RecursoNaoEncontradoException;

@Service
public class CatalogoPublicoService {

    /*
     * Disponibilidades que podem aparecer no catalogo publico.
     *
     * INDISPONIVEL fica fora. A_NEGOCIAR ficava fora desde a issue #93, mas o app
     * oferece essa opcao no cadastro: o produto sumia do site sem aviso. No site,
     * quem pede escolhe venda, troca ou doacao.
     */
    private static final List<Disponibilidade> DISPONIBILIDADES_PUBLICAS =
            List.of(
                    Disponibilidade.PARA_VENDA,
                    Disponibilidade.PARA_TROCA,
                    Disponibilidade.PARA_DOACAO,
                    Disponibilidade.A_NEGOCIAR
            );

    private final EstoqueRepository estoqueRepository;
    private final PropriedadeRepository propriedadeRepository;

    public CatalogoPublicoService(EstoqueRepository estoqueRepository, PropriedadeRepository propriedadeRepository) {
        this.estoqueRepository = estoqueRepository;
        this.propriedadeRepository = propriedadeRepository;
    }

    @Transactional(readOnly = true)
    public Page<CatalogoProdutoResponseDTO> listar(
            String nomePopular,
            TipoProduto tipo,
            EspecieGeral especie,
            Disponibilidade disponibilidade,
            String comunidade,
            String municipio,
            Pageable pageable) {

        Page<Estoque> pagina = estoqueRepository.buscarCatalogoPublico(
                DISPONIBILIDADES_PUBLICAS,
                normalizar(nomePopular),
                tipo != null ? List.of(tipo) : List.of(TipoProduto.values()),
                especie != null ? List.of(especie) : List.of(EspecieGeral.values()),
                disponibilidade != null
                        ? List.of(disponibilidade)
                        : DISPONIBILIDADES_PUBLICAS,
                normalizar(comunidade),
                normalizar(municipio),
                StatusComunidade.ATIVA,
                pageable
        );

        Map<UUID, Comunidade> daPropriedade = comunidadesDasPropriedades(pagina.getContent());

        return pagina.map(estoque -> toResponseDTO(estoque, comunidadeDaOferta(estoque, daPropriedade)));
    }

    /**
     * Comunidade que o site mostra para a oferta: a de origem do produto ou,
     * sem ela, a da propriedade mais recente do produtor numa comunidade ativa.
     * O app não pede a comunidade no cadastro do produto, então sem isso
     * nenhuma oferta do app tinha comunidade nem município no site (#100).
     */
    private static Comunidade comunidadeDaOferta(Estoque estoque, Map<UUID, Comunidade> daPropriedade) {
        Comunidade origem = estoque.getProduto().getComunidadeOrigem();
        return origem != null ? origem : daPropriedade.get(estoque.getProprietario().getId());
    }

    /** Proprietário → comunidade da propriedade mais recente dele numa comunidade ativa. */
    private Map<UUID, Comunidade> comunidadesDasPropriedades(Collection<Estoque> estoques) {
        List<UUID> semOrigem = estoques.stream()
                .filter(e -> e.getProduto().getComunidadeOrigem() == null)
                .map(e -> e.getProprietario().getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, Comunidade> porProprietario = new HashMap<>();
        if (semOrigem.isEmpty()) {
            return porProprietario;
        }
        // Vem da mais recente para a mais antiga: fica a primeira de cada proprietário
        for (Propriedade propriedade : propriedadeRepository.findByProprietariosEStatusDaComunidade(
                semOrigem, StatusComunidade.ATIVA)) {
            porProprietario.putIfAbsent(propriedade.getProprietario().getId(), propriedade.getComunidade());
        }
        return porProprietario;
    }

    @Transactional(readOnly = true)
    public CatalogoProdutoDetalheResponseDTO buscarPorProdutoId(
            UUID produtoId) {

        List<Estoque> estoques =
                estoqueRepository.buscarProdutosPublicosPorId(
                        produtoId,
                        DISPONIBILIDADES_PUBLICAS
                );

        if (estoques.isEmpty()) {
            throw new RecursoNaoEncontradoException(
                    "Produto nao encontrado no catalogo publico: " + produtoId
            );
        }

        Produto produto = estoques.get(0).getProduto();

        Comunidade comunidade = comunidadeDaOferta(estoques.get(0), comunidadesDasPropriedades(estoques));

        Logradouro logradouro = comunidade != null
                ? comunidade.getLogradouro()
                : null;

        List<CatalogoOfertaResponseDTO> ofertas = estoques.stream()
                .map(estoque -> new CatalogoOfertaResponseDTO(
                        estoque.getId(),
                        estoque.getDescricao(),
                        estoque.getPreco(),
                        estoque.getQuantidade(),
                        estoque.getTipoPesagem(),
                        estoque.getDisponibilidade(),
                        estoque.isAceitaNegociacao()
                ))
                .toList();

        return new CatalogoProdutoDetalheResponseDTO(
                produto.getId(),
                produto.getNomePopular(),
                produto.getNomeCientifico(),
                produto.getHistorico(),
                produto.getUrlFoto(),
                produto.getTipo(),
                produto.getEspecie(),
                produto.getFormato(),
                produto.getFamiliaBotanica(),
                comunidade != null ? comunidade.getId() : null,
                comunidade != null ? comunidade.getNome() : null,
                logradouro != null ? logradouro.getMunicipio() : null,
                logradouro != null ? logradouro.getUf() : null,
                ofertas
        );
    }

    private CatalogoProdutoResponseDTO toResponseDTO(
            Estoque estoque, Comunidade comunidade) {

        Produto produto = estoque.getProduto();

        Logradouro logradouro = comunidade != null
                ? comunidade.getLogradouro()
                : null;

        UUID comunidadeId = comunidade != null
                ? comunidade.getId()
                : null;

        String nomeComunidade = comunidade != null
                ? comunidade.getNome()
                : null;

        String municipio = logradouro != null
                ? logradouro.getMunicipio()
                : null;

        String uf = logradouro != null
                ? logradouro.getUf()
                : null;

        return new CatalogoProdutoResponseDTO(
                produto.getId(),
                produto.getNomePopular(),
                produto.getNomeCientifico(),
                produto.getHistorico(),
                produto.getUrlFoto(),
                produto.getTipo(),
                produto.getEspecie(),
                produto.getFormato(),
                produto.getFamiliaBotanica(),
                estoque.getId(),
                estoque.getDescricao(),
                estoque.getPreco(),
                estoque.getQuantidade(),
                estoque.getTipoPesagem(),
                estoque.getDisponibilidade(),
                comunidadeId,
                nomeComunidade,
                municipio,
                uf,
                estoque.isAceitaNegociacao(),
                nomePublicoDoProdutor(estoque.getProprietario()),
                produto.getDataInclusao(),
                ultimaAlteracao(produto, estoque)
        );
    }

    /**
     * LGPD (RNF-06): o nome do produtor so vai para o site quando ele autorizou
     * aparecer ({@code exibirNoSitePublico}), a mesma regra do perfil publico
     * (ProdutorPublicoService). Sem autorizacao, o site nao recebe nome nenhum.
     */
    static String nomePublicoDoProdutor(Proprietario produtor) {
        if (produtor == null || !produtor.isExibirNoSitePublico()) {
            return null;
        }
        String nome = produtor.getNome();
        return nome == null || nome.isBlank() ? null : nome.trim();
    }

    /**
     * Data mais recente entre a alteracao do produto e as da oferta. A oferta
     * muda sem mexer no produto (preco, saldo, disponibilidade), e o site
     * filtra por "alterados nos ultimos dias" olhando a oferta inteira.
     */
    static LocalDateTime ultimaAlteracao(Produto produto, Estoque estoque) {
        return Stream.of(
                        produto.getDataUltimaAlteracao(),
                        estoque.getDataUltimaAtualizacao(),
                        estoque.getDataMovimentacao(),
                        produto.getDataInclusao())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return "";
        }

        return valor.trim().toLowerCase();
    }
}