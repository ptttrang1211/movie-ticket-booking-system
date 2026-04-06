package com.trang.gachon.movie.enums;

public enum TicketType {
    NORMAL(1), VIP(2);
    private int code;

    TicketType(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
    public static TicketType fromCode(int code) {
        for (TicketType ticketType : TicketType.values()) {
            if (ticketType.getCode() == code) {
                return ticketType;
            }
           
        }
        throw new  IllegalArgumentException("invalid TicketType code " + code);

    }
}
