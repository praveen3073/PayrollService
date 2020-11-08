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

    public void updateMultipleSalariesInDB(String[] names, double[] newSalaries) {
        HashMap<String, Boolean> salaryUpdatedStatus = new HashMap<>();
        for(int i=0; i<names.length; i++) {
            int finalI = i;
            Runnable task = () -> {
                salaryUpdatedStatus.put(names[finalI], false);
                CrudOperations crudOperations = new CrudOperations();
                crudOperations.updateSalaryByName(this, names[finalI], newSalaries[finalI]);
                salaryUpdatedStatus.put(names[finalI], true);
            };
            Thread thread = new Thread(task);
            thread.start();
        }
        while (salaryUpdatedStatus.containsValue(false)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void createRecordInDB(int company_id, String name, String phone, String address, char gender, String startDate, double basic_pay) throws RecordAlreadyExistsException, RecordsNotFoundException {
        CrudOperations crudOperations = new CrudOperations();
        crudOperations.sync(this);
        if (!this.employeePayrollMap.containsKey(this.getEmpIdByName("Shalu")))
            crudOperations.createRecord(this, company_id, name, phone, address, gender, startDate, basic_pay);
        else
            throw new RecordAlreadyExistsException("The record with name " + name + " already exists in DB");
    }

    public double updateSalaryForEmployee(String name, double newSalary) throws RecordsNotFoundException {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            CrudOperations crudOperations = new CrudOperations();
            crudOperations.updateSalaryByName(this, name, newSalary);
            int emp_id = this.getEmpIdByName("Shalu");
            String query = "select basic_pay from payroll where emp_id = " + emp_id;
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            return resultSet.getDouble("basic_pay");
        } catch (SQLException | RecordsNotFoundException e) {
            System.out.println(e.getMessage());
        }
        throw new RecordsNotFoundException("The record for " + name + "doesn't exist in DB");
    }

    public int getEmployeeCountBetweenDatesInLocalEmployeeMap(String dateLowerLimit, String dateUpperLimit) {
        CrudOperations crudOperations = new CrudOperations();
        int count = 0;
        crudOperations.readByDate(this, "2018-01-01", "2019-01-01");
        for (EmployeePayroll employeePayroll : this.employeePayrollMap.values()) {
            if (employeePayroll.start.compareTo("2018-01-01") > 0 &&
                    employeePayroll.start.compareTo("2019-01-01") < 0)
                count++;
        }
        return count;
    }

    public int getEmployeeCountBetweenDatesInDB(String dateLowerLimit, String dateUpperLimit) {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "select count(emp_id) from employee where start between cast('2018-01-01' as date) and cast('2019-01-01' as date)";
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            return resultSet.getInt("count(emp_id)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getAverageFemaleSalaryInLocalEmployeeMap() {
        try {
            CrudOperations crudOperations = new CrudOperations();
            crudOperations.readSalaryStatsByGender(this);
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            double avgSalaryFemale;
            double totalSalaryFemale = 0;
            int countFemale = 0;
            for (EmployeePayroll employeePayroll : this.employeePayrollMap.values()) {
                if (employeePayroll.gender == 'F' || employeePayroll.gender == 'f') {
                    totalSalaryFemale += employeePayroll.basic_pay;
                    countFemale++;
                }
            }
            return totalSalaryFemale / countFemale;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getAverageMaleSalaryInLocalEmployeeMap() {
        try {
            CrudOperations crudOperations = new CrudOperations();
            crudOperations.readSalaryStatsByGender(this);
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            double avgSalaryMale;
            double totalSalaryMale = 0;
            int countMale = 0;
            for (EmployeePayroll employeePayroll : this.employeePayrollMap.values()) {
                if (employeePayroll.gender == 'M' || employeePayroll.gender == 'm') {
                    totalSalaryMale += employeePayroll.basic_pay;
                    countMale++;
                }
            }
            return totalSalaryMale / countMale;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getAverageFemaleSalaryInDB() {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "SELECT employee.gender, " +
                    "AVG(p.basic_pay) as avg " +
                    "FROM " +
                    "(SELECT emp_id, basic_pay FROM payroll) p, " +
                    "employee " +
                    "WHERE employee.emp_id = p.emp_id " +
                    "and gender = 'F'";
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            return resultSet.getDouble("avg");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getAverageMaleSalaryInDB() {
        try {
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "SELECT employee.gender, " +
                    "AVG(p.basic_pay) as avg " +
                    "FROM " +
                    "(SELECT emp_id, basic_pay FROM payroll) p, " +
                    "employee " +
                    "WHERE employee.emp_id = p.emp_id " +
                    "and gender = 'M'";
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            return resultSet.getDouble("avg");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void deleteEmployeeRecordsFromDB(ArrayList<String> names) {
        try {
            CrudOperations crudOperations = new CrudOperations();
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query;
            for(String name : names) {
                query = "delete from employee where name = '"+ name +"'";
                stmt.executeUpdate(query);
                if(con.getAutoCommit() == false)
                    con.commit();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
