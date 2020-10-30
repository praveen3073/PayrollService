package com.payrollservice;

import java.sql.*;

public class CrudOperations {
    public void read(PayrollService payrollService) {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "select * from employee e, payroll p " +
                    "where e.emp_id = p.emp_id";
            ResultSet resultSet = stmt.executeQuery(query);
            while (resultSet.next()) {
                EmployeePayroll tempLoopObject = new EmployeePayroll();
                tempLoopObject.company_id = resultSet.getInt("company_id");
                tempLoopObject.emp_id = resultSet.getInt("emp_id");
                tempLoopObject.name = resultSet.getString("name");
                tempLoopObject.phone = resultSet.getString("phone");
                tempLoopObject.address = resultSet.getString("address");
                tempLoopObject.gender = resultSet.getString("gender").charAt(0);
                tempLoopObject.start = resultSet.getDate("start");
                tempLoopObject.basic_pay = resultSet.getDouble("basic_pay");
                tempLoopObject.deductions = resultSet.getDouble("deductions");
                tempLoopObject.taxable_pay = resultSet.getDouble("taxable_pay");
                tempLoopObject.tax = resultSet.getDouble("tax");
                tempLoopObject.net_pay = resultSet.getDouble("net_pay");
                payrollService.employeePayrollMap.put(tempLoopObject.emp_id, tempLoopObject);
                System.out.println(tempLoopObject);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(PayrollService payrollService, String name, double newSalary) {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            PreparedStatement updatePreparedStatement = con.prepareStatement("update payroll set basic_pay = ? where emp_id = " +
                    "(select emp_id from employee where name = ?)");
            updatePreparedStatement.setDouble(1, newSalary);
            updatePreparedStatement.setString(2, name);
            updatePreparedStatement.executeUpdate();
            for (EmployeePayroll employeePayroll : payrollService.employeePayrollMap.values()) {
                if (employeePayroll.name.equals(name)) {
                    int emp_id = employeePayroll.emp_id;
                    employeePayroll.basic_pay = newSalary;
                    payrollService.employeePayrollMap.put(emp_id, employeePayroll);
                }
            }
            updatePreparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
