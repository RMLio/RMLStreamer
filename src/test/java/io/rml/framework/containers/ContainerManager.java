package io.rml.framework.containers;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;

public class ContainerManager {

    public static JdbcDatabaseContainer<?> getPostgresContainer(String tag, String username, String password) {
        Random r = new Random();
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(tag))
                .withDatabaseName("test")
                .withUsername(username)
                .withPassword(password)
                .withEnv("POSTGRES_HOST_AUTH_METHOD", "trust")
                .withEnv("runID", Integer.toString(r.nextInt()));

        container.start();

        return container;
    }

    public static void executeScript(String scriptPath, String dbURL, String username, String password) throws FileNotFoundException, SQLException {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            ScriptRunner runner = new ScriptRunner(conn);
            runner.runScript(new FileReader("src/test/resources/" + scriptPath));
        }
    }
}
