package com.example.EstoqueFacil.exception;

import com.example.EstoqueFacil.dto.error.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TraceId: {} - Recurso não encontrado: {}", traceId, ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage(), traceId);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TraceId: {} - Regra de negócio violada: {}", traceId, ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Erro de negócio", ex.getMessage(), traceId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Erro de validação: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TraceId: {} - Violação de integridade de dados: {}", traceId, ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, "Violação de integridade", "Dado duplicado ou referência inválida", traceId);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TraceId: {} - Requisição inválida: {}", traceId, ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage(), traceId);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TraceId: {} - Método HTTP não suportado: {}", traceId, ex.getMessage());
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado", ex.getMessage(), traceId);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        String traceId = UUID.randomUUID().toString();
        log.warn("TraceId: {} - Acesso negado: {}", traceId, ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para acessar este recurso", traceId);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("TraceId: {} - Erro interno no servidor - Tipo: {}, Mensagem: {}", traceId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor",
                "Ocorreu um erro inesperado. Contate o administrador.",
                traceId
        );
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(HttpStatus status, String error, String message, String traceId) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .traceId(traceId)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}