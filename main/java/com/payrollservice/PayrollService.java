package com.payrollservice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class PayrollService {
    HashMap<Integer, EmployeePayroll> employeePayrollMap = new HashMap<>();

    public static void main(String[] args) {
        PayrollService payrollService = new PayrollService();
        CrudOperations crudOperations = new CrudOperations();
        crudOperations.readAll(payrollService);                                            // Retrieve employee payroll records
        crudOperations.updateSalaryByName(payrollService, "Mani", 300000);            // Update salary for a particular record
        crudOperations.readByName(payrollService, "Ganesh");                            // Retrieve particular employee payroll records
        crudOperations.readByName(payrollService, "Rahul");
        crudOperations.readByName(payrollService, "Mani");
        crudOperations.readByDate(payrollService, "2018-01-01", "2019-12-31");        // Retrieve records with start date in given date frame
        crudOperations.readSalaryStatsByGender(payrollService);                      // Retrieve sum, avg, min, max, count of employee salaries grouped by gender
    }

    protected boolean checkIfSynced() {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery("select * from employee e, payroll p where e.emp_id = p.emp_id and e.name = 'Mani'");
            while (resultSet.next()) {
                int emp_id = resultSet.getInt("emp_id");
                EmployeePayroll employeePayrollObject = employeePayrollMap.get(emp_id);
                if (!(employeePayrollObject.name.equals(resultSet.getString("name")) &&
                        employeePayrollObject.phone.equals(resultSet.getString("phone")) &&
                        employeePayrollObject.address.equals(resultSet.getString("address")) &&
                        employeePayrollObject.gender == resultSet.getString("gender").charAt(0) &&
                        employeePayrollObject.start.compareTo(resultSet.getDate("start").toString()) == 0 &&
                        employeePayrollObject.basic_pay == resultSet.getDouble("basic_pay") &&
                        employeePayrollObject.deductions == resultSet.getDouble("deductions") &&
                        employeePayrollObject.taxable_pay == resultSet.getDouble("taxable_pay") &&
                        employeePayrollObject.tax == resultSet.getDouble("tax") &&
                        employeePayrollObject.net_pay == resultSet.getDouble("net_pay")))
                    return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public int getEmpIdByName(String name) throws RecordsNotFoundException {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "select emp_id from employee where name = '" + name + "'";
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            return resultSet.getInt("emp_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RecordsNotFoundException("Record not found");
    }

    public void addEmployeesToDBWithThread(ArrayList<EmployeePayroll> employeeList) {
        HashMap<Integer, Boolean> employeeAdditionStatus = new HashMap<>();
        for(EmployeePayroll employee : employeeList) {
            Runnable task = () -> {
                employeeAdditionStatus.put(employee.hashCode(), false);
                CrudOperations crudOperations = new CrudOperations();
                PayrollService payrollService = new PayrollService();
                crudOperations.createRecord(payrollService, 1, employee.name, employee.phone, employee.address, employee.gender, employee.start, employee.basic_pay);
                employeeAdditionStatus.put(employee.hashCode(), true);
            };
                Thread thread = new Thread(task, employee.name);
                thread.start();
        }
        while(employeeAdditionStatus.containsValue(false)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
