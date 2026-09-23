package com.sementelivre.backend;

import com.sementelivre.backend.integration.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = BackendApplication.class)
class BackendApplicationTests extends AbstractPostgresIntegrationTest {

    @Test
    void contextLoads() {
    }

}