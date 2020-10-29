package com.payrollservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Properties;

public class PayrollService {
    HashMap<Integer, EmployeePayroll> employeePayrollMap = new HashMap<>();

    public static void main(String[] args) {
        PayrollService payrollService = new PayrollService();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection con;
        try {
            con = payrollService.getConnection();
            payrollService.getRecordsFromDB(payrollService, con);
            payrollService.updateSalary(payrollService, con, "Mani", 300000);
        } catch (IOException | SQLException | RecordNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void updateSalary(PayrollService payrollService, Connection con, String name, double newSalary) throws SQLException, RecordNotFoundException {
        PreparedStatement updatePreparedStatement = con.prepareStatement("update payroll set basic_pay = ? where emp_id = " +
                "(select emp_id from employee where name = ?)");
        updatePreparedStatement.setDouble(1, newSalary);
        updatePreparedStatement.setString(2, name);
        updatePreparedStatement.executeUpdate();
        PreparedStatement selectPreparedStatement = con.prepareStatement("select emp_id from employee where name = ?");
        selectPreparedStatement.setString(1, name);
        ResultSet rs = selectPreparedStatement.executeQuery();
        rs.next();
        int emp_id = rs.getInt(1);
        payrollService.syncSalaryInMap(payrollService, emp_id, newSalary);
    }

    private void syncSalaryInMap(PayrollService payrollService, int emp_id, double newSalary) throws RecordNotFoundException {
        payrollService.checkRecordPresentInMap(payrollService, emp_id);
        EmployeePayroll tempEmployeePayrollObject = payrollService.employeePayrollMap.get(emp_id);
        tempEmployeePayrollObject.basic_pay = newSalary;
        payrollService.employeePayrollMap.put(emp_id, tempEmployeePayrollObject);
    }

    protected Connection getConnection() throws IOException, SQLException {
        FileInputStream fin = new FileInputStream("C:\\Users\\Praveen Satya\\IdeaProjects\\PayrollServiceProject\\config.properties");
        Properties prop = new Properties();
        prop.load(fin);
        return DriverManager.getConnection(prop.getProperty("db.url"), prop.getProperty("db.user"), prop.getProperty("db.password"));
    }

    public void getRecordsFromDB(PayrollService payrollService, Connection con) throws SQLException {
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
    }

    public void checkRecordPresentInMap(PayrollService payrollService, int emp_id) throws RecordNotFoundException {
        if (!payrollService.employeePayrollMap.containsKey(emp_id))
            throw new RecordNotFoundException("Records not found");
    }


}
