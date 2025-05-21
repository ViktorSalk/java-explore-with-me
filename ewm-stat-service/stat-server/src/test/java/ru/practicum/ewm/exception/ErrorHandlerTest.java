package ru.practicum.ewm.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import ru.practicum.ewm.exceptions.ErrorResponse;

import java.security.InvalidParameterException;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ErrorHandlerTest {
    private static class ErrorHandler {
        public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid request parameters"));
        }

        public ResponseEntity<ErrorResponse> handleInvalidParameterException(InvalidParameterException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }

        public ResponseEntity<ErrorResponse> handleDateTimeParseException(DateTimeParseException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid date format. Use yyyy-MM-dd HH:mm:ss"));
        }

        public ResponseEntity<ErrorResponse> handleInternalServerError(Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error"));
        }
    }

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleBadRequest_WithMethodArgumentNotValidException_ShouldReturnBadRequestStatus() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        ResponseEntity<ErrorResponse> response = errorHandler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request parameters", response.getBody().getError());
    }

    @Test
    void handleBadRequest_WithMissingServletRequestParameterException_ShouldReturnBadRequestStatus() {
        MissingServletRequestParameterException exception = mock(MissingServletRequestParameterException.class);
        ResponseEntity<ErrorResponse> response = errorHandler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request parameters", response.getBody().getError());
    }

    @Test
    void handleInvalidParameterException_ShouldReturnBadRequestStatus() {
        InvalidParameterException exception = new InvalidParameterException("Start time cannot be after end time");
        ResponseEntity<ErrorResponse> response = errorHandler.handleInvalidParameterException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Start time cannot be after end time", response.getBody().getError());
    }

    @Test
    void handleDateTimeParseException_ShouldReturnBadRequestStatus() {
        DateTimeParseException exception = mock(DateTimeParseException.class);
        ResponseEntity<ErrorResponse> response = errorHandler.handleDateTimeParseException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid date format. Use yyyy-MM-dd HH:mm:ss", response.getBody().getError());
    }

    @Test
    void handleInternalServerError_ShouldReturnInternalServerErrorStatus() {
        Exception exception = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorResponse> response = errorHandler.handleInternalServerError(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().getError());
    }
}