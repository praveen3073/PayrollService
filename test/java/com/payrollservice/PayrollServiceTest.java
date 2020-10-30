package com.payrollservice;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PayrollServiceTest {
    @Test
    public void givenADatabase_WhenUpdated_ShouldSync() {
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            crudOperations.read(payrollService);
            crudOperations.update(payrollService, "Mani", 180000);
            ResultSet rs = stmt.executeQuery("select emp_id from employee where name = 'Mani'");
            rs.next();
            boolean result = payrollService.checkIfSynced();
            Assert.assertTrue(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
