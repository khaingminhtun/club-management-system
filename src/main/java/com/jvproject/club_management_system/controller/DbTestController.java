package com.jvproject.club_management_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@RestController
public class DbTestController {

    private final DataSource dataSource;

    public DbTestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/db-test")
    public String testDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT current_database(), current_user"
             )) {


            result.next();

            return "Database connected successfully! Database: "
                    + result.getString(1)
                    + ", User: "
                    + result.getString(2);
        }
    }
}