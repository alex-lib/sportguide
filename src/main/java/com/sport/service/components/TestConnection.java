package com.sport.service.components;
import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TestConnection {

    private final DataSource dataSource;

    @PostConstruct
    public void test() {
        try (var connection = dataSource.getConnection()) {
            System.out.println("Connected to DB: " + connection.getMetaData().getURL());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}