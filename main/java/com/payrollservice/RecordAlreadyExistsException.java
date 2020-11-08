package com.payrollservice;

public class RecordAlreadyExistsException extends Exception{
    public RecordAlreadyExistsException(String message) {
        super(message);
    }
}
