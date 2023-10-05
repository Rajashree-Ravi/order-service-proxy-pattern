package com.ecommerce.orderservice.handler;

import java.time.LocalDateTime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ecommerce.sharedlibrary.exception.EcommerceException;
import com.ecommerce.sharedlibrary.model.ApiErrorResponse;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EcommerceErrorHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(EcommerceException.class)
	public ResponseEntity<?> handleEcommerceException(final EcommerceException exception,
			final HttpServletRequest request) {

		var response = new ApiErrorResponse(exception.getErrorCode(), exception.getMessage(),
				exception.getHttpStatus().value(), exception.getHttpStatus().name(), request.getRequestURI(),
				request.getMethod(), LocalDateTime.now());

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleUnknownException(final Exception exception, final HttpServletRequest request) {

		var response = new ApiErrorResponse("internal-server-error", exception.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.name(),
				request.getRequestURI(), request.getMethod(), LocalDateTime.now());

		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
