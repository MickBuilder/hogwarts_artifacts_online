package fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.Choice;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

@RestClientTest(OpenAIChatClient.class)
class OpenAIChatClientTest {
  @Autowired
  private OpenAIChatClient openAIChatClient;
  @Autowired
  private MockRestServiceServer mockServer;
  @Autowired
  private ObjectMapper objectMapper;
  private ChatRequest chatRequest;
  private String url;

  @BeforeEach
  void setUp() {
    url = "https://api.openai.com/v1/chat/completions";
    chatRequest = new ChatRequest("gpt-3.5-turbo", List.of(
        new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
        new Message("user", "A json array.")
    ));
  }

  @Test
  void testGenerateSuccess() throws JsonProcessingException {
    // Given
    var chatResponse = new ChatResponse(List.of(new Choice(0, new Message("assistant", "The summary includes six artifacts owned by three different wizard"))));
    mockServer.expect(requestTo(url))
        .andExpect(method(HttpMethod.POST))
        .andExpect(header("Authorization", startsWith("Bearer")))
        .andExpect(content().json(objectMapper.writeValueAsString(chatRequest)))
        .andRespond(withSuccess(objectMapper.writeValueAsString(chatResponse), MediaType.APPLICATION_JSON));

    // When
    var generatedResponse = openAIChatClient.generate(chatRequest);

    // Then
    mockServer.verify(); // verify that all expected request set up via expect and andRespond are performed
    assertThat(generatedResponse.choices().getFirst().message().content()).isEqualTo("The summary includes six artifacts owned by three different wizard");
  }

  /**
   * This test simulates receiving a 401 Unauthorized response.
   */
  @Test
  void testGenerateUnauthorizedRequest(){
    // Given:
    mockServer.expect(requestTo(url))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withUnauthorizedRequest());

    // When:
    var thrown = catchThrowable(() -> openAIChatClient.generate(chatRequest));

    // Then:
    mockServer.verify();
    assertThat(thrown).isInstanceOf(HttpClientErrorException.Unauthorized.class);
  }

  /**
   * This test simulates a scenario where the service receives a 429 Quota Exceeded response.
   */
  @Test
  void testGenerateQuotaExceeded() {
    // Given
    mockServer.expect(requestTo(url))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withTooManyRequests());

    // When
    var thrown = catchThrowable(() -> openAIChatClient.generate(chatRequest));

    // Then
    mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
    assertThat(thrown).isInstanceOf(HttpClientErrorException.TooManyRequests.class);
  }

  /**
   * This test simulates receiving a 500 Internal Server Error response.
   */
  @Test
  void testGenerateServerError() {
    // Given
    mockServer.expect(requestTo(url))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withServerError());

    // When
    var thrown = catchThrowable(() -> openAIChatClient.generate(chatRequest));

    // Then
    mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
    assertThat(thrown).isInstanceOf(HttpServerErrorException.InternalServerError.class);
  }

  /**
   * This test simulates receiving a 503 Service Unavailable response.
   */
  @Test
  void testGenerateServerOverloaded() {
    // Given
    mockServer.expect(requestTo(url))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withServiceUnavailable());

    // When
    var thrown = catchThrowable(() -> openAIChatClient.generate(chatRequest));

    // Then
    mockServer.verify(); // Verify that all expected requests set up via expect(RequestMatcher) were indeed performed.
    assertThat(thrown).isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
  }
}