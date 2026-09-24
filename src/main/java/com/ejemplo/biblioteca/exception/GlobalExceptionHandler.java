package com.ejemplo.biblioteca.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BookNotFoundException.class)
  public ResponseEntity<ApiError> handleNotFound(
      BookNotFoundException ex, HttpServletRequest request) {

    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
  }

  @ExceptionHandler({DuplicateIsbnException.class, BusinessRuleException.class})
  public ResponseEntity<ApiError> handleBusinessRule(
      RuntimeException ex, HttpServletRequest request) {

    return build(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    return build(HttpStatus.BAD_REQUEST, "La petición contiene datos no válidos", request, errors);
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      Map<String, String> validationErrors) {

    ApiError error =
        new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            validationErrors);

    return ResponseEntity.status(status).body(error);
  }
}
