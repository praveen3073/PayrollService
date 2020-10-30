package com.payrollservice;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class PayrollService {
    HashMap<Integer, EmployeePayroll> employeePayrollMap = new HashMap<>();

    public static void main(String[] args) {
        PayrollService payrollService = new PayrollService();
        CrudOperations crudOperations = new CrudOperations();
        crudOperations.read(payrollService);                                            // Retrieve employee payroll records
        crudOperations.update(payrollService, "Mani", 300000);            // Update salary for a particular record
        crudOperations.read(payrollService, "Ganesh");                            // Retrieve particular employee payroll records
        crudOperations.read(payrollService, "Rahul");
        crudOperations.read(payrollService, "Mani");
    }

    protected boolean checkIfSynced() {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery("select * from employee e, payroll p where e.emp_id = p.emp_id and e.name = 'Mani'");
            while (resultSet.next()) {
                int emp_id = resultSet.getInt("emp_id");
                EmployeePayroll employeePayrollObject = employeePayrollMap.get(emp_id);
                if (!(employeePayrollObject.company_id == resultSet.getInt("company_id") &&
                        employeePayrollObject.name.equals(resultSet.getString("name")) &&
                        employeePayrollObject.phone.equals(resultSet.getString("phone")) &&
                        employeePayrollObject.address.equals(resultSet.getString("address")) &&
                        employeePayrollObject.gender == resultSet.getString("gender").charAt(0) &&
                        employeePayrollObject.start.compareTo(resultSet.getDate("start")) == 0 &&
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
}
