package com.sementelivre.backend.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PostgresIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void containerPostgresDeveSubirEEstarPronto() {
        assertThat(POSTGRES.isRunning()).isTrue();
        assertThat(POSTGRES.isCreated()).isTrue();
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
