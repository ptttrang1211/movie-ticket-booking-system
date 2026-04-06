package com.trang.gachon.movie.exception;

public class PendingAccountException extends RuntimeException {
    private final String email;

    public PendingAccountException(String message, String email) {
        super(message);
        this.email = email;
    }

    public String getEmail() { return email; }
}