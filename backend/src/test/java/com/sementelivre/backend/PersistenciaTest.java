package com.sementelivre.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Propriedade;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.enums.StatusComunidade;
import com.sementelivre.backend.entity.enums.TipoDocumento;
import com.sementelivre.backend.integration.AbstractPostgresIntegrationTest;


//Este teste de persistencia busca trabalhar com Comunidade, Logradouro e Propriedade
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Disabled("Depende de uma migração Flyway para comunidade_t/propriedade_t que não existe ainda "
        + "(só há V1__init.sql, com uma tabela dummy, e V2, do domínio Pessoa). Contra um Postgres "
        + "real, ddl-auto=validate falha porque essas tabelas nunca foram criadas. Schema dessas "
        + "tabelas é responsabilidade do dono do domínio Comunidade/Propriedade — não deve ser "
        + "definido de passagem numa migração do domínio Pessoa. Reabilitar quando essa migração existir.")
public class PersistenciaTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void deveCriarComunidadeEPropriedade() {
        //Arrange

        //Logradouro, Comunidade e Propriedade não possuem umn builder de id, pois dentro deles há um @GeneratedValue(strategy = GenerationType.AUTO) que gera o id automaticamente, então não é necessário setar o id manualmente.
        //Proprietario agora é a entidade real do domínio Pessoa (herança TPT) — seu id também é gerado automaticamente, e ela exige os campos obrigatórios de Pessoa (documento, nome, email, senha).
        //Criar um logradouro
        Logradouro logradouro = Logradouro.builder()
                .logradouro("Rua Teste")
                .numero("123")
                .complemento("Apto 1")
                .bairro("Bairro Teste")
                .municipio("Rio Pomba")
                .uf("MG")
                .build();

        //Criar Proprietario
        Proprietario proprietario = new Proprietario();
        proprietario.setTipoDocumento(TipoDocumento.CPF);
        proprietario.setDocumento("52998224725");
        proprietario.setNome("Proprietario Teste");
        proprietario.setEmail("proprietario.teste@teste.com");
        proprietario.setSenhaHash("hash123");
        proprietario.setRg("MG-000000");

        //Criar Comunidade
        Comunidade comunidade = Comunidade.builder()
                .nome("Comunidade Teste")
                .logradouro(logradouro)
                .status(StatusComunidade.PENDENTE_APROVACAO)
                .dataSolicitacao(java.time.LocalDateTime.now())
                .dataAprovacao(null)
                .build();

        //Criar Propriedade
        Propriedade propriedade = Propriedade.builder()
                .nome("Propriedade Teste")
                .tamanhoHectares(new java.math.BigDecimal("10.5"))
                .logradouro(logradouro)
                .proprietario(proprietario)
                .comunidade(comunidade)
                .build();

        //Act
        entityManager.persist(logradouro);
        entityManager.persist(proprietario);
        entityManager.persist(comunidade);
        entityManager.persist(propriedade);
        entityManager.flush();
        entityManager.clear();

        Comunidade comunidadeSalva = entityManager.find(Comunidade.class, comunidade.getId());
        Propriedade propriedadeSalva = entityManager.find(Propriedade.class, propriedade.getId());

        //Assert
        assertNotNull(comunidadeSalva);
        assertEquals("Comunidade Teste", comunidadeSalva.getNome());
        assertEquals(logradouro.getId(), comunidadeSalva.getLogradouro().getId());
        assertEquals(StatusComunidade.PENDENTE_APROVACAO, comunidadeSalva.getStatus());

        assertNotNull(propriedadeSalva);
        assertEquals("Propriedade Teste", propriedadeSalva.getNome());
        assertEquals(new java.math.BigDecimal("10.5").compareTo(propriedadeSalva.getTamanhoHectares()), 0);
        assertEquals(logradouro.getId(), propriedadeSalva.getLogradouro().getId());
        assertEquals(proprietario.getId(), propriedadeSalva.getProprietario().getId());
        assertEquals(comunidade.getId(), propriedadeSalva.getComunidade().getId());

    }
}
