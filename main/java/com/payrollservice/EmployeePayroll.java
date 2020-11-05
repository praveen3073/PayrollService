package com.payrollservice;

import java.util.ArrayList;
import java.util.Objects;

public class EmployeePayroll {
    String company_name;
    int emp_id;
    String name;
    String phone;
    String address;
    char gender;
    String start;
    double basic_pay;
    double deductions;
    double taxable_pay;
    double tax;

    public EmployeePayroll(int emp_id, String name, double basic_pay) {
        this.emp_id = emp_id;
        this.name = name;
        this.basic_pay = basic_pay;
        this.phone = "";
        this.address = "";
        this.gender = ' ';
        this.start = "";
    }

    public EmployeePayroll(String name, String phone, String address, char gender, String start, double basic_pay) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.gender = gender;
        this.start = start;
        this.basic_pay = basic_pay;
    }

    double net_pay;
    ArrayList<String> departments;

    @Override
    public int hashCode() {
        return Objects.hash(emp_id, name);
    }

    public EmployeePayroll() {
        this.departments = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "EmployeePayroll { company_name : " + company_name + ", " +
                " emp_id : " + emp_id + ", " +
                " name : " + name + ", " +
                " phone : " + phone + ", " +
                " address : " + address + ", " +
                " gender : " + gender + ", " +
                " date : " + start + ", " +
                " basic_pay : " + basic_pay + ", " +
                " deductions : " + deductions + ", " +
                " taxable_pay : " + taxable_pay + ", " +
                " tax : " + tax + ", " +
                " net_pay: " + net_pay + ", " +
                " departments : " + departments + " }";
    }
}
