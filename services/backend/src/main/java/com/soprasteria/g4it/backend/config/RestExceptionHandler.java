/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;


import com.soprasteria.g4it.backend.exception.*;
import com.soprasteria.g4it.backend.server.gen.api.dto.RestError;
import com.soprasteria.g4it.backend.server.gen.api.dto.RestValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {G4itRestException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RestError> handleG4itRestException(final G4itRestException ex, final WebRequest request) {

        if (String.valueOf(HttpStatus.NO_CONTENT.value()).equals(ex.getCode())) {
            return ResponseEntity.noContent().build();
        }
        if (String.valueOf(HttpStatus.CONFLICT.value()).equals(ex.getCode())) {
            return new ResponseEntity<>(
                    RestError.builder()
                            .code("409")
                            .message(ex.getMessage())
                            .timestamp(LocalDateTime.now())
                            .status(409)
                            .build(), HttpStatus.CONFLICT);
        }
        if (String.valueOf(HttpStatus.FORBIDDEN.value()).equals(ex.getCode())) {
            log.error("Forbidden: {}", ex.getMessage());
            return new ResponseEntity<>(
                    RestError.builder()
                            .code("403")
                            .message(ex.getMessage())
                            .timestamp(LocalDateTime.now())
                            .status(403)
                            .build(), HttpStatus.FORBIDDEN);
        }

        if (String.valueOf(HttpStatus.NOT_FOUND.value()).equals(ex.getCode())) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // to check url expiry for shared digital service
        if (ex.getCode().equals("410")) {
            return new ResponseEntity<>(
                    RestError.builder()
                            .code("410")
                            .message(ex.getMessage())
                            .timestamp(LocalDateTime.now())
                            .status(410)
                            .build(),
                    HttpStatus.GONE);
        }
        // Added BAD_REQUEST Exception handler
        if (String.valueOf(HttpStatus.BAD_REQUEST.value()).equals(ex.getCode())) {
            return new ResponseEntity<>(
                    RestError.builder()
                            .code(HttpStatus.BAD_REQUEST.toString())
                            .message(ex.getMessage())
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build(),
                    HttpStatus.BAD_REQUEST);
        }

        log.error("Exception de validation survenue lors de la requête {}", request.getContextPath(), ex);
        return new ResponseEntity<>(
                RestError.builder()
                        .code("500")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .status(500)
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {ExternalApiException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RestError> externalApiException(final ExternalApiException ex, WebRequest request) {
        log.error("External API error: {}", ex.getMessage());
        return new ResponseEntity<>(
                RestError.builder()
                        .code("500")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .status(500)
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ResponseEntity<Void> accessDeniedException(final AccessDeniedException ex, WebRequest request) {
        ServletWebRequest req = (ServletWebRequest) request;
        log.error("UserId={} is not allowed to access: {} {} with roles: {}",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                req.getHttpMethod().name(),
                req.getRequest().getRequestURI(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(value = AuthorizationException.class)
    public ResponseEntity<Void> unauthorizedException(final AuthorizationException ex, WebRequest request) {
        log.error("Unauthorized to access {}: {}", request.getContextPath(), ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).build();
    }

    @ExceptionHandler(value = {InvalidReferentialException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestValidationError> handleInvalidReferentialException(final InvalidReferentialException ex, final WebRequest request) {
        return ResponseEntity.badRequest().body(RestValidationError.builder().field(ex.getReferentialInErrorCode()).error("Invalid referential").build());
    }

    @ExceptionHandler(value = {BadRequestException.class})
    public ResponseEntity<RestValidationError> handleBadRequestException(final BadRequestException ex) {
        return ResponseEntity.badRequest().body(RestValidationError.builder().field(ex.getField()).error(ex.getError()).build());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<RestValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .<RestValidationError>map(fieldError ->
                        RestValidationError.builder()
                                .field(fieldError.getField())
                                .error(fieldError.getDefaultMessage())
                                .build())
                .toList();
        return ResponseEntity.status(status.value()).body(errors);
    }


    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<RestValidationError> handleConstraintViolationException(
            final ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(RestValidationError.builder()
                .field(ex.getConstraintViolations().stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.joining(", ")))
                .error(ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(". "))).build());
    }

    @ExceptionHandler(value = {RuntimeException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RestError> runtimeException(Exception ex, WebRequest request) {
        log.error("RuntimeException lors d'un traitement sur l'URI {}", request.getContextPath(), ex);
        return new ResponseEntity<>(
                RestError.builder()
                        .code("500")
                        .message("Erreur interne de traitement lors du traitement de la requête")
                        .timestamp(LocalDateTime.now())
                        .status(500)
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RestError> exception(Exception ex, WebRequest request) {
        log.error("Exception lors d'un traitement sur l'URI {}", request.getContextPath(), ex);
        return new ResponseEntity<>(
                RestError.builder()
                        .code("500")
                        .message("Erreur interne de traitement lors du traitement de la requête")
                        .timestamp(LocalDateTime.now())
                        .status(500)
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
