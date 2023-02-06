package io.rml.framework.containers;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Random;

public class ContainerManager {

    private JdbcDatabaseContainer<?> container;

    public static JdbcDatabaseContainer<?> getPostgresContainer(String tag, String initScriptPath) {
        Random r = new Random();
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(tag))
                .withDatabaseName("test")
                .withUsername("postgres")
                .withPassword("")
                .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
                .withEnv("runID", Integer.toString(r.nextInt()))
                .withInitScript(initScriptPath);

        container.start();

        return container;
    }
}
