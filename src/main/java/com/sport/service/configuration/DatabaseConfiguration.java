package com.sport.service.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DatabaseConfiguration {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Configure PostgreSQL to handle large objects properly
        jdbcTemplate.execute("SET bytea_output = 'escape'");
        jdbcTemplate.execute("SET client_encoding = 'UTF8'");

        return jdbcTemplate;
    }
}
