package com.trang.gachon.movie.exception;

public class ShowTimeException extends RuntimeException {
    private final String errorCode;

    public ShowTimeException(String message) {
        super(message);
        this.errorCode = "SHOWTIME_ERROR";
    }

    public ShowTimeException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}