package com.mercadolibre.infrastructure.adapter.input.rest.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Product response DTO for the comparison API.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Product detail for comparison")
public class ProductResponse {

    @Schema(description = "Unique product identifier", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Smartphone Alpha X1")
    private String name;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Price", example = "449.99")
    private BigDecimal price;

    @Schema(description = "Size", example = "6.2\"")
    private String size;

    @Schema(description = "Weight", example = "180g")
    private String weight;

    @Schema(description = "Color", example = "Black")
    private String color;

    @Schema(description = "Product image URL")
    private String imageUrl;

    @Schema(description = "Product rating", example = "4.5")
    private BigDecimal rating;

    @Schema(description = "Product type (GENERIC, SMARTPHONE, etc.)", example = "SMARTPHONE")
    private String productType;

    @Schema(description = "Dynamic specifications by product type", additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    private Map<String, Object> specifications;
}
