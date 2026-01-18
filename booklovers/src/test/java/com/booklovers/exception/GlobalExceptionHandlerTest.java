package com.booklovers.exception;

import com.booklovers.dto.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", 1L);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("User with id 1 not found", response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void testHandleBadRequestException() {
        BadRequestException ex = new BadRequestException("Invalid input");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequestException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void testHandleConflictException() {
        ConflictException ex = new ConflictException("Resource already exists");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConflictException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertEquals("Resource already exists", response.getBody().getMessage());
    }

    @Test
    void testHandleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Access denied");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleForbiddenException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void testHandleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Not authenticated");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorizedException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Not authenticated", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid argument", response.getBody().getMessage());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("userDto", "username", "Username is required");
        List<org.springframework.validation.ObjectError> errors = Arrays.asList(fieldError);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals("Validation failed for one or more fields", response.getBody().getMessage());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals("Username is required", response.getBody().getValidationErrors().get("username"));
    }

    @Test
    void testHandleConstraintViolationException() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        Set<ConstraintViolation<?>> violations = new HashSet<>(Arrays.asList(violation));

        when(ex.getConstraintViolations()).thenReturn(violations);
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("username");
        when(violation.getMessage()).thenReturn("Username is required");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals("Constraint violation", response.getBody().getMessage());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals("Username is required", response.getBody().getValidationErrors().get("username"));
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Access denied: Access denied", response.getBody().getMessage());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void testHandleResourceNotFoundException_WithString() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "testuser");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("testuser"));
        assertTrue(response.getBody().getMessage().contains("User"));
    }

    @Test
    void testHandleMethodArgumentNotValidException_MultipleErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("userDto", "username", "Username is required");
        FieldError fieldError2 = new FieldError("userDto", "email", "Email is required");
        List<org.springframework.validation.ObjectError> errors = Arrays.asList(fieldError1, fieldError2);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(errors);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(ex, webRequest);

        assertNotNull(response);
        assertEquals(2, response.getBody().getValidationErrors().size());
        assertEquals("Username is required", response.getBody().getValidationErrors().get("username"));
        assertEquals("Email is required", response.getBody().getValidationErrors().get("email"));
    }

    @Test
    void testHandleConstraintViolationException_MultipleViolations() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);
        Set<ConstraintViolation<?>> violations = new HashSet<>(Arrays.asList(violation1, violation2));

        when(ex.getConstraintViolations()).thenReturn(violations);
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("Username is required");
        when(path1.toString()).thenReturn("username");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("Email is required");
        when(path2.toString()).thenReturn("email");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(ex, webRequest);

        assertNotNull(response);
        assertEquals(2, response.getBody().getValidationErrors().size());
        assertEquals("Username is required", response.getBody().getValidationErrors().get("username"));
        assertEquals("Email is required", response.getBody().getValidationErrors().get("email"));
    }

    @Test
    void testHandleGenericException_NullMessage() {
        Exception ex = new RuntimeException();

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, webRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
