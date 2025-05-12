package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void badRequest_ShouldReturnBadRequestStatus() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        ResponseEntity<ErrorResponse> response = errorHandler.badRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getStatus());
        assertEquals("Incorrectly made request.", response.getBody().getMessage());
    }

    @Test
    void badRequest_WithMissingRequestHeaderException_ShouldReturnBadRequestStatus() {
        MissingRequestHeaderException exception = mock(MissingRequestHeaderException.class);

        ResponseEntity<ErrorResponse> response = errorHandler.badRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getStatus());
    }

    @Test
    void badRequest_WithConstraintViolationException_ShouldReturnBadRequestStatus() {
        ConstraintViolationException exception = mock(ConstraintViolationException.class);

        ResponseEntity<ErrorResponse> response = errorHandler.badRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getStatus());
    }

    @Test
    void badRequest_WithMissingServletRequestParameterException_ShouldReturnBadRequestStatus() {
        MissingServletRequestParameterException exception = mock(MissingServletRequestParameterException.class);

        ResponseEntity<ErrorResponse> response = errorHandler.badRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getStatus());
    }

    @Test
    void badRequest_WithWrongTimeException_ShouldReturnBadRequestStatus() {
        WrongTimeException exception = new WrongTimeException("Start time cannot be after end time");

        ResponseEntity<ErrorResponse> response = errorHandler.badRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getStatus());
        assertEquals("Start time cannot be after end time", response.getBody().getReason());
    }

    @Test
    void internalServerError_ShouldReturnInternalServerErrorStatus() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = errorHandler.internalServerError(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getMessage());
        assertEquals("Unexpected error", response.getBody().getReason());
    }

    @Test
    void errorResponse_ShouldContainTimestamp() {
        Exception exception = new RuntimeException("Test error");

        ResponseEntity<ErrorResponse> response = errorHandler.internalServerError(exception);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());

        String timestamp = response.getBody().getTimestamp();
        assertTrue(timestamp.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                "Timestamp should match format yyyy-MM-dd HH:mm:ss");
    }
}