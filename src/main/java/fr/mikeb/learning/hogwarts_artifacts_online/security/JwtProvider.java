package fr.mikeb.learning.hogwarts_artifacts_online.security;

import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Component
public class JwtProvider {
  private final JwtEncoder jwtEncoder;

  public JwtProvider(JwtEncoder jwtEncoder) {
    this.jwtEncoder = jwtEncoder;
  }

  public String createToken(Authentication authentication) {
    var now = Instant.now();
    long expiresIn = 2; // in 2 hours
    // prepares claims called authorities
    var authorities = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(" "));

    var claims = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(expiresIn, ChronoUnit.HOURS))
        .subject(authentication.getName())
        .claim("userId", ((UserPrincipal)(authentication.getPrincipal())).user().getId())
        .claim("authorities", authorities)
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
