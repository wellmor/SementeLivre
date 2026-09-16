package com.example.demo.integration;

import com.example.demo.exception.ErrorResponse;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.model.Logradouro;
import com.example.demo.model.Proprietario;
import com.example.demo.model.TipoDocumento;
import com.example.demo.model.Usuario;
import com.example.demo.repository.PessoaRepository;
import com.example.demo.repository.ProprietarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Reproduz, contra um Postgres real (Testcontainers), o cenário de corrida em que
 * duas requisições passam pelo check de unicidade do service e só a constraint
 * UNIQUE do banco barra a segunda. Aqui a colisão é forçada inserindo diretamente
 * via repository (passando por cima do check do service), para confirmar que os
 * nomes de constraint retornados pelo Postgres batem com o switch do
 * GlobalExceptionHandler.
 */
@SpringBootTest
@Testcontainers
class GlobalExceptionHandlerIntegrationTest {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:15-alpine")
                    .withDatabaseName("sementelivre_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private ProprietarioRepository proprietarioRepository;

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    private Usuario novoUsuario(String documento, String email) {
        Usuario usuario = new Usuario();
        usuario.setTipoDocumento(TipoDocumento.CPF);
        usuario.setDocumento(documento);
        usuario.setNome("Usuario Teste");
        usuario.setEmail(email);
        usuario.setSenhaHash("hash123");
        return usuario;
    }

    private Proprietario novoProprietario(String documento, String email, String rg) {
        Proprietario proprietario = new Proprietario();
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setDocumento(documento);
        proprietario.setNome("Proprietario Teste");
        proprietario.setEmail(email);
        proprietario.setSenhaHash("hash123");
        proprietario.setRg(rg);
        return proprietario;
    }

    private ErrorResponse tratarViolacao(DataIntegrityViolationException ex) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/teste");
        return globalExceptionHandler.handleDataIntegrityViolationException(ex, request).getBody();
    }

    @Test
    @Transactional
    void colisaoDeEmailDeveRetornar409ComMensagemEspecifica() {
        pessoaRepository.save(novoUsuario("11122233344", "colisao-email@teste.com"));
        pessoaRepository.flush();

        try {
            pessoaRepository.save(novoUsuario("55566677788", "colisao-email@teste.com"));
            pessoaRepository.flush();
            fail("Esperava DataIntegrityViolationException por email duplicado");
        } catch (DataIntegrityViolationException ex) {
            ErrorResponse resposta = tratarViolacao(ex);
            assertThat(resposta.getStatus()).isEqualTo(409);
            assertThat(resposta.getMessage()).isEqualTo("E-mail já cadastrado no sistema.");
        }
    }

    @Test
    @Transactional
    void colisaoDeDocumentoDeveRetornar409ComMensagemEspecifica() {
        pessoaRepository.save(novoUsuario("22233344455", "doc1@teste.com"));
        pessoaRepository.flush();

        try {
            pessoaRepository.save(novoUsuario("22233344455", "doc2@teste.com"));
            pessoaRepository.flush();
            fail("Esperava DataIntegrityViolationException por documento duplicado");
        } catch (DataIntegrityViolationException ex) {
            ErrorResponse resposta = tratarViolacao(ex);
            assertThat(resposta.getStatus()).isEqualTo(409);
            assertThat(resposta.getMessage()).isEqualTo("Documento já cadastrado no sistema.");
        }
    }

    @Test
    @Transactional
    void colisaoDeRgDeveRetornar409ComMensagemEspecifica() {
        proprietarioRepository.save(novoProprietario("33344455566", "rg1@teste.com", "MG-COLISAO"));
        proprietarioRepository.flush();

        try {
            proprietarioRepository.save(novoProprietario("44455566677", "rg2@teste.com", "MG-COLISAO"));
            proprietarioRepository.flush();
            fail("Esperava DataIntegrityViolationException por RG duplicado");
        } catch (DataIntegrityViolationException ex) {
            ErrorResponse resposta = tratarViolacao(ex);
            assertThat(resposta.getStatus()).isEqualTo(409);
            assertThat(resposta.getMessage()).isEqualTo("RG já cadastrado no sistema.");
        }
    }

    @Test
    @Transactional
    void violacaoDeForeignKeyNaoDeveRetornarMensagemDeJaCadastrado() {
        Logradouro logradouroInexistente = new Logradouro();
        logradouroInexistente.setId(UUID.randomUUID());
        logradouroInexistente.setLogradouro("Rua Fantasma");
        logradouroInexistente.setMunicipio("Cidade");
        logradouroInexistente.setUf("MG");

        Usuario usuario = novoUsuario("66677788899", "fk-violacao@teste.com");
        usuario.setLogradouro(logradouroInexistente);

        try {
            pessoaRepository.save(usuario);
            pessoaRepository.flush();
            fail("Esperava DataIntegrityViolationException por violação de foreign key");
        } catch (DataIntegrityViolationException ex) {
            ErrorResponse resposta = tratarViolacao(ex);
            assertThat(resposta.getStatus()).isEqualTo(409);
            assertThat(resposta.getMessage())
                    .isEqualTo("Violação de integridade de dados (ex: registro já existe ou possui dependências).");
        }
    }
}
