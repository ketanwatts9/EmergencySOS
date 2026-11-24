package com.sos.exception;

public class InvalidInputException extends AppException {
    private static final long serialVersionUID = 1L;
    public InvalidInputException(String message) { super(400, message); }
}
