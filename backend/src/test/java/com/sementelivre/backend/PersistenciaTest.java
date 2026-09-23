package com.sementelivre.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sementelivre.backend.entity.Comunidade;
import com.sementelivre.backend.entity.Propriedade;
import com.sementelivre.backend.entity.enums.StatusComunidade;
import com.sementelivre.backend.integration.AbstractPostgresIntegrationTest;

import jakarta.persistence.EntityManager;

@SpringBootTest(classes = BackendApplication.class)
class PersistenciaTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void devePersistirEGarantirRelacionamentosEmPostgres() {
        Propriedade propriedade = DatabaseFixture.persistCommunityGraph(entityManager);
        UUID comunidadeId = propriedade.getComunidade().getId();

        entityManager.clear();

        Comunidade comunidadeSalva = entityManager.createQuery(
                        "select c from Comunidade c where c.id = :id", Comunidade.class)
                .setParameter("id", comunidadeId)
                .getSingleResult();
        Propriedade propriedadeSalva = entityManager.find(Propriedade.class, propriedade.getId());

        assertNotNull(comunidadeSalva);
        assertEquals("Comunidade Teste", comunidadeSalva.getNome());
        assertEquals(StatusComunidade.PENDENTE_APROVACAO, comunidadeSalva.getStatus());
        assertNotNull(propriedadeSalva);
        assertEquals("Propriedade Teste", propriedadeSalva.getNome());
        assertEquals(0, new BigDecimal("10.5").compareTo(propriedadeSalva.getTamanhoHectares()));
        assertEquals(comunidadeId, propriedadeSalva.getComunidade().getId());
    }
}
