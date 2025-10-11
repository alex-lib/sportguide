package com.sport.service.components;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;

@RequiredArgsConstructor
@Service
@Slf4j
public class TestConnection {

    private final DataSource dataSource;

    @PostConstruct
    public void testConnection() {
        try (var connection = dataSource.getConnection()) {
            log.info("Connected to DB: " + connection.getMetaData().getURL());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}