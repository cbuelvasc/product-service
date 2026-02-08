package com.mercadolibre.infrastructure.adapter.input.rest.impl;

import com.mercadolibre.application.port.input.CompareProductsUseCase;
import com.mercadolibre.application.port.input.ProductField;
import com.mercadolibre.domain.model.Product;
import com.mercadolibre.infrastructure.adapter.input.rest.IProductController;
import com.mercadolibre.infrastructure.adapter.input.rest.mapper.ProductMapper;
import com.mercadolibre.infrastructure.adapter.input.rest.response.ProductListResponse;
import com.mercadolibre.infrastructure.adapter.input.rest.response.ProductResponse;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController implements IProductController {

    private final CompareProductsUseCase compareProductsUseCase;

    private final ProductMapper productMapper;

    @Override
    public ProductListResponse compare(String ids, String fields) {
        log.info("Comparing products with ids: {} and fields: {}", ids, fields);
        List<Long> idList = productMapper.parseIds(ids);
        Set<ProductField> fieldSet = productMapper.parseFields(fields);

        log.info("Parsed ids: {} and fields: {}", idList, fieldSet);

        List<Product> products = compareProductsUseCase.getComparison(idList, fieldSet);

        log.info("Products: {}", products);

        List<ProductResponse> productResponses = products.stream()
            .map(p -> productMapper.fromProductToProductResponse(p, fieldSet))
            .toList();
        return ProductListResponse.builder()
            .products(productResponses)
            .build();
    }
}
