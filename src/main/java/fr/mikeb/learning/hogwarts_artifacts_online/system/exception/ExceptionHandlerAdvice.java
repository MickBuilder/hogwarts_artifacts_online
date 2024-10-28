package fr.mikeb.learning.hogwarts_artifacts_online.system.exception;

import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Result<Void> handleArtifactNotFoundException(NotFoundException ex) {
    return new Result<>(false, StatusCode.NOT_FOUND, ex.getMessage());
  }

  /**
   * This handles invalid inputs.
   *
   * @param ex The exception
   * @return Returns a json result
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
    var errors = ex.getBindingResult().getAllErrors();
    var map = new HashMap<String, String>(errors.size());
    errors.forEach((error) -> {
      String key = ((FieldError) error).getField();
      String val = error.getDefaultMessage();
      map.put(key, val);
    });
    return new Result<>(false, StatusCode.INVALID_ARGUMENT, "Provided arguments are invalid, see data for details.", map);
  }

  @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result<String> handleAuthenticationException(Exception ex) {
    return new Result<>(false, StatusCode.UNAUTHORIZED, "username or password is incorrect.", ex.getMessage());
  }

  @ExceptionHandler(AccountStatusException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result<String> handleAccountStatusException(AccountStatusException ex) {
    return new Result<>(false, StatusCode.UNAUTHORIZED, "User account is abnormal.", ex.getMessage());
  }

  @ExceptionHandler(InvalidBearerTokenException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result<String> handleInvalidBearerTokenException(InvalidBearerTokenException ex) {
    return new Result<>(false, StatusCode.UNAUTHORIZED, "The access token provided is expired, revoked, malformed or invalid for other reason.", ex.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Result<String> handleAccessDeniedExceptionException(AccessDeniedException ex) {
    return new Result<>(false, StatusCode.FORBIDDEN, "No permission.", ex.getMessage());
  }

  /**
   * Fallback handles any unhandled exceptions.
   *
   * @param ex
   * @return result string
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  Result<String> handleOtherException(Exception ex) {
    return new Result<>(false, StatusCode.INTERNAL_SERVER_ERROR, "A server internal error occurs.", ex.getMessage());
  }
}
