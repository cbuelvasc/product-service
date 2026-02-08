package com.mercadolibre.domain.model;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private String size;

    private String weight;

    private String color;

    private String imageUrl;

    private BigDecimal rating;

    @Builder.Default
    private ProductType productType = ProductType.GENERIC;

    @Builder.Default
    private Map<String, Object> specifications = new java.util.HashMap<>();
}
