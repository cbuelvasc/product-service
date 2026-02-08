package com.mercadolibre.application.service;

import com.mercadolibre.application.port.input.ProductField;
import com.mercadolibre.application.port.output.ProductRepository;
import com.mercadolibre.domain.exception.InvalidRequestException;
import com.mercadolibre.domain.exception.ProductNotFoundException;
import com.mercadolibre.domain.model.Product;
import com.mercadolibre.domain.model.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {

    private static final String PRODUCT_CACHE_NAME = "product";

    @Mock
    private ProductRepository loadProductsPort;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private ProductApplicationService productApplicationService;

    @Test
    void getComparison_returnsProductsInRequestedOrder() {
        when(cacheManager.getCache(PRODUCT_CACHE_NAME)).thenReturn(null);
        Product p1 = product(1L, "Product A", "100.00");
        Product p2 = product(2L, "Product B", "200.00");
        when(loadProductsPort.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(p2, p1));

        var result = productApplicationService.getComparison(List.of(1L, 2L), null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Product A");
        assertThat(result.get(1).getName()).isEqualTo("Product B");
        verify(loadProductsPort).findByIdIn(List.of(1L, 2L));
    }

    @Test
    void getComparison_withFieldFilter_returnsSameProducts() {
        when(cacheManager.getCache(PRODUCT_CACHE_NAME)).thenReturn(null);
        Product p = product(1L, "Phone", "299.99");
        p.setRating(new BigDecimal("4.5"));
        when(loadProductsPort.findByIdIn(List.of(1L))).thenReturn(List.of(p));

        var result = productApplicationService.getComparison(
                List.of(1L),
                Set.of(ProductField.NAME, ProductField.PRICE));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Phone");
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("299.99");
    }

    @Test
    void getComparison_whenProductMissing_throwsProductNotFoundException() {
        when(cacheManager.getCache(PRODUCT_CACHE_NAME)).thenReturn(null);
        Product p1 = product(1L, "A", "1.00");
        when(loadProductsPort.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(p1));

        assertThatThrownBy(() -> productApplicationService.getComparison(List.of(1L, 2L), null))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("2");
    }

    @Test
    void getComparison_whenIdsEmpty_throwsInvalidRequestException() {
        assertThatThrownBy(() -> productApplicationService.getComparison(List.of(), null))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("At least one");
    }

    @Test
    void getComparison_whenIdsNull_throwsInvalidRequestException() {
        assertThatThrownBy(() -> productApplicationService.getComparison(null, null))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("At least one");
    }

    @Test
    void getComparison_whenCacheMiss_loadsFromRepositoryAndPutsInCacheAndIdToProduct() {
        when(cacheManager.getCache(PRODUCT_CACHE_NAME)).thenReturn(cache);
        when(cache.get(1L, Product.class)).thenReturn(null);
        when(cache.get(2L, Product.class)).thenReturn(null);

        Product p1 = product(1L, "Product A", "100.00");
        Product p2 = product(2L, "Product B", "200.00");
        when(loadProductsPort.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(p2, p1));

        var result = productApplicationService.getComparison(List.of(1L, 2L), null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Product A");
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getName()).isEqualTo("Product B");
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(loadProductsPort).findByIdIn(List.of(1L, 2L));
        verify(cache).put(1L, p1);
        verify(cache).put(2L, p2);
    }

    private static Product product(Long id, String name, String price) {
        return Product.builder()
            .id(id)
            .name(name)
            .price(new BigDecimal(price))
            .productType(ProductType.GENERIC)
            .build();
    }
}
