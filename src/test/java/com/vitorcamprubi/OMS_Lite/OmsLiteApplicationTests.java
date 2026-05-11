package com.vitorcamprubi.OMS_Lite;

import com.vitorcamprubi.OMS_Lite.integration.AbstractMySqlContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — verifies the full Spring context loads against a real MySQL
 * container, exercising auto-configuration + Flyway V1 migration.
 */
@SpringBootTest
class OmsLiteApplicationTests extends AbstractMySqlContainerTest {

    @Test
    void contextLoads() {
    }
}
