package com.payrollservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnection {
    private static JDBCConnection jdbcConnection;
    private Connection connection;
    public Connection getConnection() {
        return connection;
    }
    private JDBCConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded");
            connection =  DriverManager.getConnection("jdbc:mysql://localhost:3306/payroll_service", "root", "Password@444");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static JDBCConnection getInstance() {
        if(jdbcConnection == null)
            jdbcConnection = new JDBCConnection();
        return jdbcConnection;
    }
}
