package com.sementelivre.backend;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Propriedade;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.enums.StatusComunidade;


//Este teste de persistencia busca trabalhar com Comunidade, Logradouro e Propriedade
@DataJpaTest
public class PersistenciaTest {

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void deveCriarComunidadeEPropriedade() {
        //Arrange

        //Logradouro, Comunidade e Propriedade não possuem umn builder de id, pois dentro deles há um @GeneratedValue(strategy = GenerationType.AUTO) que gera o id automaticamente, então não é necessário setar o id manualmente.
        //Já o Proprietario é um placeholder, então o id é gerado manualmente.
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
        //Este proprietário está levando em consideração um placeholder, Modificar mais tarde
        Proprietario proprietario = Proprietario.builder()
                .id(UUID.randomUUID())
                .pessoaId(UUID.randomUUID())
                .build();

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
