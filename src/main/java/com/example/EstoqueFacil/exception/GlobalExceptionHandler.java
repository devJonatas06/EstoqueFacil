package com.example.EstoqueFacil.exception.handler;

import com.example.EstoqueFacil.dto.error.ErrorResponseDTO;
import com.example.EstoqueFacil.exception.BusinessException;
import com.example.EstoqueFacil.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ✅ 404 - Recurso não encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    // ✅ 400 - Regra de negócio violada
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Erro de negócio", ex.getMessage());
    }

    // ✅ 400 - Validação de campos (@Valid)
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

    // ✅ 400 - Parâmetro de query/path inválido
    @ExceptionHandler({
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(Exception ex) {
        log.warn("Requisição inválida: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage());
    }

    // ✅ 403 - Acesso negado
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Acesso negado", "Você não tem permissão para acessar este recurso");
    }

    // ✅ 500 - Erro interno (não expor detalhes em produção)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("Erro interno no servidor", ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor",
                "Ocorreu um erro inesperado. Contate o administrador."
        );
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(HttpStatus status, String error, String message) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}