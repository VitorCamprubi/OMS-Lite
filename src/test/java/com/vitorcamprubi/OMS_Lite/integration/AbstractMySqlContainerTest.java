package com.vitorcamprubi.OMS_Lite.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests. Starts a singleton MySQL Testcontainer and
 * exposes its connection details via {@link DynamicPropertySource}.
 *
 * <p>The container is intentionally started in a static initializer so it is
 * shared across all subclasses within the same JVM. This is much faster than
 * starting a fresh container per test class.</p>
 *
 * <p>Requires Docker available locally — that is also why this is a separate
 * package from unit tests, so it is easy to exclude / include via Surefire/Failsafe
 * filters if needed.</p>
 */
public abstract class AbstractMySqlContainerTest {

    @SuppressWarnings("resource") // shutdown is delegated to the JVM
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0.36")
    )
            .withDatabaseName("oms_lite_it")
            .withUsername("oms")
            .withPassword("oms");

    static {
        MYSQL.start();
    }

    /**
     * Returns the container JDBC URL with TLS disabled. The MySQL 8 image generates
     * a self-signed certificate on startup, and on machines with even a few seconds
     * of clock skew the JDBC driver may reject it with CertificateNotYetValidException.
     * We don't need TLS to talk to a throw-away container on localhost.
     */
    private static String jdbcUrlNoSsl() {
        String url = MYSQL.getJdbcUrl();
        String sep = url.contains("?") ? "&" : "?";
        return url + sep + "useSSL=false&allowPublicKeyRetrieval=true";
    }

    @DynamicPropertySource
    static void registerMySqlProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractMySqlContainerTest::jdbcUrlNoSsl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL::getDriverClassName);
        // Force a clean state for Flyway between test classes
        registry.add("spring.flyway.clean-disabled", () -> "false");
        registry.add("spring.flyway.url", AbstractMySqlContainerTest::jdbcUrlNoSsl);
        registry.add("spring.flyway.user", MYSQL::getUsername);
        registry.add("spring.flyway.password", MYSQL::getPassword);
    }
}
