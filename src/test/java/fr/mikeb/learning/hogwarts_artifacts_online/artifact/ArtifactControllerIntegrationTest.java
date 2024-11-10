package fr.mikeb.learning.hogwarts_artifacts_online.artifact;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Integration tests for Artifact API endpoints.")
@Tag("integration")
@ActiveProfiles(value = "dev")
public class ArtifactControllerIntegrationTest {
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @Value("${api.endpoint.base-url}")
  String baseUrl;
  String token;
  @Container
  @ServiceConnection
  static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

  @BeforeEach
  void setUp() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("john", "123456"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    token = "Bearer " + json.getJSONObject("data").getString("token"); // Don't forget to add "Bearer " as prefix.
  }

  @Test
  @DisplayName("Check findAllArtifacts (GET)")
  @Order(1)
  void testFindAllArtifactSuccess() throws Exception {
    mockMvc.perform(get(baseUrl + "/artifacts").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data.content", Matchers.hasSize(6)));
  }

  @Test
  @DisplayName("Check findArtifactById (GET)")
//  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  @Order(2)
  void testFindArtifactByIdSuccess() throws Exception {
    mockMvc.perform(get(baseUrl + "/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value("1250808601744904191"))
        .andExpect(jsonPath("$.data.name").value("Deluminator"));
  }

  @Test
  @DisplayName("Check findArtifactById with non-existent id (GET)")
  void testFindArtifactByIdNotFound() throws Exception {
    mockMvc.perform(get(baseUrl + "/artifacts/1250808601744904199").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904199 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check addArtifact with valid input (POST)")
  void testAddArtifactSuccess() throws Exception {
    var a = new Artifact();
    a.setName("Remembrall");
    a.setDescription("A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered.");
    a.setImgUrl("ImageUrl");

    var json = objectMapper.writeValueAsString(a);

    mockMvc.perform(post(baseUrl + "/artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.name").value("Remembrall"))
        .andExpect(jsonPath("$.data.description").value("A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered."))
        .andExpect(jsonPath("$.data.imgUrl").value("ImageUrl"));

    mockMvc.perform(get(baseUrl + "/artifacts").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data.content", Matchers.hasSize(7)));
  }

  @Test
  @DisplayName("Check addArtifact with invalid input (POST)")
  void testAddArtifactErrorWithInvalidInput() throws Exception {
    var a = new Artifact();
    a.setName(""); // Name is not provided.
    a.setDescription(""); // Description is not provided.
    a.setImgUrl(""); // ImageUrl is not provided.

    var json = objectMapper.writeValueAsString(a);

    mockMvc.perform(post(baseUrl + "/artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
        .andExpect(jsonPath("$.data.name").value("name is required."))
        .andExpect(jsonPath("$.data.description").value("description is required."))
        .andExpect(jsonPath("$.data.imgUrl").value("imgUrl is required."));

    mockMvc.perform(get(baseUrl + "/artifacts").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data.content", Matchers.hasSize(6)));
  }

  @Test
  @DisplayName("Check updateArtifact with valid input (PUT)")
  void testUpdateArtifactSuccess() throws Exception {
    var a = new Artifact();
    a.setId("1250808601744904192");
    a.setName("Updated artifact name");
    a.setDescription("Updated description");
    a.setImgUrl("Updated imageUrl");

    var json = objectMapper.writeValueAsString(a);

    mockMvc.perform(put(baseUrl + "/artifacts/1250808601744904192").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value("1250808601744904192"))
        .andExpect(jsonPath("$.data.name").value("Updated artifact name"))
        .andExpect(jsonPath("$.data.description").value("Updated description"))
        .andExpect(jsonPath("$.data.imgUrl").value("Updated imageUrl"));
  }

  @Test
  @DisplayName("Check updateArtifact with non-existent id (PUT)")
  void testUpdateArtifactErrorWithNonExistentId() throws Exception {
    var a = new Artifact();
    a.setId("1250808601744904199"); // This id does not exist in the database.
    a.setName("Updated artifact name");
    a.setDescription("Updated description");
    a.setImgUrl("Updated imageUrl");

    var json = objectMapper.writeValueAsString(a);

    mockMvc.perform(put(baseUrl + "/artifacts/1250808601744904199").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904199 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check updateArtifact with invalid input (PUT)")
  void testUpdateArtifactErrorWithInvalidInput() throws Exception {
    var a = new Artifact();
    a.setId("1250808601744904191"); // Valid id
    a.setName(""); // Updated name is empty.
    a.setDescription(""); // Updated description is empty.
    a.setImgUrl(""); // Updated imageUrl is empty.

    var json = objectMapper.writeValueAsString(a);

    mockMvc.perform(put(baseUrl + "/artifacts/1250808601744904191").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
        .andExpect(jsonPath("$.data.name").value("name is required."))
        .andExpect(jsonPath("$.data.description").value("description is required."))
        .andExpect(jsonPath("$.data.imgUrl").value("imgUrl is required."));

    mockMvc.perform(get(baseUrl + "/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value("1250808601744904191"))
        .andExpect(jsonPath("$.data.name").value("Deluminator"));
  }

  @Test
  @DisplayName("Check deleteArtifact with valid input (DELETE)")
  void testDeleteArtifactSuccess() throws Exception {
    mockMvc.perform(delete(baseUrl + "/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"));

    mockMvc.perform(get(baseUrl + "/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904191 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testFindArtifactsByDescription() throws Exception {
    // Given
    var searchCriteria = new HashMap<String, String>();
    searchCriteria.put("description", "Hogwarts");
    String json = objectMapper.writeValueAsString(searchCriteria);

    var requestParams = new LinkedMultiValueMap<String, String>();
    requestParams.add("page", "0");
    requestParams.add("size", "2");
    requestParams.add("sort", "name,asc");

    // When and then
    mockMvc.perform(post(baseUrl + "/artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Search Success"))
        .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
  }

  @Test
  void testFindArtifactsByNameAndDescription() throws Exception {
    // Given
    var searchCriteria = new HashMap<String, String>();
    searchCriteria.put("name", "Sword");
    searchCriteria.put("description", "Hogwarts");
    String json = objectMapper.writeValueAsString(searchCriteria);

    var requestParams = new LinkedMultiValueMap<String, String>();
    requestParams.add("page", "0");
    requestParams.add("size", "2");
    requestParams.add("sort", "name,asc");

    // When and then
    mockMvc.perform(post(baseUrl + "/artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Search Success"))
        .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
  }
}
