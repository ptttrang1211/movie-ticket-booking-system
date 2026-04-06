package com.trang.gachon.movie.enums;

public enum MovieGenre {
    ACTION("Hành động"),
    COMEDY("Hài hước"),
    ROMANCE("Tình cảm"),
    HORROR("Kinh dị"),
    ANIMATION("Hoạt hình"),
    SCI_FI("Khoa học viễn tưởng"),
    DOCUMENTARY("Tài liệu"),
    THRILLER("Giật gân");

    private final String displayName;

    MovieGenre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Tìm enum từ displayName (dùng khi đọc từ DB cũ) */
    public static MovieGenre fromDisplayName(String name) {
        for (MovieGenre g : values()) {
            if (g.displayName.equalsIgnoreCase(name)) return g;
        }
        throw new IllegalArgumentException("Unknown genre: " + name);
    }
}
 