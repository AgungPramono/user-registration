package com.agung.registration.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.agung.registration")
@EnableJdbcRepositories(basePackages = "com.agung.registration.dao")
@EnableTransactionManagement
@PropertySource("classpath:database.properties")
public class AppConfig extends AbstractJdbcConfiguration {

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(Environment env) {
        String url = System.getenv().getOrDefault("DB_URL", env.getProperty("db.url"));
        String username = System.getenv().getOrDefault("DB_USERNAME", env.getProperty("db.username"));
        String password = System.getenv().getOrDefault("DB_PASSWORD", env.getProperty("db.password"));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(Integer.parseInt(env.getProperty("db.pool.maximum-size", "10")));
        config.setMinimumIdle(Integer.parseInt(env.getProperty("db.pool.minimum-idle", "2")));
        config.setIdleTimeout(Long.parseLong(env.getProperty("db.pool.idle-timeout", "30000")));
        config.setConnectionTimeout(Long.parseLong(env.getProperty("db.pool.connection-timeout", "20000")));
        config.setPoolName("SpringDataJdbc-HikariCP-Pool");

        return new HikariDataSource(config);
    }

    @Bean
    public NamedParameterJdbcOperations namedParameterJdbcOperations(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(initMethod = "initialize")
    public SchemaInitializer schemaInitializer(DataSource dataSource) {
        return new SchemaInitializer(dataSource);
    }
}
