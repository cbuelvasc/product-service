package com.mercadolibre.infrastructure.adapter.input.rest.mapper;

import com.mercadolibre.application.port.input.ProductField;
import com.mercadolibre.domain.model.Product;
import com.mercadolibre.infrastructure.adapter.input.rest.response.ProductResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse fromProductToProductResponse(Product product) {
        return fromProductToProductResponse(product, null);
    }

    public ProductResponse fromProductToProductResponse(Product product, Set<ProductField> requestedFields) {
        boolean filter = requestedFields != null && !requestedFields.isEmpty();

        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder();

        if (!filter || requestedFields.contains(ProductField.ID)) {
            builder.id(product.getId());
        }
        if (!filter || requestedFields.contains(ProductField.NAME)) {
            builder.name(product.getName());
        }
        if (!filter || requestedFields.contains(ProductField.DESCRIPTION)) {
            builder.description(product.getDescription());
        }
        if (!filter || requestedFields.contains(ProductField.PRICE)) {
            builder.price(product.getPrice());
        }
        if (!filter || requestedFields.contains(ProductField.SIZE)) {
            builder.size(product.getSize());
        }
        if (!filter || requestedFields.contains(ProductField.WEIGHT)) {
            builder.weight(product.getWeight());
        }
        if (!filter || requestedFields.contains(ProductField.COLOR)) {
            builder.color(product.getColor());
        }
        if (!filter || requestedFields.contains(ProductField.IMAGE_URL)) {
            builder.imageUrl(product.getImageUrl());
        }
        if (!filter || requestedFields.contains(ProductField.RATING)) {
            builder.rating(product.getRating());
        }
        if (!filter || requestedFields.contains(ProductField.PRODUCT_TYPE)) {
            builder.productType(product.getProductType() != null ? product.getProductType().name() : null);
        }
        if (!filter || requestedFields.contains(ProductField.SPECIFICATIONS)) {
            Map<String, Object> specs = product.getSpecifications();
            builder.specifications(specs == null || specs.isEmpty() ? null : new HashMap<>(specs));
        }

        return builder.build();
    }

    public List<Long> parseIds(String ids) {
        if (ids == null || ids.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(ids.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(s -> {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid ID: " + s);
                }
            })
            .toList();
    }

    public Set<ProductField> parseFields(String fields) {
        if (fields == null || fields.isBlank()) {
            return null;
        }
        Set<ProductField> result = Arrays.stream(fields.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(ProductField::fromString)
            .filter(f -> f != null)
            .collect(Collectors.toSet());
        return result.isEmpty() ? null : result;
    }
}
