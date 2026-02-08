package com.mercadolibre.infrastructure.adapter.output.persistence.adapter;

import com.mercadolibre.application.port.output.ProductRepository;
import com.mercadolibre.domain.model.Product;
import com.mercadolibre.infrastructure.adapter.output.persistence.mapper.ProductEntityMapper;
import com.mercadolibre.infrastructure.adapter.output.persistence.repository.ProductJpaRepository;
import com.mercadolibre.infrastructure.adapter.output.persistence.entity.ProductEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    private final ProductEntityMapper productEntityMapper;

    @Override
    public List<Product> findByIdIn(List<Long> ids) {
        List<ProductEntity> entities = productJpaRepository.findByIdIn(ids);
        return entities.stream()
            .map(productEntityMapper::toDomain)
            .toList();
    }
}
