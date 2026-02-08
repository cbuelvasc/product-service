package com.mercadolibre.infrastructure.adapter.input.rest.exception;

import com.mercadolibre.domain.exception.InvalidRequestException;
import com.mercadolibre.domain.exception.ProductDomainException;
import com.mercadolibre.domain.exception.ProductNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Global exception handler for the Event Service API.
 * Provides standardized error responses for all exceptions.
 */
@Slf4j
@RestControllerAdvice
@Hidden // Hide from OpenAPI documentation
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ApiResponse(
        responseCode = "422",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
        MethodArgumentNotValidException ex,
        HttpServletRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::mapFieldError)
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .error("VALIDATION_ERROR")
            .message("Invalid input data")
            .details("One or more fields have validation errors")
            .path(request.getRequestURI())
            .validationErrors(validationErrors)
            .build();

        return ResponseEntity.unprocessableEntity()
            .body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ApiResponse(
        responseCode = "422",
        description = "Constraint violation",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
        ConstraintViolationException ex,
        HttpServletRequest request) {

        log.warn("Constraint violation: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
            .stream()
            .map(this::mapConstraintViolation)
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .error("VALIDATION_ERROR")
            .message("Constraint violation")
            .details("One or more constraints were violated")
            .path(request.getRequestURI())
            .validationErrors(validationErrors)
            .build();

        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ApiResponse(
        responseCode = "422",
        description = "Validation error (e.g. required parameter missing or empty)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
        HandlerMethodValidationException ex,
        HttpServletRequest request) {

        log.warn("Method validation error: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            String field = result.getMethodParameter().getParameterName();
            Object rejectedValue = result.getArgument();
            for (MessageSourceResolvable err : result.getResolvableErrors()) {
                String message = err.getDefaultMessage() != null ? err.getDefaultMessage() : "Validation failed";
                validationErrors.add(ErrorResponse.ValidationError.builder()
                    .field(field)
                    .rejectedValue(rejectedValue)
                    .message(message)
                    .build());
            }
        }

        String detailsMessage = validationErrors.isEmpty()
            ? "One or more required parameters are missing or invalid."
            : "One or more required parameters are missing or invalid. See validationErrors for details.";

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .error("VALIDATION_ERROR")
            .message("Validation failure")
            .details(detailsMessage)
            .path(request.getRequestURI())
            .validationErrors(validationErrors.isEmpty() ? null : validationErrors)
            .build();

        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ApiResponse(
        responseCode = "422",
        description = "Invalid request (e.g. missing or empty required data)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(
        InvalidRequestException ex,
        HttpServletRequest request) {

        log.warn("Invalid request: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .error("INVALID_REQUEST")
            .message("Invalid request")
            .details(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.unprocessableEntity().body(errorResponse);
    }

    @ExceptionHandler(ProductDomainException.class)
    @ApiResponse(
        responseCode = "409",
        description = "Business rule violation",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleProductDomainException(
        ProductDomainException ex,
        HttpServletRequest request) {

        log.warn("Domain exception: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.CONFLICT.value())
            .error("BUSINESS_RULE_VIOLATION")
            .message("Business rule violation")
            .details(ex.getMessage())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ApiResponse(
        responseCode = "400",
        description = "Invalid argument",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex,
        HttpServletRequest request) {

        log.warn("Invalid argument: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = buildValidationErrorsForInvalidArgument(ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("INVALID_ARGUMENT")
            .message("Invalid argument provided")
            .details(ex.getMessage())
            .path(request.getRequestURI())
            .validationErrors(validationErrors)
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ApiResponse(
        responseCode = "404",
        description = "Product(s) not found",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
        ProductNotFoundException ex,
        HttpServletRequest request) {

        log.warn("Product(s) not found: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getMissingIds().stream()
            .map(id -> ErrorResponse.ValidationError.builder()
                .field("ids")
                .rejectedValue(id)
                .message("Product not found: " + id)
                .build())
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("NOT_FOUND")
            .message("One or more products were not found")
            .details("The following product ID(s) do not exist: " + ex.getMissingIds())
            .path(request.getRequestURI())
            .validationErrors(validationErrors)
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ApiResponse(
        responseCode = "400",
        description = "Missing required parameter",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex,
        HttpServletRequest request) {

        log.warn("Missing request parameter: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("BAD_REQUEST")
            .message("Required parameter '" + ex.getParameterName() + "' is missing")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleRuntimeException(
        RuntimeException ex,
        HttpServletRequest request) {

        log.error("Unexpected runtime error: ", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred")
            .details("Please contact support if the problem persists")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request) {

        log.error("Unexpected error: ", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(Instant.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("INTERNAL_SERVER_ERROR")
            .message("An unexpected error occurred")
            .details("Please contact support if the problem persists")
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ErrorResponse.ValidationError mapFieldError(FieldError fieldError) {
        return ErrorResponse.ValidationError.builder()
            .field(fieldError.getField())
            .rejectedValue(fieldError.getRejectedValue())
            .message(fieldError.getDefaultMessage())
            .build();
    }

    private ErrorResponse.ValidationError mapConstraintViolation(ConstraintViolation<?> violation) {
        String fieldName = violation.getPropertyPath().toString();
        return ErrorResponse.ValidationError.builder()
            .field(fieldName)
            .rejectedValue(violation.getInvalidValue())
            .message(violation.getMessage())
            .build();
    }

    private List<ErrorResponse.ValidationError> buildValidationErrorsForInvalidArgument(IllegalArgumentException ex) {
        List<ErrorResponse.ValidationError> errors = new ArrayList<>();
        String msg = ex.getMessage();
        if (msg != null && msg.startsWith("Invalid ID: ")) {
            String rejectedValue = msg.substring("Invalid ID: ".length()).trim();
            errors.add(ErrorResponse.ValidationError.builder()
                .field("ids")
                .rejectedValue(rejectedValue)
                .message(msg)
                .build());
        }
        return errors;
    }
} 