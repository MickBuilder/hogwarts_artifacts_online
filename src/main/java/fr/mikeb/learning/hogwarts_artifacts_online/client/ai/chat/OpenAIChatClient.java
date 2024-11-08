package fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat;

import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAIChatClient implements ChatClient{
  private final RestClient restClient;

  public OpenAIChatClient(
      RestClient.Builder restClientBuilder,
      @Value("${ai.openai.endpoint}") String endpoint,
      @Value("${ai.openai.api-key}") String apikey
  ) {
    this.restClient = restClientBuilder
        .baseUrl(endpoint)
        .defaultHeader("Authorization", "Bearer " + apikey)
        .build();
  }

  @Override
  public ChatResponse generate(ChatRequest chatRequest) {
    return restClient
        .post()
        .contentType(MediaType.APPLICATION_JSON)
        .body(chatRequest)
        .retrieve()
        .body(ChatResponse.class);
  }
}
