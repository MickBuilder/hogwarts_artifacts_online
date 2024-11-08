package fr.mikeb.learning.hogwarts_artifacts_online.system.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Result<Void> handleNotFoundException(NotFoundException ex) {
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

  @ExceptionHandler(InsufficientAuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  Result<String> handleInsufficientAuthenticationException(InsufficientAuthenticationException ex) {
    return new Result<>(false, StatusCode.UNAUTHORIZED, "Login credentials are missing.", ex.getMessage());
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

  @ExceptionHandler(NoHandlerFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  Result<String> handleNoHandlerFoundException(NoHandlerFoundException ex) {
    return new Result<>(false, StatusCode.NOT_FOUND, "This API endpoint is not found.", ex.getMessage());
  }

  @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
  ResponseEntity<Result<String>> handleRestClientException(HttpStatusCodeException ex) throws JsonProcessingException {
    var exceptionMessage = ex.getMessage();
    // Replace <EOL> with actual newlines.
    exceptionMessage = exceptionMessage.replace("<EOL>", "\n");
    // Extract the JSON part from the string.
    String jsonPart = exceptionMessage.substring(exceptionMessage.indexOf("{"), exceptionMessage.lastIndexOf("}") + 1);

    // Create an ObjectMapper instance.
    var mapper = new ObjectMapper();

    // Parse the JSON string to a JsonNode.
    var rootNode = mapper.readTree(jsonPart);

    // Extract the message.
    String formattedExceptionMessage = rootNode.path("error").path("message").asText();

    return new ResponseEntity<>(
        new Result<>(false,
            ex.getStatusCode().value(),
            "A rest client error occurs, see data for details.",
            formattedExceptionMessage),
        ex.getStatusCode());
  }

  @ExceptionHandler(CustomBlobStorageException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  Result<String> handleCustomBlobStorageException(CustomBlobStorageException ex) {
    return new Result<>(false, StatusCode.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getCause().getMessage());
  }

  /**
   * Fallback handles any unhandled exceptions.
   *
   * @param ex an exception
   * @return result string
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  Result<String> handleOtherException(Exception ex) {
    return new Result<>(false, StatusCode.INTERNAL_SERVER_ERROR, "A server internal error occurs.", ex.getMessage());
  }
}
