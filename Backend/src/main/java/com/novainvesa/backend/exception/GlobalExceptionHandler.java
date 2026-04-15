package com.novainvesa.backend.exception;

import com.novainvesa.backend.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;
import com.novainvesa.backend.exception.ImportException;
import com.novainvesa.backend.exception.OrderException;
import com.novainvesa.backend.exception.PaymentException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Recursos no encontrados (404) */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado [{}]: {}", ex.getCode(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Errores de autenticación/autorización propios */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthException(AuthException ex) {
        log.warn("Error de autenticación [{}]: {}", ex.getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Errores de pedidos (400 o 404 según código) */
    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiResponse<?>> handleOrderException(OrderException ex) {
        log.warn("Error en pedido [{}]: {}", ex.getCode(), ex.getMessage());
        HttpStatus status = "ORDER_006".equals(ex.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Errores de pagos (400 o 401 según código) */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<?>> handlePaymentException(PaymentException ex) {
        // No loggear detalles de PAYMENT_002 para no revelar información del sistema de firma
        if ("PAYMENT_002".equals(ex.getCode())) {
            log.warn("Webhook recibido con firma inválida");
        } else {
            log.warn("Error de pago [{}]: {}", ex.getCode(), ex.getMessage());
        }
        HttpStatus status = "PAYMENT_002".equals(ex.getCode()) ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Errores del importador Dropi */
    @ExceptionHandler(ImportException.class)
    public ResponseEntity<ApiResponse<?>> handleImportException(ImportException ex) {
        log.warn("Error de importación [{}]: {}", ex.getCode(), ex.getMessage());
        // IMPORT_003 (no encontrado en Dropi) e IMPORT_005 (job no encontrado) → 404
        HttpStatus status = switch (ex.getCode()) {
            case "IMPORT_003", "IMPORT_005" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /** Errores de validación de @Valid */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_001", message));
    }

    /** Fallback para errores no controlados */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        log.error("Error interno no controlado", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("SERVER_001", "Error interno del servidor"));
    }
}
