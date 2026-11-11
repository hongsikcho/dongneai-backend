package com.dongneai.review.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.YearMonth;

/** YearMonth ↔ "YYYY-MM" (CHAR(7)) */
@Converter
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth value) {
        return value == null ? null : value.toString();
    }

    @Override
    public YearMonth convertToEntityAttribute(String value) {
        return value == null ? null : YearMonth.parse(value);
    }
}
