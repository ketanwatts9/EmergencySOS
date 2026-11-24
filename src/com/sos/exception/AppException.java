package com.sos.exception;

/** Checked exception: every caller must either catch it or declare "throws". */
public class AppException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int status;   // HTTP-style code (400, 401, 404 ...)

    public AppException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() { return status; }
}
