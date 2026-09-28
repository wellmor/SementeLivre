package com.sementelivre.backend.service;

import com.sementelivre.backend.dto.LogradouroDTO;
import com.sementelivre.backend.dto.PessoaUpdateRequestDTO;
import com.sementelivre.backend.exception.EmailJaCadastradoException;
import com.sementelivre.backend.exception.PessoaNaoEncontradaException;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Pessoa;
import com.sementelivre.backend.entity.repository.EstoqueRepository;
import com.sementelivre.backend.entity.repository.PedidoRepository;
import com.sementelivre.backend.repository.LogradouroRepository;
import com.sementelivre.backend.repository.NotificacaoRepository;
import com.sementelivre.backend.repository.PessoaRepository;
import com.sementelivre.backend.repository.PropriedadeRepository;
import com.sementelivre.backend.repository.RelatorioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PessoaService {

    private final PessoaRepository pessoaRepository;
    private final LogradouroRepository logradouroRepository;
    private final UsuarioService usuarioService;
    private final EstoqueRepository estoqueRepository;
    private final PedidoRepository pedidoRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final RelatorioRepository relatorioRepository;
    private final PropriedadeRepository propriedadeRepository;

    public PessoaService(PessoaRepository pessoaRepository, LogradouroRepository logradouroRepository,
                         UsuarioService usuarioService, EstoqueRepository estoqueRepository,
                         PedidoRepository pedidoRepository, NotificacaoRepository notificacaoRepository,
                         RelatorioRepository relatorioRepository, PropriedadeRepository propriedadeRepository) {
        this.pessoaRepository = pessoaRepository;
        this.logradouroRepository = logradouroRepository;
        this.usuarioService = usuarioService;
        this.estoqueRepository = estoqueRepository;
        this.pedidoRepository = pedidoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.relatorioRepository = relatorioRepository;
        this.propriedadeRepository = propriedadeRepository;
    }

    @Transactional(readOnly = true)
    public List<Pessoa> listarTodas() {
        return pessoaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pessoa buscarPorId(UUID id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new PessoaNaoEncontradaException("Pessoa não encontrada com o ID: " + id));
    }

    @Transactional
    public Pessoa atualizar(UUID id, PessoaUpdateRequestDTO dto) {
        Pessoa pessoa = buscarPorId(id);

        if (!pessoa.getEmail().equalsIgnoreCase(dto.getEmail()) && pessoaRepository.existsByEmail(dto.getEmail())) {
            throw new EmailJaCadastradoException("Email já cadastrado no sistema.");
        }

        pessoa.setNome(dto.getNome());
        pessoa.setTelefone(dto.getTelefone());
        pessoa.setEmail(dto.getEmail());

        if (dto.getEndereco() != null) {
            Logradouro logradouro = pessoa.getLogradouro();
            if (logradouro == null) {
                logradouro = new Logradouro();
            }
            mapLogradouro(dto.getEndereco(), logradouro);
            logradouroRepository.save(logradouro);
            pessoa.setLogradouro(logradouro);
        }

        return pessoaRepository.save(pessoa);
    }

    @Transactional
    public void deletar(UUID id) {
        Pessoa pessoa = buscarPorId(id);
        usuarioService.excluirContaDaPessoa(id);
        pessoaRepository.delete(pessoa);
    }

    /**
     * Exclusão de conta a pedido do titular (direito ao esquecimento, LGPD).
     *
     * <p>Todas as tabelas filhas usam ON DELETE RESTRICT, então a conta só sai
     * depois que os dados operacionais da pessoa são removidos. A ordem importa:
     * pedidos primeiro (os itens caem por cascade), depois o que o proprietário
     * possui, e por fim login e cadastro.</p>
     *
     * <p>Os produtos em si não são apagados: são catálogo compartilhado entre
     * produtores e não pertencem à pessoa. O que sai é o estoque dela.</p>
     */
    @Transactional
    public void excluirConta(UUID id) {
        buscarPorId(id);

        pedidoRepository.deleteAllEnvolvendoPessoa(id);
        notificacaoRepository.deleteAllByProprietarioId(id);
        relatorioRepository.deleteAllByProprietarioId(id);
        propriedadeRepository.deleteAllByProprietarioId(id);
        estoqueRepository.deleteAllByProprietarioId(id);

        usuarioService.excluirContaDaPessoa(id);
        pessoaRepository.deleteById(id);
    }

    private void mapLogradouro(LogradouroDTO dto, Logradouro logradouro) {
        logradouro.setLogradouro(dto.getLogradouro());
        logradouro.setNumero(dto.getNumero());
        logradouro.setComplemento(dto.getComplemento());
        logradouro.setBairro(dto.getBairro());
        logradouro.setMunicipio(dto.getMunicipio());
        logradouro.setUf(dto.getUf());
        logradouro.setCep(dto.getCep());
    }
}
