package com.trang.gachon.movie.converter;

import com.trang.gachon.movie.enums.AccountStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
/**
 * JPA Converter: map AccountStatus enum ↔ Integer trong DB
 * DB lưu: 1 = ACTIVE, 2 = LOCKED
 */
@Converter
public class AccountStatusConverter
        implements AttributeConverter<AccountStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AccountStatus status) {
        if (status == null) return null;
        return status.getCode();        // ACTIVE → 1, LOCKED → 2
    }

    @Override
    public AccountStatus convertToEntityAttribute(Integer code) {
        if (code == null) return null;
        return AccountStatus.fromCode(code);  // 1 → ACTIVE, 2 → LOCKED
    }
}