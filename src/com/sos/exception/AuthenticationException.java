package com.sos.exception;

public class AuthenticationException extends AppException {
    private static final long serialVersionUID = 1L;
    public AuthenticationException(String message) { super(401, message); }
}
