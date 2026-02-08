package com.mercadolibre.application.port.output;

import com.mercadolibre.domain.model.Product;
import java.util.List;

public interface ProductRepository {

    /**
     * Finds all products whose identifiers are contained in the given list.
     *
     * @param ids list of product identifiers; must not be {@code null}; may be empty
     * @return list of matching products; never {@code null}; may be empty if no matches
     * or if {@code ids} is empty; order is not guaranteed
     */
    List<Product> findByIdIn(List<Long> ids);
}
