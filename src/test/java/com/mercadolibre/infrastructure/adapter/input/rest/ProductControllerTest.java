package com.mercadolibre.infrastructure.adapter.input.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the product comparison endpoint.
 * Uses path within context: /products/compare (context-path is applied by the server).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    /** Path within servlet context (context-path is stripped by MockMvc when matching controllers). */
    private static final String COMPARE_PATH = "/products/compare";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void compare_returnsMultipleProductsWithAllFields() throws Exception {
        mockMvc.perform(get(COMPARE_PATH).param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products", hasSize(2)))
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].name").value("Smartphone Alpha X1"))
                .andExpect(jsonPath("$.products[0].price").value(449.99))
                .andExpect(jsonPath("$.products[0].specifications.batteryCapacityMah").value(5000))
                .andExpect(jsonPath("$.products[1].id").value(2))
                .andExpect(jsonPath("$.products[1].name").value("Smartphone Beta Pro"));
    }

    @Test
    void compare_withFields_returnsOnlyRequestedFields() throws Exception {
        mockMvc.perform(get(COMPARE_PATH)
                        .param("ids", "1,2")
                        .param("fields", "name,price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products", hasSize(2)))
                .andExpect(jsonPath("$.products[0].name").value("Smartphone Alpha X1"))
                .andExpect(jsonPath("$.products[0].price").value(449.99))
                .andExpect(jsonPath("$.products[0].description").doesNotExist())
                .andExpect(jsonPath("$.products[0].specifications").doesNotExist());
    }

    @Test
    void compare_whenProductNotFound_returns404() throws Exception {
        mockMvc.perform(get(COMPARE_PATH).param("ids", "1,999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("One or more products were not found"))
                .andExpect(jsonPath("$.details", containsString("do not exist")))
                .andExpect(jsonPath("$.details", containsString("999")))
                .andExpect(jsonPath("$.path").value(COMPARE_PATH))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].field").value("ids"))
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").value(999))
                .andExpect(jsonPath("$.validationErrors[0].message", containsString("999")));
    }

    @Test
    void compare_whenIdsMissing_returns400() throws Exception {
        mockMvc.perform(get(COMPARE_PATH))
                .andExpect(status().isBadRequest());
    }

    @Test
    void compare_whenIdsEmpty_returns422() throws Exception {
        mockMvc.perform(get(COMPARE_PATH).param("ids", ""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failure"))
                .andExpect(jsonPath("$.details", containsString("required parameters are missing or invalid")))
                .andExpect(jsonPath("$.path").value(COMPARE_PATH))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].field").value("ids"))
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").value(""))
                .andExpect(jsonPath("$.validationErrors[0].message", containsString("required")));
    }

    @Test
    void compare_whenIdsOnlyCommasOrNoValidIds_returns422() throws Exception {
        mockMvc.perform(get(COMPARE_PATH).param("ids", ",,,"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.details", containsString("At least one product ID")))
                .andExpect(jsonPath("$.path").value(COMPARE_PATH));
    }

    @Test
    void compare_whenInvalidIdFormat_returns400() throws Exception {
        mockMvc.perform(get(COMPARE_PATH).param("ids", "1,abc,2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.details", containsString("Invalid ID")))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(1))
                .andExpect(jsonPath("$.validationErrors[0].field").value("ids"))
                .andExpect(jsonPath("$.validationErrors[0].rejectedValue").value("abc"))
                .andExpect(jsonPath("$.validationErrors[0].message", containsString("Invalid ID")));
    }
}
