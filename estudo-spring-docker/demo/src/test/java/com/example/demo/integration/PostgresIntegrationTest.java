package com.example.demo.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class PostgresIntegrationTest {

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
    private JdbcTemplate jdbcTemplate;

    @Test
    void containerPostgresDeveSubirEEstarPronto() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(postgres.isCreated()).isTrue();
    }

    @Test
    void flywayDeveExecutarMigrationsEConexaoDeveFuncionar() {
        Integer resultado = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertThat(resultado).isEqualTo(1);

        Integer tabelasCriadas = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'pessoa_t'",
                Integer.class);
        assertThat(tabelasCriadas).isEqualTo(1);
    }
}
