package com.sementelivre.backend;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Logradouro;
import com.sementelivre.backend.entity.Propriedade;
import com.sementelivre.backend.entity.Proprietario;
import com.sementelivre.backend.entity.enums.StatusComunidade;

public final class DatabaseFixture {

    private DatabaseFixture() {
    }

    public static Propriedade persistCommunityGraph(EntityManager entityManager) {
        Logradouro logradouro = Logradouro.builder()
                .logradouro("Rua Teste")
                .numero("123")
                .complemento("Apto 1")
                .bairro("Bairro Teste")
                .municipio("Rio Pomba")
                .uf("MG")
                .cep("36180-000")
                .build();

        Proprietario proprietario = new Proprietario();
        proprietario.setTipoDocumento(com.sementelivre.backend.entity.enums.TipoDocumento.CPF);
        proprietario.setDocumento("52998224725");
        proprietario.setNome("Proprietario Teste");
        proprietario.setEmail("proprietario.teste@teste.com");
        proprietario.setSenhaHash("hash123");
        proprietario.setRg("MG-000000");

        Comunidade comunidade = Comunidade.builder()
                .nome("Comunidade Teste")
                .logradouro(logradouro)
                .status(StatusComunidade.PENDENTE_APROVACAO)
                .dataSolicitacao(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();

        Propriedade propriedade = Propriedade.builder()
                .nome("Propriedade Teste")
                .tamanhoHectares(new BigDecimal("10.5"))
                .logradouro(logradouro)
                .proprietario(proprietario)
                .comunidade(comunidade)
                .build();

        entityManager.persist(logradouro);
        entityManager.persist(proprietario);
        entityManager.persist(comunidade);
        entityManager.persist(propriedade);
        entityManager.flush();

        return propriedade;
    }
}
