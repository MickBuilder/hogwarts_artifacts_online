package fr.mikeb.learning.hogwarts_artifacts_online.system.exception;

import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Result handleArtifactNotFoundException(NotFoundException ex) {
    return new Result(false, StatusCode.NOT_FOUND, ex.getMessage());
  }

  /**
   * This handles invalid inputs.
   *
   * @param ex The exception
   * @return Returns a json result
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  Result handleValidationException(MethodArgumentNotValidException ex) {
    var errors = ex.getBindingResult().getAllErrors();
    var map = new HashMap<>(errors.size());
    errors.forEach((error) -> {
      String key = ((FieldError) error).getField();
      String val = error.getDefaultMessage();
      map.put(key, val);
    });
    return new Result(false, StatusCode.INVALID_ARGUMENT, "Provided arguments are invalid, see data for details.", map);
  }
}
