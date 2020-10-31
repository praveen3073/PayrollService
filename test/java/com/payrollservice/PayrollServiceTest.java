package com.payrollservice;

import org.junit.Assert;
import org.junit.Test;

import java.sql.*;

public class PayrollServiceTest {
    @Test
    public void givenADatabase_WhenUpdated_ShouldSync() {
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            crudOperations.readAll(payrollService);
            crudOperations.update(payrollService, "Mani", 180000);
            ResultSet rs = stmt.executeQuery("select emp_id from employee where name = 'Mani'");
            rs.next();
            boolean result = payrollService.checkIfSynced();
            Assert.assertTrue(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenASqlQuery_WhenExecutedSuccessfully_ShouldCommit() {
        PayrollService payrollService = new PayrollService();
        CrudOperations crudOperations = new CrudOperations();
        crudOperations.createRecord(payrollService, 1, "Shalu", "6654321223", "Jodhpur", 'F', "2018-02-02", 800000);
        boolean result = payrollService.checkIfSynced();
        Assert.assertTrue(result);
    }
}
