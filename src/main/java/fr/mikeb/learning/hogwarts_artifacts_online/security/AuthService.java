package fr.mikeb.learning.hogwarts_artifacts_online.security;

import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.UserPrincipal;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.converter.UserToUserDtoConverter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
  private final JwtProvider jwtProvider;
  private final UserToUserDtoConverter userToUserDtoConverter;

  public AuthService(JwtProvider jwtProvider, UserToUserDtoConverter userToUserDtoConverter) {
    this.jwtProvider = jwtProvider;
    this.userToUserDtoConverter = userToUserDtoConverter;
  }

  public Map<String, Object> createLoginInfo(Authentication authentication) {
    // user info
    var principal = (UserPrincipal)authentication.getPrincipal();
    var user = principal.user();
    var userDto = userToUserDtoConverter.convert(user);

    // then jwt
    var token = jwtProvider.createToken(authentication);

    var loginInfo = new HashMap<String, Object>();
    loginInfo.put("userInfo", userDto);
    loginInfo.put("token", token);

    return loginInfo;
  }
}
