package br.senac.fianceiro.exception;

import br.senac.fianceiro.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/test");
    }

    @Test
    @DisplayName("Deve tratar BusinessException com status 422 e dados formatados")
    void deveTratarBusinessException() {
        BusinessException ex = new BusinessException("Saldo insuficiente na conta");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSucesso());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("Violação de Regra de Negócio", response.getBody().getErro());
        assertEquals("Saldo insuficiente na conta", response.getBody().getMensagem());
        assertEquals("/api/test", response.getBody().getCaminho());
    }

    @Test
    @DisplayName("Deve tratar ResourceNotFoundException com status 404")
    void deveTratarResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Conta não encontrada");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSucesso());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Recurso Não Encontrado", response.getBody().getErro());
        assertEquals("Conta não encontrada", response.getBody().getMensagem());
    }

    @Test
    @DisplayName("Deve tratar exceção genérica com status 500 sem vazar detalhes")
    void deveTratarExcecaoGenerica() {
        Exception ex = new RuntimeException("Erro inesperado no banco");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSucesso());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Erro Interno do Servidor", response.getBody().getErro());
        assertEquals("Ocorreu um erro interno inesperado. Por favor, tente novamente mais tarde.", response.getBody().getMensagem());
    }
}
