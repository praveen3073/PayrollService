package com.payrollservice;

import java.sql.*;

// Retrieve all records from DB
public class CrudOperations {
    public void createRecord(PayrollService payrollService, int company_id, String name, String phone, String address, char gender, String startDate, double basic_pay) {
        try {
            System.out.println("Inserting record...");
            Connection con = JDBCConnection.getInstance().getConnection();
            con.setAutoCommit(false);

            // Insert employee in employee table
            String insertEmployeeQuery = "insert into employee (company_id, name, phone, address, gender, start) values " +
                    "(?,?,?,?,?,?)";
            PreparedStatement employeePreparedStatement = con.prepareStatement(insertEmployeeQuery);
            employeePreparedStatement.setInt(1, company_id);
            employeePreparedStatement.setString(2, name);
            employeePreparedStatement.setString(3, phone);
            employeePreparedStatement.setString(4, address);
            employeePreparedStatement.setString(5, String.valueOf(gender));
            employeePreparedStatement.setString(6, startDate);
            int countEmployeeRecordsChanged = employeePreparedStatement.executeUpdate();

            // Insert employee payroll in payroll table
            String insertPayrollQuery = "insert into payroll (emp_id, basic_pay, deductions, taxable_pay, tax, net_pay) values " +
                    "(?,?,?,?,?,?)";
            double deductions = 0.2 * basic_pay;
            double taxable_pay = basic_pay - deductions;
            double tax = 0.1 * taxable_pay;
            double net_pay = basic_pay - tax;
            PreparedStatement payrollPreparedStatement = con.prepareStatement(insertPayrollQuery);
            payrollPreparedStatement.setInt(1, payrollService.getEmpIdByName(name));
            payrollPreparedStatement.setDouble(2, basic_pay);
            payrollPreparedStatement.setDouble(3, deductions);
            payrollPreparedStatement.setDouble(4, taxable_pay);
            payrollPreparedStatement.setDouble(5, tax);
            payrollPreparedStatement.setDouble(6, net_pay);
            int countPayrollRecordsChanged = payrollPreparedStatement.executeUpdate();

            if(countEmployeeRecordsChanged>0 && countPayrollRecordsChanged>0)
                con.commit();
            con.setAutoCommit(true);
            sync(payrollService);
        } catch (SQLException | RecordsNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void readAll(PayrollService payrollService) {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            /*String query = "select * from employee e, payroll p " +
                    "where e.emp_id = p.emp_id";*/
            String query = "select c.company_name, e.emp_id, e.name, e.phone, e.address, e.gender, e.start, p.basic_pay, p.deductions, p.taxable_pay, p.tax, p.net_pay, d.department_name " +
                    "from company c " +
                    "right join employee e on c.company_id = e.company_id " +
                    "inner join payroll p on e.emp_id = p.emp_id " +
                    "left join (select emp_id, department_name from employee_department, department " +
                    "where employee_department.department_id = department.department_id) d " +
                    "on e.emp_id = d.emp_id";
            ResultSet resultSet = stmt.executeQuery(query);
            System.out.println("Displaying all records: ");
            displayResultSet(resultSet);
            sync(payrollService);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve records from DB for a given name
    public void readByName(PayrollService payrollService, String name) {
        try {
            PreparedStatement preparedStatement = PayrollDBService.getInstance().getPreparedStatement();
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("Display records for " + name + ": ");
            displayResultSet(resultSet);
            sync(payrollService);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve records from DB where employee start date in given date frame
    public void readByDate(PayrollService payrollService, String dateLowerLimit, String dateUpperLimit) {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "select * from employee e, payroll p " +
                    "where e.emp_id = p.emp_id and " +
                    "start between cast('" + dateLowerLimit + "' as date) " +
                    "and cast('" + dateUpperLimit + "' as date)";
            ResultSet resultSet = stmt.executeQuery(query);
            System.out.println("Displaying records with start date between " + dateLowerLimit + " and " + dateUpperLimit + ":");
            displayResultSet(resultSet);
            sync(payrollService);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve sum, avg, min, max, count of salaries grouped by gender, from DB
    public void readSalaryStatsByGender(PayrollService payrollService) {
        try {
            System.out.println("Displaying employee salary stats gender wise: ");
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "SELECT employee.gender, SUM(p.basic_pay) as sum, " +
                    "AVG(p.basic_pay) as avg, MIN(p.basic_pay) as min, " +
                    "MAX(p.basic_pay) as max, COUNT(p.basic_pay) as count " +
                    "FROM " +
                    "(SELECT emp_id, basic_pay FROM payroll) p, " +
                    "employee " +
                    "WHERE employee.emp_id = p.emp_id " +
                    "GROUP BY employee.gender";
            ResultSet resultSet = stmt.executeQuery(query);
            displayResultSet(resultSet);
            sync(payrollService);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update salary of a given employee in map and DB
    public void updateSalaryByName(PayrollService payrollService, String name, double newSalary) {
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
            sync(payrollService);
            updatePreparedStatement.close();
            System.out.println("Salary updated");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sync(PayrollService payrollService) {
        try {
            payrollService.employeePayrollMap.clear();
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "select c.company_name, e.emp_id, e.name, e.phone, e.address, e.gender, e.start, p.basic_pay, p.deductions, p.taxable_pay, p.tax, p.net_pay, d.department_name " +
                    "from company c " +
                    "right join employee e on c.company_id = e.company_id " +
                    "inner join payroll p on e.emp_id = p.emp_id " +
                    "left join (select emp_id, department_name from employee_department, department " +
                    "where employee_department.department_id = department.department_id) d " +
                    "on e.emp_id = d.emp_id";
            ResultSet resultSet = stmt.executeQuery(query);
            while(resultSet.next()) {
                EmployeePayroll tempLoopObject = new EmployeePayroll();
                tempLoopObject.company_name = resultSet.getString("company_name");
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
                String department_name = resultSet.getString("department_name");
                tempLoopObject.departments.add(department_name);
                payrollService.employeePayrollMap.put(tempLoopObject.emp_id, tempLoopObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayResultSet(ResultSet resultSet) {
        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnsNumber = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = resultSet.getString(i);
                    System.out.print(resultSetMetaData.getColumnName(i) + " : " + columnValue);
                }
                System.out.println();
            }
        } catch (SQLException e) {
                e.printStackTrace();
        }
    }
}
