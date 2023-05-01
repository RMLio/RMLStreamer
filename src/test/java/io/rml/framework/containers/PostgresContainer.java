package io.rml.framework.containers;

import io.rml.framework.util.logging.Logger;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Wrapper class around the Testcontainers' Postgres container. Contains convenience methods for managing it
 */
@Testcontainers
public class PostgresContainer {

    @Container
    public final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer()
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("");

    public void start() {
        POSTGRES_CONTAINER.start();
    }

    public void stop() {
        POSTGRES_CONTAINER.stop();
    }

    public void executeScript(String scriptPath) throws SQLException {
        try (Connection conn = DriverManager.getConnection(POSTGRES_CONTAINER.getJdbcUrl(), POSTGRES_CONTAINER.getUsername(), POSTGRES_CONTAINER.getPassword())) {
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null); // make it not spit out the ran SQL into the output
            runner.runScript(new FileReader(scriptPath));
        } catch (FileNotFoundException exception) {
            Logger.logError(exception.getMessage());
            throw new Error();
        }
    }

    public String getURL() {
        return this.POSTGRES_CONTAINER.getJdbcUrl();
    }
}
