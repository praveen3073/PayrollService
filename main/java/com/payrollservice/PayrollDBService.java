package com.payrollservice;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PayrollDBService {
    private static PayrollDBService payrollDBService;
    private PreparedStatement preparedStatement;

    public PayrollDBService() {
        try {
            String query = "select * from employee e, payroll p " +
                    "where e.emp_id = p.emp_id and name = ?";
            preparedStatement = JDBCConnection.getInstance().getConnection().prepareStatement(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static PayrollDBService getInstance() {
        if (payrollDBService == null)
            payrollDBService = new PayrollDBService();
        return payrollDBService;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
}
