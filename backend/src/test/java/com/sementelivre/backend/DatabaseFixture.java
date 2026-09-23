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

        public static Proprietario persistProprietario(EntityManager entityManager) {
                return persistProprietario(entityManager, "52998224725", "Proprietario Teste",
                                "proprietario.teste@teste.com", "MG-000000");
        }

        public static Proprietario persistProprietario(EntityManager entityManager, String sufixo) {
                return persistProprietario(entityManager, "529" + sufixo,
                                "Proprietario Cross Domain " + sufixo,
                                "proprietario.cross." + sufixo + "@teste.com", "MG-C" + sufixo);
        }

        private static Proprietario persistProprietario(
                        EntityManager entityManager,
                        String documento,
                        String nome,
                        String email,
                        String rg) {
                Proprietario proprietario = new Proprietario();
                proprietario.setTipoDocumento(com.sementelivre.backend.entity.enums.TipoDocumento.CPF);
                proprietario.setDocumento(documento);
                proprietario.setNome(nome);
                proprietario.setEmail(email);
                proprietario.setSenhaHash("hash123");
                proprietario.setRg(rg);
                entityManager.persist(proprietario);
                entityManager.flush();
                return proprietario;
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

        Proprietario proprietario = persistProprietario(entityManager);

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
        entityManager.persist(comunidade);
        entityManager.persist(propriedade);
        entityManager.flush();

        return propriedade;
    }
}
