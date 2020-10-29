package com.payrollservice;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PayrollServiceTest {
    @Test
    public void givenADatabase_OnSyncing_ShouldMatchObjectInMap() {
        PayrollService payrollService = new PayrollService();
        try {
            Connection con = payrollService.getConnection();
            Statement stmt = con.createStatement();
            payrollService.getRecordsFromDB(payrollService, con);
            payrollService.updateSalary(payrollService, con, "Mani", 180000);
            ResultSet rs = stmt.executeQuery("select emp_id from employee where name = 'Mani'");
            rs.next();
            int emp_id = rs.getInt(1);
            rs = stmt.executeQuery("select * from employee e, payroll p where e.emp_id = p.emp_id and e.name = 'Mani'");
            rs.next();
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).company_id, rs.getInt("company_id"));
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).emp_id, rs.getInt("emp_id"));
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).name, rs.getString("name"));
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).phone, rs.getString("phone"));
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).address, rs.getString("address"));
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).gender, rs.getString("gender").charAt(0));
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).start, rs.getDate("start"));
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).basic_pay, rs.getDouble("basic_pay"), 0.0);
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).deductions, rs.getDouble("deductions"), 0.0);
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).taxable_pay, rs.getDouble("taxable_pay"), 0.0);
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).tax, rs.getDouble("tax"), 0.0);
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).net_pay, rs.getDouble("net_pay"), 0.0);
        } catch (IOException | SQLException | RecordNotFoundException e) {
            e.printStackTrace();
        }
    }
}
