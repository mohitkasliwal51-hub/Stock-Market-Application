package com.sankalp.userservice.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sankalp.userservice.dto.ApiResult;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResult<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		ApiResult<Object> response = ApiResult.error("Validation failed", "VALIDATION_ERROR");
		response.setData(errors);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResult<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getConstraintViolations().forEach(violation -> {
			String fieldName = violation.getPropertyPath().toString();
			String message = violation.getMessage();
			errors.put(fieldName, message);
		});
		ApiResult<Object> response = ApiResult.error("Validation failed", "CONSTRAINT_VIOLATION");
		response.setData(errors);
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResult<Object>> handleGlobalException(Exception ex) {
		logger.error("Unhandled exception", ex);
		ApiResult<Object> response = ApiResult.error("An unexpected error occurred", "INTERNAL_SERVER_ERROR");
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
