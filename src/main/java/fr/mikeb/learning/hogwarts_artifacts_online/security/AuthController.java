package fr.mikeb.learning.hogwarts_artifacts_online.security;

import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class AuthController {
  private final AuthService authService;

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public Result<Map<String, Object>> getLoginInfo(Authentication authentication) {
    LOGGER.debug("Authenticated user : {}", authentication.getName());
    var loginInfo = authService.createLoginInfo(authentication);
    return new Result<>(true, StatusCode.SUCCESS, "User Info and JSON Web Token", loginInfo);
  }
}
