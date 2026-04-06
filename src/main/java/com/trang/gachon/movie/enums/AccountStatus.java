package com.trang.gachon.movie.enums;

public enum AccountStatus {
    ACTIVE(1), LOCKED(2), PENDING(3);
    private final int code;
    AccountStatus(int code) {   this.code = code;   }
    public int getCode() { return code; }
    public static AccountStatus fromCode(int code) {
        for(AccountStatus status : AccountStatus.values()) {
            if(status.getCode() == code) return status;
        }
        throw  new IllegalArgumentException("invalid AccountStatus code " + code);
    }
}
