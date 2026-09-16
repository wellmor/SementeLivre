package com.sementelivre.backend.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Container Postgres compartilhado (padrão "singleton container" do Testcontainers):
 * iniciado uma única vez por JVM de teste e nunca parado explicitamente — o Ryuk
 * do próprio Testcontainers encerra ao final da execução. Toda classe de teste de
 * integração deve estender esta base em vez de declarar seu próprio container,
 * para não subir um Postgres por classe.
 */
public abstract class AbstractPostgresIntegrationTest {

    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:15-alpine")
                    .withDatabaseName("sementelivre_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Flyway é a fonte de verdade do schema aqui, não o Hibernate. "none" evita que o
        // Hibernate valide TODAS as entidades do módulo — incluindo Comunidade/Propriedade, que
        // ainda não têm migration própria — contra o schema. Nunca usar update/create/create-drop:
        // isso mascararia divergências entre as entidades de Pessoa e a migration V2.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }
}
