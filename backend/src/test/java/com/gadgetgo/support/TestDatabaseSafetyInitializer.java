package com.gadgetgo.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/** Fail before Flyway starts if test configuration points to the protected development database. */
public final class TestDatabaseSafetyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        String jdbcUrl = environment.getProperty("spring.datasource.url", "");
        String databaseName = jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1).split("\\?")[0];
        if ("gadgetgo".equalsIgnoreCase(databaseName)) {
            throw new IllegalStateException("Tests are blocked from using the protected gadgetgo development database.");
        }
    }
}
