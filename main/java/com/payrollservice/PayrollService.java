package com.payrollservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class PayrollService {
    private static Connection getConnection() throws IOException, SQLException {
        FileInputStream fin = new FileInputStream("C:\\Users\\Praveen Satya\\IdeaProjects\\PayrollServiceProject\\config.properties");
        Properties prop = new Properties();
        prop.load(fin);
        return DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));
    }
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection con = null;
        try {
            con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery("select * from employee");
            while (resultSet.next()) {
                System.out.println(resultSet.getString("name"));
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
