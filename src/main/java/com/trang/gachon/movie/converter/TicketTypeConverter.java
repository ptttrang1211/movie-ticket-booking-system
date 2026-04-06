package com.trang.gachon.movie.converter;

import com.trang.gachon.movie.enums.TicketType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter: map TicketType enum ↔ Integer trong DB
 * DB lưu: 1 = NORMAL, 2 = VIP
 */
@Converter(autoApply = false)
public class TicketTypeConverter
        implements AttributeConverter<TicketType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TicketType attribute) {
        return attribute == null ? null : attribute.getCode();
    }          // NORMAL → 1, VIP → 2


    @Override
    public TicketType convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : TicketType.fromCode(dbData);
    }   // 1 → NORMAL, 2 → VIP
    
}