package fr.mikeb.learning.hogwarts_artifacts_online.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class SecurityConfiguration {
  private final RSAPublicKey publicKey;
  private final RSAPrivateKey privateKey;
  private final CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;
  private final CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint;
  private final CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler;
  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  public SecurityConfiguration(CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint, CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint, CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler) throws NoSuchAlgorithmException {
    this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
    this.customBearerTokenAuthenticationEntryPoint = customBearerTokenAuthenticationEntryPoint;
    this.customBearerTokenAccessDeniedHandler = customBearerTokenAccessDeniedHandler;

    // Generate a public/private key pair.
    var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048); // The generated key will have a size of 2048
    var keyPair = keyPairGenerator.generateKeyPair();
    this.publicKey = (RSAPublicKey) keyPair.getPublic();
    this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(authorizeRequest -> authorizeRequest
            .requestMatchers(HttpMethod.GET, baseUrl + "/artifacts/**").permitAll()
            .requestMatchers(HttpMethod.GET, baseUrl + "/users/**").hasAuthority("ROLE_admin")
            .requestMatchers(HttpMethod.POST, baseUrl + "/users").hasAuthority("ROLE_admin")
            .requestMatchers(HttpMethod.PUT, baseUrl + "/users/**").hasAuthority("ROLE_admin")
            .requestMatchers(HttpMethod.DELETE, baseUrl + "/users/**").hasAuthority("ROLE_admin")
            .requestMatchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint().excluding("health", "info", "prometheus")).hasAuthority("ROLE_admin")
            .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
            .anyRequest().authenticated() // Disallow everything else
        )
        .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable())
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(customBasicAuthenticationEntryPoint))
        .oauth2ResourceServer(oAuth2ResourceServer -> oAuth2ResourceServer
            .jwt(Customizer.withDefaults())
            .authenticationEntryPoint(customBearerTokenAuthenticationEntryPoint)
            .accessDeniedHandler(customBearerTokenAccessDeniedHandler)
        )
        .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    var jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
    var jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwkSet);
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(publicKey).build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    var jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
    jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

    var jwtAuthConverter = new JwtAuthenticationConverter();
    jwtAuthConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

    return jwtAuthConverter;
  }
}
