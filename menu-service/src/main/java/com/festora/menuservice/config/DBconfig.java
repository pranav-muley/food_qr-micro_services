package com.festora.menuservice.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DBconfig {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private final Connection _instance_ = null;

    @PostConstruct
    public Connection init() {
        if (_instance_ == null) {
            return buildConnection();
        }
        return _instance_;
    }

    private Connection buildConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // ensure driver is loaded
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Failed to connect to MySQL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to MySQL: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return _instance_;
    }
}
