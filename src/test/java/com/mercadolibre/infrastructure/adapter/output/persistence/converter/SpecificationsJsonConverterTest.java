package com.mercadolibre.infrastructure.adapter.output.persistence.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SpecificationsJsonConverter")
class SpecificationsJsonConverterTest {

    private SpecificationsJsonConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SpecificationsJsonConverter();
    }

    @Nested
    @DisplayName("convertToDatabaseColumn")
    class ConvertToDatabaseColumn {

        @Test
        @DisplayName("returns null when attribute is null")
        void nullAttribute_returnsNull() {
            String result = converter.convertToDatabaseColumn(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns null when attribute is empty map")
        void emptyMap_returnsNull() {
            String result = converter.convertToDatabaseColumn(new HashMap<>());
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns JSON string for valid map with string values")
        void validMap_returnsJsonString() {
            Map<String, Object> specs = Map.of(
                    "color", "red",
                    "weight", "1.5"
            );

            String result = converter.convertToDatabaseColumn(specs);

            assertThat(result).isNotNull();
            assertThat(result).contains("\"color\"");
            assertThat(result).contains("\"red\"");
            assertThat(result).contains("\"weight\"");
            assertThat(result).contains("1.5");
        }

        @Test
        @DisplayName("returns JSON string for map with nested structure")
        void mapWithNestedStructure_returnsJsonString() {
            Map<String, Object> specs = new HashMap<>();
            specs.put("dimensions", Map.of("width", 10, "height", 20));

            String result = converter.convertToDatabaseColumn(specs);

            assertThat(result).isNotNull();
            assertThat(result).contains("\"dimensions\"");
            assertThat(result).contains("\"width\"");
            assertThat(result).contains("10");
        }
    }

    @Nested
    @DisplayName("convertToEntityAttribute")
    class ConvertToEntityAttribute {

        @Test
        @DisplayName("returns empty map when dbData is null")
        void nullDbData_returnsEmptyMap() {
            Map<String, Object> result = converter.convertToEntityAttribute(null);
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("returns empty map when dbData is empty string")
        void emptyString_returnsEmptyMap() {
            Map<String, Object> result = converter.convertToEntityAttribute("");
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("returns empty map when dbData is blank string")
        void blankString_returnsEmptyMap() {
            Map<String, Object> result = converter.convertToEntityAttribute("   ");
            assertThat(result).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("returns map when dbData is valid JSON object")
        void validJson_returnsMap() {
            String dbData = "{\"color\":\"blue\",\"size\":\"M\"}";

            Map<String, Object> result = converter.convertToEntityAttribute(dbData);

            assertThat(result).hasSize(2);
            assertThat(result).containsEntry("color", "blue");
            assertThat(result).containsEntry("size", "M");
        }

        @Test
        @DisplayName("returns map with number values when JSON contains numbers")
        void validJsonWithNumbers_returnsMapWithNumbers() {
            String dbData = "{\"quantity\":42,\"price\":19.99}";

            Map<String, Object> result = converter.convertToEntityAttribute(dbData);

            assertThat(result).hasSize(2);
            assertThat(result.get("quantity")).isEqualTo(42);
            assertThat(result.get("price")).isEqualTo(19.99);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when dbData is invalid JSON")
        void invalidJson_throwsIllegalArgumentException() {
            String invalidJson = "{invalid json}";

            assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidJson))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot deserialize specifications from JSON")
                    .hasMessageContaining(invalidJson)
                    .hasCauseInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class);
        }
    }

    @Nested
    @DisplayName("round-trip")
    class RoundTrip {

        @Test
        @DisplayName("convertToEntityAttribute returns same data after convertToDatabaseColumn")
        void roundTrip_preservesData() {
            Map<String, Object> original = Map.of(
                    "key1", "value1",
                    "key2", 100,
                    "key3", true
            );

            String json = converter.convertToDatabaseColumn(original);
            Map<String, Object> restored = converter.convertToEntityAttribute(json);

            assertThat(restored).isEqualTo(original);
        }
    }
}
