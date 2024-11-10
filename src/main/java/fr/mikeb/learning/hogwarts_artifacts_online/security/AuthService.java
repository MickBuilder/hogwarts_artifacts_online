package fr.mikeb.learning.hogwarts_artifacts_online.security;

import fr.mikeb.learning.hogwarts_artifacts_online.client.rediscache.RedisCacheClient;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.UserPrincipal;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.converter.UserToUserDtoConverter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
  private final JwtProvider jwtProvider;
  private final UserToUserDtoConverter userToUserDtoConverter;
  private final RedisCacheClient redisCacheClient;

  public AuthService(JwtProvider jwtProvider, UserToUserDtoConverter userToUserDtoConverter, RedisCacheClient redisCacheClient) {
    this.jwtProvider = jwtProvider;
    this.userToUserDtoConverter = userToUserDtoConverter;
    this.redisCacheClient = redisCacheClient;
  }

  public Map<String, Object> createLoginInfo(Authentication authentication) {
    // user info
    var principal = (UserPrincipal)authentication.getPrincipal();
    var user = principal.user();
    var userDto = userToUserDtoConverter.convert(user);

    // then jwt
    var token = jwtProvider.createToken(authentication);

    // Save the token in Redis. Key is "whitelist:{userId}", value is the token. Expire time in 2 HOURS
    redisCacheClient.set("whitelist:" + user.getId(), token, 2, TimeUnit.HOURS);

    var loginInfo = new HashMap<String, Object>();
    loginInfo.put("userInfo", userDto);
    loginInfo.put("token", token);

    return loginInfo;
  }
}
