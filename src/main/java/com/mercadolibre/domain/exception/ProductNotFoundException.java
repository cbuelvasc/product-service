package com.mercadolibre.domain.exception;

import java.util.Collection;

public class ProductNotFoundException extends ProductDomainException {

    private final Collection<Long> missingIds;

    public ProductNotFoundException(Collection<Long> missingIds) {
        super("Product(s) not found: " + missingIds);
        this.missingIds = missingIds;
    }

    public Collection<Long> getMissingIds() {
        return missingIds;
    }
}
