package com.agung.registration.config;

import com.agung.registration.exception.DataAccessException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static volatile DatabaseConfig instance;
    private final HikariDataSource dataSource;

    private DatabaseConfig() {
        Properties props = loadProperties();

        String url = System.getenv().getOrDefault("DB_URL", props.getProperty("db.url"));
        String username = System.getenv().getOrDefault("DB_USERNAME", props.getProperty("db.username"));
        String password = System.getenv().getOrDefault("DB_PASSWORD", props.getProperty("db.password"));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximum-size", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimum-idle", "2")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idle-timeout", "30000")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connection-timeout", "20000")));
        config.setPoolName("MySQL-HikariCP-Pool");

        this.dataSource = new HikariDataSource(config);
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfig.class) {
                if (instance == null) {
                    instance = new DatabaseConfig();
                }
            }
        }
        return instance;
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (in == null) {
                throw new DataAccessException("Resource database.properties not found on classpath", null);
            }
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new DataAccessException("Failed to load database.properties", e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
