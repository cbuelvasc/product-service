package com.mercadolibre.infrastructure.adapter.output.persistence.mapper;

import com.mercadolibre.domain.model.Product;
import com.mercadolibre.domain.model.ProductType;
import com.mercadolibre.infrastructure.adapter.output.persistence.entity.ProductEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityMapper {

    public Product toDomain(ProductEntity entity) {
        if (entity == null) return null;
        Map<String, Object> specs = entity.getSpecifications();
        return Product.builder()
            .id(entity.getId())
            .name(entity.getName())
            .description(entity.getDescription())
            .price(entity.getPrice())
            .size(entity.getSize())
            .weight(entity.getWeight())
            .color(entity.getColor())
            .imageUrl(entity.getImageUrl())
            .rating(entity.getRating())
            .productType(entity.getProductType() != null ? entity.getProductType() : ProductType.GENERIC)
            .specifications(specs == null || specs.isEmpty() ? new HashMap<>() : new HashMap<>(specs))
            .build();
    }
}
