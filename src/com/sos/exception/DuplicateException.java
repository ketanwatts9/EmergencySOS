package com.sos.exception;

public class DuplicateException extends AppException {
    private static final long serialVersionUID = 1L;
    public DuplicateException(String message) { super(409, message); }
}
