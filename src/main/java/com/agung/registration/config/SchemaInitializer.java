package com.agung.registration.config;

import com.agung.registration.dao.CityDao;
import com.agung.registration.dao.ProvinceDao;
import com.agung.registration.exception.DataAccessException;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class SchemaInitializer {

    private final DataSource dataSource;

    public SchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SchemaInitializer(DataSource dataSource, ProvinceDao provinceDao, CityDao cityDao) {
        this(dataSource);
    }

    public void initialize() {
        executeScript("schema.sql");
        executeScript("data.sql");
    }

    private void executeScript(String resourceName) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new DataAccessException("SQL resource not found: " + resourceName, null);
            }
            String sqlContent = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String[] statements = sqlContent.split(";");
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                for (String rawSql : statements) {
                    String trimmed = rawSql.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed executing SQL script: " + resourceName, e);
        } catch (Exception e) {
            throw new DataAccessException("Error loading resource: " + resourceName, e);
        }
    }
}
