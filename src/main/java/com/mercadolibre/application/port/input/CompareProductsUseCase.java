package com.mercadolibre.application.port.input;

import com.mercadolibre.domain.model.Product;

import java.util.List;
import java.util.Set;

public interface CompareProductsUseCase {

    /**
     * Retrieves products for comparison in the requested order.
     *
     * @param ids    list of product IDs (non-empty)
     * @param fields optional fields to include; if null or empty, all fields are considered
     * @return list of products in the same order as ids
     */
    List<Product> getComparison(List<Long> ids, Set<ProductField> fields);
}
