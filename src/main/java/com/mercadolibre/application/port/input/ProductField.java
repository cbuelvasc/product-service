package com.mercadolibre.application.port.input;

/**
 * Field names that the client can request in the comparison endpoint to focus only on relevant details.
 */
public enum ProductField {
    ID("id"),
    NAME("name"),
    DESCRIPTION("description"),
    PRICE("price"),
    SIZE("size"),
    WEIGHT("weight"),
    COLOR("color"),
    IMAGE_URL("imageUrl"),
    RATING("rating"),
    PRODUCT_TYPE("productType"),
    SPECIFICATIONS("specifications");

    private final String value;

    ProductField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ProductField fromString(String s) {
        if (s == null) return null;
        for (ProductField f : values()) {
            if (f.value.equalsIgnoreCase(s)) return f;
        }
        return null;
    }
}
