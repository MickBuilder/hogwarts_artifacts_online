package fr.mikeb.learning.hogwarts_artifacts_online.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfiguration {
  @Value("${api.endpoint.base-url}")
  private String baseUrl;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(authorizeRequest -> authorizeRequest
            .requestMatchers(HttpMethod.GET, baseUrl + "/artifacts/**").permitAll()
            .requestMatchers(HttpMethod.GET, baseUrl + "/users/**").hasAuthority("ROLE_admin")
            .requestMatchers(HttpMethod.POST, baseUrl + "/users").hasAuthority("ROLE_admin")
            .requestMatchers(HttpMethod.PUT, baseUrl + "/users/**").hasAuthority("ROLE_admin")
            .requestMatchers(HttpMethod.DELETE, baseUrl + "/users/**").hasAuthority("ROLE_admin")
            .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
            .anyRequest().authenticated() // Disallow everything else
        )
        .headers(headers -> headers.frameOptions(Customizer.withDefaults()).disable())
        .csrf(csrf -> csrf.disable())
        .httpBasic(Customizer.withDefaults())
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}
