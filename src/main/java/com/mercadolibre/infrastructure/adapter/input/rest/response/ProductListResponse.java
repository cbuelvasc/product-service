package com.mercadolibre.infrastructure.adapter.input.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Comparison endpoint response: list of products with only the requested fields (when fields parameter is used).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product list result")
public class ProductListResponse {

    @Schema(description = "List of products to compare")
    private List<ProductResponse> products;
}
