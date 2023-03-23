package io.rml.framework.containers;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Testcontainers
public class ContainerManager {

    @Container
    public static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer()
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("");

    public static void executeScript(String scriptPath, String dbURL, String username, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbURL, username, password)) {
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null); // make it not spit out the ran SQL into the output
            runner.runScript(new FileReader("src/test/resources/" + scriptPath));
        } catch (FileNotFoundException ignored) { // if the file is absent, it means that no DB setup is required

        }
    }
}
