package com.mercadolibre.infrastructure.adapter.input.rest.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response format")
public class ErrorResponse {

    @Schema(
        description = "Timestamp when the error occurred",
        example = "2024-01-15T10:30:00Z"
    )
    private Instant timestamp;

    @Schema(
        description = "HTTP status code",
        example = "400"
    )
    private int status;

    @Schema(
        description = "Error type or category",
        example = "VALIDATION_ERROR"
    )
    private String error;

    @Schema(
        description = "Brief description of the error",
        example = "Invalid input data"
    )
    private String message;

    @Schema(
        description = "Detailed error description",
        example = "The provided event date is in the past"
    )
    private String details;

    @Schema(
        description = "API path where the error occurred",
        example = "/api/event-service/events"
    )
    private String path;

    @Schema(
        description = "List of validation errors (if applicable)"
    )
    private List<ValidationError> validationErrors;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Validation error details")
    public static class ValidationError {

        @Schema(
            description = "Field name that failed validation",
            example = "startDate"
        )
        private String field;

        @Schema(
            description = "Rejected value",
            example = "2023-01-01T10:00:00"
        )
        private Object rejectedValue;

        @Schema(
            description = "Validation error message",
            example = "Event start date cannot be in the past"
        )
        private String message;
    }
}
