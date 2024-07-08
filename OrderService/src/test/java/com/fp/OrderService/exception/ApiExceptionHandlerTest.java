package com.fp.OrderService.exception;

import com.fp.OrderService.dto.ErrorResponse;
import com.fp.OrderService.utils.AppConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    @InjectMocks
    private ApiExceptionHandler apiExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleValidationExceptions() {
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("objectName", "field1", "Error message 1"));
        fieldErrors.add(new FieldError("objectName", "field2", "Error message 2"));

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<Object> responseEntity = apiExceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody() instanceof Map);

        Map<String, String> errors = (Map<String, String>) responseEntity.getBody();
        assertEquals(2, errors.size());
        assertEquals("Error message 1", errors.get("field1"));
        assertEquals("Error message 2", errors.get("field2"));
    }

    @Test
    void testHandleNotFound() {
        NoSuchElementException exception = new NoSuchElementException("Element not found");

        ResponseEntity<ErrorResponse> responseEntity = apiExceptionHandler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(AppConstant.STATUS_NOT_FOUND, errorResponse.getError());
        assertEquals("Element not found", errorResponse.getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
    }

    @Test
    void testHandleAlreadyExists() {
        AlreadyExistsException exception = new AlreadyExistsException("Element already exists");

        ResponseEntity<ErrorResponse> responseEntity = apiExceptionHandler.handleAlreadyExists(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        ErrorResponse errorResponse = responseEntity.getBody();
        assertEquals(AppConstant.STATUS_ALREADY_EXISTS, errorResponse.getError());
        assertEquals("Element already exists", errorResponse.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
    }
}