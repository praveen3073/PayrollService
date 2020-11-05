package com.payrollservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

public class PayrollServiceTest {
    @Test
    public void givenADatabase_WhenUpdated_ShouldSync() {
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            crudOperations.readAll(payrollService);
            crudOperations.updateSalaryByName(payrollService, "Mani", 180000);
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
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            crudOperations.sync(payrollService);
            if (!payrollService.employeePayrollMap.containsKey(payrollService.getEmpIdByName("Shalu")))
                crudOperations.createRecord(payrollService, 1, "Shalu", "6654321223", "Jodhpur", 'F', "2018-02-02", 800000);
            boolean result = payrollService.checkIfSynced();
            Assert.assertTrue(result);
        } catch (RecordsNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenEmployeePayrollInDB_WhenRetreived_ShouldMatchEmployeeCount() {
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            crudOperations.sync(payrollService);
            int employeeCount = payrollService.employeePayrollMap.size();
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "select count(emp_id) from employee";
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            Assert.assertEquals(employeeCount, resultSet.getInt("count(emp_id)"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenNewSalaryForEmployee_WhenUpdated_ShouldMatch() {
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();

            // testSalary1
            crudOperations.updateSalaryByName(payrollService, "Shalu", 100000);
            int emp_id = payrollService.getEmpIdByName("Shalu");
            String query = "select basic_pay from payroll where emp_id = " + emp_id;
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).basic_pay, resultSet.getDouble("basic_pay"), 0.0);
            Assert.assertEquals(100000, payrollService.employeePayrollMap.get(emp_id).basic_pay, 0.0);
            Assert.assertEquals(100000, resultSet.getDouble("basic_pay"), 0.0);

            // testSalary2
            crudOperations.updateSalaryByName(payrollService, "Shalu", 200000);
            emp_id = payrollService.getEmpIdByName("Shalu");
            query = "select basic_pay from payroll where emp_id = " + emp_id;
            resultSet = stmt.executeQuery(query);
            resultSet.next();
            Assert.assertEquals(payrollService.employeePayrollMap.get(emp_id).basic_pay, resultSet.getDouble("basic_pay"), 0.0);
            Assert.assertEquals(200000, payrollService.employeePayrollMap.get(emp_id).basic_pay, 0.0);
            Assert.assertEquals(200000, resultSet.getDouble("basic_pay"), 0.0);
        } catch (SQLException | RecordsNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenDateRange_WhenRetrieved_ShouldMatchEmployeeCount() {
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            int expectedCount = 0;
            crudOperations.readByDate(payrollService, "2018-01-01", "2019-01-01");
            for (EmployeePayroll employeePayroll : payrollService.employeePayrollMap.values()) {
                if (employeePayroll.start.compareTo("2018-01-01") > 0 &&
                        employeePayroll.start.compareTo("2019-01-01") < 0)
                    expectedCount++;
            }
            String query = "select count(emp_id) from employee where start between cast('2018-01-01' as date) and cast('2019-01-01' as date)";
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            Assert.assertEquals(expectedCount, resultSet.getInt("count(emp_id)"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenPayrollData_WhenAverageSalaryRetrievedByGender_ShouldReturnProperValue() {
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            crudOperations.readSalaryStatsByGender(payrollService);
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            double avgSalaryFemale;
            double avgSalaryMale;
            double totalSalaryFemale = 0;
            double totalSalaryMale = 0;
            int countFemale = 0;
            int countMale = 0;
            for (EmployeePayroll employeePayroll : payrollService.employeePayrollMap.values()) {
                if (employeePayroll.gender == 'M' || employeePayroll.gender == 'm') {
                    totalSalaryMale += employeePayroll.basic_pay;
                    countMale++;
                } else if (employeePayroll.gender == 'F' || employeePayroll.gender == 'f') {
                    totalSalaryFemale += employeePayroll.basic_pay;
                    countFemale++;
                }
            }
            avgSalaryFemale = totalSalaryFemale / countFemale;
            avgSalaryMale = totalSalaryMale / countMale;
            String query = "SELECT employee.gender, " +
                    "AVG(p.basic_pay) as avg " +
                    "FROM " +
                    "(SELECT emp_id, basic_pay FROM payroll) p, " +
                    "employee " +
                    "WHERE employee.emp_id = p.emp_id " +
                    "GROUP BY employee.gender";
            ResultSet resultSet = stmt.executeQuery(query);
            resultSet.next();
            Assert.assertEquals(avgSalaryMale, resultSet.getDouble("avg"), 0.0);
            resultSet.next();
            Assert.assertEquals(avgSalaryFemale, resultSet.getDouble("avg"), 0.0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void given3Employees_WhenAddedToDB_ShouldReturnCorrectCount() {
        try {
            PayrollService payrollService = new PayrollService();
            CrudOperations crudOperations = new CrudOperations();
            int countBeforeInsertion = crudOperations.readCountOfEmployees();
            Instant start = Instant.now();
            ArrayList<EmployeePayroll> employeeList = new ArrayList<>();
            employeeList.add(new EmployeePayroll("TestName1", "6654321223", "Jodhpur", 'F', "2018-02-02", 800000));
            employeeList.add(new EmployeePayroll("TestName2", "6654321223", "Jodhpur", 'F', "2018-02-02", 800000));
            employeeList.add(new EmployeePayroll("TestName3", "6654321223", "Jodhpur", 'F', "2018-02-02", 800000));
            payrollService.addEmployeesToDBWithThread(employeeList);
            Instant end = Instant.now();
            System.out.println("Duration Without Thread: " + Duration.between(start, end));
            int countAfterInsertion = crudOperations.readCountOfEmployees();
            Assert.assertEquals(countBeforeInsertion + 3, countAfterInsertion);
            Connection con = JDBCConnection.getInstance().getConnection();
            Statement stmt = con.createStatement();
            String query = "delete from employee where name in ('TestName1', 'TestName2', 'TestName3')";
            stmt.executeUpdate(query);
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void given3Names_WhenSalariesUpdatedIn_ShouldBeSynced() {
        PayrollService payrollService = new PayrollService();
        Instant start = Instant.now();
        String[] names = new String[]{"Rahul", "Ganesh", "Mani"};
        double[] newSalaries = new double[]{6000000, 6000000, 6000000};
        payrollService.updateMultipleSalariesInDB(names, newSalaries);
        Instant end = Instant.now();
        System.out.println("Duration Without Thread: " + Duration.between(start, end));
        boolean output = payrollService.checkIfSynced();
        Assert.assertTrue(output);
    }

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4001;
    }

    public Response getEmployeeList() {
        Response response = RestAssured.get("/employees/list");
        System.out.println(response.asString());
        return response;
    }

    @Test
    public void givenEmployee_WhenAddedToJSONServer_ShouldReturnAddedEmployee() {
        int emp_id = 33;
        Response response = RestAssured.get("/employees/" + emp_id);
        if (response.getStatusCode() == 404)
        {
            EmployeePayroll employeePayroll = new EmployeePayroll(emp_id, "Rakesh", 2000);
            RestAssured.given().contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"id\": " + employeePayroll.emp_id + ", \"name\": \"" + employeePayroll.name + "\",\"salary\": \"" + employeePayroll.basic_pay + "\"}")
                    .when()
                    .post("/employees/create")
                    .then()
                    .body("id", Matchers.is(emp_id))
                    .body("name", Matchers.is("Rakesh"));
        }
        else
            System.out.println("Emp ID " + emp_id + " already exists in json server");
    }

    @Test
    public void givenEmployees_WhenAdded_ShouldReturnAddedEmployees() {
        int emp_id1 = 21;
        int emp_id2 = 22;
        int emp_id3 = 23;
        ArrayList<EmployeePayroll> employeeList = new ArrayList<>();
        employeeList.add(new EmployeePayroll(emp_id1, "Rakesh", 2000));
        employeeList.add(new EmployeePayroll(emp_id2, "Manikanta", 10000));
        employeeList.add(new EmployeePayroll(emp_id3, "Surya", 22000));
        HashMap<Integer, Boolean> employeeAdditionStatus = new HashMap<>();
        for(EmployeePayroll employee : employeeList) {
            Runnable task = () -> {
                employeeAdditionStatus.put(employee.hashCode(), false);
                Response response = RestAssured.get("/employees/" + employee.emp_id);
                if (response.getStatusCode() == 404)
                {
                    RestAssured.given().contentType(ContentType.JSON)
                            .accept(ContentType.JSON)
                            .body("{\"id\": " + employee.emp_id + ", \"name\": \"" + employee.name + "\",\"salary\": \"" + employee.basic_pay + "\"}")
                            .when()
                            .post("/employees/create")
                            .then()
                            .body("id", Matchers.is(employee.emp_id))
                            .body("name", Matchers.is(employee.name));
                }
                else
                    System.out.println("Emp ID " + employee.emp_id + " already exists in json server");
                employeeAdditionStatus.put(employee.hashCode(), true);
            };
            Thread thread = new Thread(task);
            thread.start();
        }
        while (employeeAdditionStatus.containsValue(false)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void givenEmployee_WhenPut_ShouldReturnUpdatedEmployee() {
        int emp_id = 23;
        Response response = RestAssured.get("/employees/" + emp_id);
        if (response.getStatusCode() == 200)
        {
            RestAssured.given().contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"name\": \"Surya\", \"salary\": \"250000\"}")
                    .when()
                    .put("/employees/update/" + emp_id)
                    .then()
                    .body("id", Matchers.is(emp_id))
                    .body("name", Matchers.is("Surya"))
                    .body("salary", Matchers.is("250000"));
        }
        else
            System.out.println("Emp ID " + emp_id + " doesn't exist in Json server");
    }

    @Test
    public void onRetrievingFromJsonServer_ShouldReturnCorrectStatusCode() {
        PayrollService payrollService = new PayrollService();
        Response employeeList = getEmployeeList();
        JSONArray jsonArray = new JSONArray(employeeList.asString());
        for(int i=0; i<jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            EmployeePayroll tempEmpObj = new EmployeePayroll(jsonObject.getInt("id"), jsonObject.getString("name"),
                    jsonObject.getDouble("salary"));
            payrollService.employeePayrollMap.put(jsonObject.getInt("id"), tempEmpObj);
        }
        int statusCode = employeeList.statusCode();
        Assert.assertEquals(statusCode, 200);
    }

    @Test
    public void givenEmployeeId_OnDelete_ShouldReturnSuccessStatus() {
        int empId = 21;
        Response response = RestAssured.get("/employees/" + empId);
        if (response.getStatusCode() == 200) {
            Response deleteResponse = RestAssured.delete("/employees/delete/" + empId);
            int statusCode = deleteResponse.statusCode();
            Assert.assertEquals(200, statusCode);
        }
        else
            System.out.println("Emp ID " + empId + " not found in Json Server");
    }
}
