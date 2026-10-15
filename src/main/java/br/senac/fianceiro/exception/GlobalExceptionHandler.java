package br.senac.fianceiro.exception;

import br.senac.fianceiro.common.dto.ErrorResponse;
import br.senac.fianceiro.common.dto.ValidationErrorItem;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Business rule violation: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .sucesso(false)
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .erro("Violação de Regra de Negócio")
                .mensagem(ex.getMessage())
                .caminho(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .sucesso(false)
                .status(HttpStatus.NOT_FOUND.value())
                .erro("Recurso Não Encontrado")
                .mensagem(ex.getMessage())
                .caminho(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationErrorItem> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ValidationErrorItem(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
                .sucesso(false)
                .status(HttpStatus.BAD_REQUEST.value())
                .erro("Erro de Validação")
                .mensagem("Um ou mais campos contêm valores inválidos.")
                .caminho(request.getRequestURI())
                .errosValidacao(validationErrors)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .sucesso(false)
                .status(ex.getStatusCode().value())
                .erro("Erro de Requisição")
                .mensagem(ex.getReason() != null ? ex.getReason() : ex.getMessage())
                .caminho(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled server exception at URI: {}", request.getRequestURI(), ex);
        ErrorResponse error = ErrorResponse.builder()
                .sucesso(false)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .erro("Erro Interno do Servidor")
                .mensagem("Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.")
                .caminho(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
