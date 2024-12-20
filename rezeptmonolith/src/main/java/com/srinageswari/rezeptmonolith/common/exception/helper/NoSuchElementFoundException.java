package com.srinageswari.rezeptmonolith.common.exception.helper;

/**
 * @author smanickavasagam
 *     <p>Custom exception class used when no record is returned for the given filter parameters
 */
public class NoSuchElementFoundException extends RuntimeException {

  public NoSuchElementFoundException() {
    super();
  }

  public NoSuchElementFoundException(String message) {
    super(message);
  }

  public NoSuchElementFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
