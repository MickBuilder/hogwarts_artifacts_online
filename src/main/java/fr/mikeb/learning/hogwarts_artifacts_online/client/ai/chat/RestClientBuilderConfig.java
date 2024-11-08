package fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientBuilderConfig {
  @Bean
  public RestClient.Builder restClient() {
    return RestClient.builder()
        .requestFactory(new JdkClientHttpRequestFactory());
  }
}
