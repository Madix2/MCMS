package com.redcode.mcms.entity;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.StringReader;

@Converter
public class JsonbStringConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) return null;
        try (var reader = Json.createReader(new StringReader(attribute))) {
            JsonStructure json = reader.read();
            return json.toString();
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("deltaDiff must contain valid JSON.", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) { return dbData; }
}
