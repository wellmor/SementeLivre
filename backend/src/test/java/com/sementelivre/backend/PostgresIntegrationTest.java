package com.sementelivre.backend;

import org.springframework.boot.test.context.SpringBootTest;
import com.sementelivre.backend.integration.AbstractPostgresIntegrationTest;

@SpringBootTest(classes = BackendApplication.class)
public abstract class PostgresIntegrationTest extends AbstractPostgresIntegrationTest {
}
