package com.scoop.api;

import com.scoop.api.repository.FlavorRepository;
import com.scoop.api.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class MariaDbIntegrationTest {
    @Container
    static final MariaDBContainer<?> DATABASE = new MariaDBContainer<>("mariadb:11")
            .withDatabaseName("scoop").withUsername("scoop").withPassword("scoop");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
        registry.add("app.google-client-id", () -> "scoop-test-client");
        registry.add("app.operator-emails", () -> "operator@example.com");
    }

    @Autowired
    private FlavorRepository flavors;
    @Autowired
    private IngredientRepository ingredients;

    @Test
    void flywayCreatesSchemaAndLoadsStarterMenu() {
        assertEquals(8, flavors.count());
        assertEquals(12, ingredients.count());
    }
}
