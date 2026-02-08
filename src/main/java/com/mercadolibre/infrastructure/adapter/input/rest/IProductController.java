package com.mercadolibre.infrastructure.adapter.input.rest;

import com.mercadolibre.infrastructure.adapter.input.rest.exception.ErrorResponse;
import com.mercadolibre.infrastructure.adapter.input.rest.response.ProductListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Product comparison API contract (API First). Defines endpoint, parameters and OpenAPI documentation.
 */
@Tag(
        name = "Product comparison",
        description = "API to retrieve details of multiple products and compare items."
)
@RequestMapping("products")
public interface IProductController {

    @GetMapping(value = "/compare", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Compare items",
            description = "Retrieves details of multiple products for comparison. " +
                    "The **ids** parameter is required (comma-separated IDs). " +
                    "The optional **fields** parameter limits the response to the specified attributes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comparison retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProductListResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Full comparison (all fields)",
                                            value = """
                                                    {
                                                      "products": [
                                                        {
                                                          "id": 1,
                                                          "name": "Smartphone Alpha X1",
                                                          "description": "Smartphone with AMOLED display and 108MP camera.",
                                                          "price": 449.99,
                                                          "size": "6.2\\"",
                                                          "weight": "180g",
                                                          "color": "Black",
                                                          "imageUrl": "https://example.com/img/alpha-x1.png",
                                                          "rating": 4.5,
                                                          "productType": "SMARTPHONE",
                                                          "specifications": {
                                                            "batteryCapacityMah": 5000,
                                                            "cameraSpecs": "108MP main, 12MP ultra wide, 8MP tele",
                                                            "memoryGb": 8,
                                                            "storageGb": 128,
                                                            "brand": "Alpha",
                                                            "modelVersion": "X1",
                                                            "operatingSystem": "Android 14"
                                                          }
                                                        }
                                                      ]
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Filtered fields (name, price only)",
                                            value = """
                                                    {
                                                      "products": [
                                                        { "name": "Smartphone Alpha X1", "price": 449.99 },
                                                        { "name": "Smartphone Beta Pro", "price": 599.99 }
                                                      ]
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Validation error: required parameter 'ids' missing or empty, or non-numeric ID",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Ids empty or invalid",
                                    value = """
                                            {
                                              "timestamp": "2026-02-08T19:33:00.628578Z",
                                              "status": 422,
                                              "error": "VALIDATION_ERROR",
                                              "message": "Validation failure",
                                              "details": "One or more required parameters are missing or invalid. See validationErrors for details.",
                                              "path": "/api/product-service/products/compare",
                                              "validationErrors": [
                                                {
                                                  "field": "ids",
                                                  "rejectedValue": "",
                                                  "message": "Parameter 'ids' is required"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "One or more product IDs do not exist",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Not Found",
                                    value = """
                                            {
                                              "timestamp": "2026-02-08T19:31:36.912549Z",
                                              "status": 404,
                                              "error": "NOT_FOUND",
                                              "message": "One or more products were not found",
                                              "details": "The following product ID(s) do not exist: [5, 6]",
                                              "path": "/api/product-service/products/compare",
                                              "validationErrors": [
                                                {
                                                  "field": "ids",
                                                  "rejectedValue": 5,
                                                  "message": "Product not found: 5"
                                                },
                                                {
                                                  "field": "ids",
                                                  "rejectedValue": 6,
                                                  "message": "Product not found: 6"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "Internal Server Error",
                                    value = """
                                            {
                                              "timestamp": "2024-01-15T10:30:00Z",
                                              "status": 500,
                                              "error": "INTERNAL_SERVER_ERROR",
                                              "message": "An unexpected error occurred",
                                              "details": "Please contact support if the problem persists",
                                              "path": "/api/product-service/products/compare"
                                            }
                                            """
                            )
                    )
            )
    })
    ProductListResponse compare(
            @RequestParam("ids")
            @Valid
            @NotEmpty(message = "Parameter 'ids' is required")
            @Parameter(description = "Product IDs to compare, comma-separated", required = true, example = "1,2,3")
            String ids,

            @RequestParam(value = "fields", required = false)
            @Valid
            @Parameter(
                    description = "Fields to include in the response (optional). Values: id, name, description, price, size, weight, color, imageUrl, rating, productType, specifications",
                    example = "name,price,rating,specifications"
            )
            String fields
    );
}
