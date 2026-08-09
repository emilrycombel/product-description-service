package com.ercode.productdescription.adapter.out.persistence;

import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Small JSON codec for persisting structured payloads as text columns. Uses Jackson 3 (the Spring Boot 4
 * default); its exceptions are unchecked, so no checked-exception plumbing is needed.
 */
final class JsonSupport {

    private final JsonMapper mapper = JsonMapper.builder().build();

    String toJson(Object value) {
        return mapper.writeValueAsString(value);
    }

    <T> T fromJson(String json, Class<T> type) {
        return mapper.readValue(json, type);
    }

    @SuppressWarnings("unchecked")
    <T> List<T> listFromJson(String json, Class<T> elementType) {
        Class<T[]> arrayType = (Class<T[]>) Array.newInstance(elementType, 0).getClass();
        T[] array = mapper.readValue(json, arrayType);
        return List.of(array);
    }
}
