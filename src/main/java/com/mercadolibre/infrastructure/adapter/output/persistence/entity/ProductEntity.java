package com.mercadolibre.infrastructure.adapter.output.persistence.entity;

import com.mercadolibre.infrastructure.adapter.output.persistence.converter.SpecificationsJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for products table. Infrastructure detail; domain uses {@link com.mercadolibre.domain.model.Product}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String weight;

    @Column(length = 50)
    private String color;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 50)
    @Builder.Default
    private com.mercadolibre.domain.model.ProductType productType = com.mercadolibre.domain.model.ProductType.GENERIC;

    @Column(name = "specifications", columnDefinition = "clob")
    @Convert(converter = SpecificationsJsonConverter.class)
    @Builder.Default
    private Map<String, Object> specifications = new HashMap<>();
}
