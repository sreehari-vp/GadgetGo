package com.gadgetgo.catalog.api;

import com.gadgetgo.catalog.service.CatalogResourceNotFoundException;
import com.gadgetgo.catalog.service.InvalidCatalogFilterException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CatalogController.class)
public class CatalogExceptionHandler {

    @ExceptionHandler(CatalogResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(CatalogResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidCatalogFilterException.class)
    public ResponseEntity<ProblemDetail> handleInvalidFilter(InvalidCatalogFilterException exception) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "One or more catalogue query parameters are invalid.");
    }

    private static ResponseEntity<ProblemDetail> problem(HttpStatus status, String detail) {
        ProblemDetail response = ProblemDetail.forStatusAndDetail(status, detail);
        return ResponseEntity.status(status).body(response);
    }
}
