package com.payrollservice;

import java.sql.*;

// Retrieve all records from DB
public class CrudOperations {
    public void read(PayrollService payrollService) {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "select * from employee e, payroll p " +
                    "where e.emp_id = p.emp_id";
            ResultSet resultSet = stmt.executeQuery(query);
            System.out.println("Display all records: ");
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

    // Retrieve records from DB for a given name
    public void read(PayrollService payrollService, String name) {
        try {
            PreparedStatement preparedStatement = PayrollDBService.getInstance().getPreparedStatement();
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("Display records for " + name + ": ");
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve records from DB where employee start date in given date frame
    public void read(PayrollService payrollService, String dateLowerLimit, String dateUpperLimit) {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "select * from employee e, payroll p " +
                    "where e.emp_id = p.emp_id and " +
                    "start between cast('" + dateLowerLimit +"' as date) " +
                    "and cast('" + dateUpperLimit + "' as date)";
            ResultSet resultSet = stmt.executeQuery(query);
            System.out.println("Displaying records with start date between " + dateLowerLimit + " and " + dateUpperLimit + ":");
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

    // Update salary of a given employee in map and DB
    public void update(PayrollService payrollService, String name, double newSalary) {
        try {
            System.out.println("Updating salary for " + name + "...");
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
            System.out.println("Salary updated");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
