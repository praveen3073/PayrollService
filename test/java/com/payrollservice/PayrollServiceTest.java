package com.payrollservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PayrollServiceTest {
    private PayrollService payrollService;
    private CrudOperations crudOperations;

    @Before
    public void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 4001;
        payrollService = new PayrollService();
        crudOperations = new CrudOperations();
    }

    @Test
    public void givenADatabase_WhenUpdated_ShouldSync() {
        crudOperations.updateSalaryByName(payrollService, "Mani", 180000);
        boolean result = payrollService.checkIfSynced();
        Assert.assertTrue(result);
    }

    @Test
    public void givenACreateSqlQuery_WhenExecutedSuccessfully_ShouldCommit() {
        try {
            payrollService.createRecordInDB(1, "Shalu", "6654321223", "Jodhpur", 'F', "2018-02-02", 800000);
            boolean result = payrollService.checkIfSynced();
            Assert.assertTrue(result);
        } catch (RecordsNotFoundException | RecordAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void givenEmployeePayrollInDB_WhenRetrieved_ShouldMatchEmployeeCountInLocalEmployeePayrollMap() {
        crudOperations.sync(payrollService);
        int employeeCountInLocalEmployeeMap = payrollService.employeePayrollMap.size();
        int employeeCountInDB = crudOperations.readCountOfEmployees();
        Assert.assertEquals(employeeCountInLocalEmployeeMap, employeeCountInDB);
    }

    @Test
    public void givenNewSalaryForEmployee_WhenUpdated_ShouldMatch() {
        try {
            // testSalary1
            double updatedSalaryAsRetrievedFromDB = payrollService.updateSalaryForEmployee("Shalu", 100000);
            Assert.assertEquals(100000, updatedSalaryAsRetrievedFromDB, 0.0);

            // testSalary2
            updatedSalaryAsRetrievedFromDB = payrollService.updateSalaryForEmployee("Shalu", 220000);
            Assert.assertEquals(220000, updatedSalaryAsRetrievedFromDB, 0.0);

        } catch (RecordsNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void givenDateRange_WhenRetrieved_ShouldMatchEmployeeCount() {
        int employeeCountInLocalEmployeeMap = payrollService.getEmployeeCountBetweenDatesInLocalEmployeeMap("2018-01-01", "2019-01-01");
        int employeeCountInDB = payrollService.getEmployeeCountBetweenDatesInDB("2018-01-01", "2019-01-01");
        Assert.assertEquals(employeeCountInLocalEmployeeMap, employeeCountInDB);
    }

    @Test
    public void givenPayrollData_WhenAverageSalaryRetrievedByGender_ShouldReturnProperValue() {
        double avgFemaleEmployeeSalaryInLocalEmployeeMap = payrollService.getAverageFemaleSalaryInLocalEmployeeMap();
        double avgMaleEmployeeSalaryInLocalEmployeeMap = payrollService.getAverageMaleSalaryInLocalEmployeeMap();
        double avgFemaleEmployeeSalaryInDB = payrollService.getAverageFemaleSalaryInDB();
        double avgMaleEmployeeSalaryInDB = payrollService.getAverageMaleSalaryInDB();
        Assert.assertEquals(avgFemaleEmployeeSalaryInLocalEmployeeMap, avgFemaleEmployeeSalaryInDB, 0.0);
        Assert.assertEquals(avgMaleEmployeeSalaryInLocalEmployeeMap, avgMaleEmployeeSalaryInDB, 0.0);
    }

    @Test
    public void given3Employees_WhenAddedToDB_ShouldReturnCorrectCount() {
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
        payrollService.deleteEmployeeRecordsFromDB(new ArrayList<String>(Arrays.asList("TestName1", "TestName2", "TestName3")));
    }

    @Test
    public void given3Names_WhenSalariesUpdatedIn_ShouldBeSynced() {
        try {
            PayrollService payrollService = new PayrollService();
            Instant start = Instant.now();
            String[] names = new String[]{"Rahul", "Ganesh", "Mani"};
            double[] newSalaries = new double[]{6000000, 6000000, 6000000};
            payrollService.updateMultipleSalariesInDB(names, newSalaries);
            Thread.sleep(1000);
            Instant end = Instant.now();
            System.out.println("Duration Without Thread: " + Duration.between(start, end));
            boolean output = payrollService.checkIfSynced();
            Assert.assertTrue(output);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenEmployee_WhenAddedToJSONServer_ShouldReturnAddedEmployee() {
        try {
            Response addedEmployeeResponse = addEmployeeToJsonServer(33, "Rakesh", 2000);
            addedEmployeeResponse.then()
                    .body("id", Matchers.is(33))
                    .body("name", Matchers.is("Rakesh"));
        } catch (JsonServerException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void givenEmployees_WhenExecutedSuccessfully_ShouldReturnTrue() {
        ArrayList<EmployeePayroll> employeeList = new ArrayList<>(Arrays.asList(new EmployeePayroll(21, "Rakesh", 2000),
                                                                            new EmployeePayroll(22, "Manikanta", 10000),
                                                                            new EmployeePayroll(23, "Surya", 22000)));
        boolean result = addMultipleEmployeesToJsonServerUsingMultiThreading(employeeList);
        Assert.assertTrue(result);
    }

    @Test
    public void givenEmployee_WhenPut_ShouldReturnUpdatedEmployee() {
        try {
            Response updateEmployeeResponse = updateEmployeeInJsonServer(23, "Surya", 250000);
            updateEmployeeResponse.then()
                    .body("id", Matchers.is(23))
                    .body("name", Matchers.is("Surya"));
        } catch (JsonServerException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void onRetrievingFromJsonServer_ShouldReturnCorrectStatusCode() {
        int statusCode = retrieveEmployeeFromJsonServer();
        Assert.assertEquals(statusCode, 200);
    }

    @Test
    public void givenEmployeeId_OnDelete_ShouldReturnSuccessStatus() {
        try {
            int statusCode = deleteEmployeeJsonServer(21);
            Assert.assertEquals(200, statusCode);
        } catch (JsonServerException e) {
            System.out.println(e.getMessage());
        }
    }
    public Response getEmployeeList() {
        Response response = RestAssured.get("/employees/list");
        System.out.println(response.asString());
        return response;
    }

    public Response addEmployeeToJsonServer(int emp_id, String name, double basic_pay) throws JsonServerException {
        Response response = RestAssured.get("/employees/" + emp_id);
        if (response.getStatusCode() == 404) {
            EmployeePayroll employeePayroll = new EmployeePayroll(emp_id, "Rakesh", 2000);
            return RestAssured.given().contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"id\": " + employeePayroll.emp_id + ", \"name\": \"" + employeePayroll.name + "\",\"salary\": \"" + employeePayroll.basic_pay + "\"}")
                    .when()
                    .post("/employees/create");
        } else
            throw new JsonServerException("Emp ID " + emp_id + " already exists in json server");
    }

    private boolean addMultipleEmployeesToJsonServerUsingMultiThreading(ArrayList<EmployeePayroll> employeeList) {
        HashMap<Integer, Boolean> employeeAdditionStatus = new HashMap<>();
        for (EmployeePayroll employee : employeeList) {
            Runnable task = () -> {
                employeeAdditionStatus.put(employee.hashCode(), false);
                try {
                    addEmployeeToJsonServer(employee.emp_id, employee.name, employee.basic_pay);
                } catch (JsonServerException e) {
                    System.out.println(e.getMessage());;
                }
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
        return true;
    }

    public Response updateEmployeeInJsonServer(int emp_id, String name, double basic_pay) throws JsonServerException {
        Response response = RestAssured.get("/employees/" + emp_id);
        if (response.getStatusCode() == 200) {
            return RestAssured.given().contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"name\": \""+ name +"\", \"salary\": \""+ basic_pay +"\"}")
                    .when()
                    .put("/employees/update/" + emp_id);
        }
        else
            throw new JsonServerException("Emp ID " + emp_id + " doesn't exist in Json server");
    }

    public int deleteEmployeeJsonServer(int empId) throws JsonServerException {
        Response response = RestAssured.get("/employees/" + empId);
        if (response.getStatusCode() == 200) {
            Response deleteResponse = RestAssured.delete("/employees/delete/" + empId);
            return deleteResponse.statusCode();
        } else
            throw new JsonServerException("Emp ID " + empId + " not found in Json Server");
    }

    public int retrieveEmployeeFromJsonServer() {
        Response employeeList = getEmployeeList();
        JSONArray jsonArray = new JSONArray(employeeList.asString());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            EmployeePayroll tempEmpObj = new EmployeePayroll(jsonObject.getInt("id"), jsonObject.getString("name"),
                    jsonObject.getDouble("salary"));
            payrollService.employeePayrollMap.put(jsonObject.getInt("id"), tempEmpObj);
        }
        return employeeList.statusCode();
    }
}
