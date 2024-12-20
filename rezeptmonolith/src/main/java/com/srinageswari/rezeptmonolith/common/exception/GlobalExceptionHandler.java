package com.srinageswari.rezeptmonolith.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.srinageswari.rezeptmonolith.common.Constants;
import com.srinageswari.rezeptmonolith.common.exception.helper.ElementAlreadyExistsException;
import com.srinageswari.rezeptmonolith.common.exception.helper.ErrorResponse;
import com.srinageswari.rezeptmonolith.common.exception.helper.NoSuchElementFoundException;

import java.util.Objects;

/**
 * @author smanickavasagam
 *     <p>Global exception handler class for handling all the exceptions
 */
@Slf4j(topic = "GlobalExceptionHandler")
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  /** This parameter is used to print StackTrace while debugging. Its value is false by default. */
  @Value("${exception.trace:false}")
  private boolean printStackTrace;

  /**
   * Handles MethodArgumentNotValidException
   *
   * @param ex
   * @param headers
   * @param status
   * @param request
   * @return ResponseEntity<Object> with detailed information related to the error
   */
  @Override
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    log.error(Constants.METHOD_ARGUMENT_NOT_VALID, ex);
    ErrorResponse errorResponse =
        new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), Constants.VALIDATION_ERROR);
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return ResponseEntity.unprocessableEntity().body(errorResponse);
  }

  /**
   * Handles NoSuchElementFoundException
   *
   * @param ex
   * @param request
   * @return ResponseEntity<Object> with detailed information related to the error
   */
  @ExceptionHandler(NoSuchElementFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<Object> handleNoSuchElementFoundException(
      NoSuchElementFoundException ex, WebRequest request) {
    log.error(Constants.NOT_FOUND, ex);
    return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
  }

  /**
   * Handles ElementAlreadyExistsException
   *
   * @param ex
   * @param request
   * @return ResponseEntity<Object> with detailed information related to the error
   */
  @ExceptionHandler(ElementAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<Object> handleElementAlreadyExistsException(
      ElementAlreadyExistsException ex, WebRequest request) {
    log.error(Constants.ALREADY_EXISTS, ex);
    return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
  }

  /**
   * Handles all the uncaught exceptions that cannot be caught by the previous methods
   *
   * @param ex
   * @param request
   * @return ResponseEntity<Object> with detailed information related to the error
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
    log.error(ex.getMessage(), ex);
    return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  /**
   * Build error message by the given exception, status and request
   *
   * @param ex
   * @param status
   * @param request
   * @return ResponseEntity<Object> with detailed information related to the error
   */
  private ResponseEntity<Object> buildErrorResponse(
      Exception ex, HttpStatus status, WebRequest request) {
    return buildErrorResponse(ex, ex.getMessage(), status, request);
  }

  /**
   * Build error message by the given exception, message, status and request
   *
   * @param ex
   * @param message
   * @param status
   * @param request
   * @return ResponseEntity<Object> with detailed information related to the error
   */
  private ResponseEntity<Object> buildErrorResponse(
      Exception ex, String message, HttpStatus status, WebRequest request) {
    ErrorResponse errorResponse = new ErrorResponse(status.value(), message);
    if (printStackTrace && isTraceOn(request)) {
      errorResponse.setStackTrace(ExceptionUtils.getStackTrace(ex));
    }
    return ResponseEntity.status(status).body(errorResponse);
  }

  /**
   * Checks the tracing parameter sent by request
   *
   * @param request
   * @return the tracing status based on the request
   */
  private boolean isTraceOn(WebRequest request) {
    String[] value = request.getParameterValues(Constants.TRACE);
    return Objects.nonNull(value) && value.length > 0 && value[0].contentEquals("true");
  }

  /**
   * Handles internal exceptions
   *
   * @param ex
   * @param body
   * @param headers
   * @param status
   * @param request
   * @return ResponseEntity<Object> with detailed information related to the error
   */
  @Override
  public ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
  }
}
