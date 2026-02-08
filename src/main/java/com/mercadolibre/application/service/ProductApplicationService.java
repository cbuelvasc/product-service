package com.mercadolibre.application.service;

import com.mercadolibre.application.port.input.CompareProductsUseCase;
import com.mercadolibre.application.port.input.ProductField;
import com.mercadolibre.application.port.output.ProductRepository;
import com.mercadolibre.domain.exception.InvalidRequestException;
import com.mercadolibre.domain.exception.ProductNotFoundException;
import com.mercadolibre.domain.model.Product;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApplicationService implements CompareProductsUseCase {

    private static final String PRODUCT_CACHE_NAME = "product";

    private final ProductRepository productRepository;
    
    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true)
    public List<Product> getComparison(List<Long> ids, Set<ProductField> fields) {
        log.info("Getting comparison for products: {}", ids);
        if (ids == null || ids.isEmpty()) {
            throw new InvalidRequestException("At least one product ID is required");
        }

        List<Long> uniqueIds = ids.stream().distinct().toList();
        Cache cache = cacheManager.getCache(PRODUCT_CACHE_NAME);
        if (cache == null) {
            return loadAndOrder(uniqueIds, productRepository.findByIdIn(uniqueIds));
        }

        Map<Long, Product> idToProduct = new LinkedHashMap<>();
        List<Long> missedIds = new ArrayList<>();

        for (Long id : uniqueIds) {
            Product cached = cache.get(Objects.requireNonNull(id), Product.class);
            if (cached != null) {
                idToProduct.put(id, cached);
            } else {
                missedIds.add(id);
            }
        }

        if (!missedIds.isEmpty()) {
            List<Product> loaded = productRepository.findByIdIn(missedIds);
            List<Long> foundIds = loaded.stream().map(Product::getId).toList();
            List<Long> missingIds = missedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
            if (!missingIds.isEmpty()) {
                log.warn("Products not found for comparison: {}", missingIds);
                throw new ProductNotFoundException(missingIds);
            }
            for (Product p : loaded) {
                Long pid = Objects.requireNonNull(p.getId());
                idToProduct.put(pid, p);
                cache.put(pid, p);
            }
        }

        return uniqueIds.stream()
            .map(idToProduct::get)
            .toList();
    }

    private static List<Product> loadAndOrder(List<Long> uniqueIds, List<Product> products) {
        List<Long> foundIds = products.stream().map(Product::getId).toList();
        List<Long> missingIds = uniqueIds.stream()
            .filter(id -> !foundIds.contains(id))
            .toList();
        if (!missingIds.isEmpty()) {
            throw new ProductNotFoundException(missingIds);
        }
        Map<Long, Product> byId = new LinkedHashMap<>();
        for (Product p : products) {
            byId.put(p.getId(), p);
        }
        return uniqueIds.stream()
            .map(byId::get)
            .toList();
    }
}
