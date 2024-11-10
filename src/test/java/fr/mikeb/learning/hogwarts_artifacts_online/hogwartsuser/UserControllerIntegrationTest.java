package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Integration tests for User API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
class UserControllerIntegrationTest {
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  String token;
  @Value("${api.endpoint.base-url}")
  String baseUrl;
  @Container
  @ServiceConnection
  static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));

  @BeforeEach
  void setUp() throws Exception {
    // User john has all permissions.
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("john", "123456"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    token = "Bearer " + json.getJSONObject("data").getString("token");
  }

  @Test
  @DisplayName("Check findAllUsers (GET)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindAllUsersSuccess() throws Exception {
    mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
  }

  @Test
  @DisplayName("Check findUserById (GET): User with ROLE_admin Accessing Any User's Info")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testFindUserByIdWithAdminAccessingAnyUsersInfo() throws Exception {
    mockMvc.perform(get(baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value(2))
        .andExpect(jsonPath("$.data.username").value("eric"));
  }

  @Test
  @DisplayName("Check findUserById (GET): User with ROLE_user Accessing Own Info")
  void testFindUserByIdWithUserAccessingOwnInfo() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    var ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    mockMvc.perform(get(baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value(2))
        .andExpect(jsonPath("$.data.username").value("eric"));
  }

  @Test
  @DisplayName("Check findUserById (GET): User with ROLE_user Accessing Another Users Info")
  void testFindUserByIdWithUserAccessingAnotherUsersInfo() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    var ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    mockMvc.perform(get(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));
  }

  @Test
  @DisplayName("Check findUserById with non-existent id (GET)")
  void testFindUserByIdNotFound() throws Exception {
    mockMvc.perform(get(baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check addUser with valid input (POST)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testAddUserSuccess() throws Exception {
    var user = new HogwartsUser();
    user.setUsername("lily");
    user.setPassword("123456");
    user.setEnabled(true);
    user.setRoles("admin user"); // The delimiter is space.

    String json = objectMapper.writeValueAsString(user);

    mockMvc.perform(post(baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.username").value("lily"))
        .andExpect(jsonPath("$.data.enabled").value(true))
        .andExpect(jsonPath("$.data.roles").value("admin user"));

    mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
  }

  @Test
  @DisplayName("Check addUser with invalid input (POST)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testAddUserErrorWithInvalidInput() throws Exception {
    var hogwartsUser = new HogwartsUser();
    hogwartsUser.setUsername(""); // Username is not provided.
    hogwartsUser.setPassword(""); // Password is not provided.
    hogwartsUser.setRoles(""); // Roles field is not provided.

    var json = objectMapper.writeValueAsString(hogwartsUser);

    mockMvc.perform(post(baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
        .andExpect(jsonPath("$.data.username").value("username is required."))
        .andExpect(jsonPath("$.data.password").value("password is required."))
        .andExpect(jsonPath("$.data.roles").value("roles are required."));

    mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
  }

  @Test
  @DisplayName("Check updateUser with valid input (PUT)")
  void testUpdateUserWithAdminUpdatingAnyUsersInfo() throws Exception {
    var hogwartsUser = new HogwartsUser();
    hogwartsUser.setUsername("tom123"); // Username is changed. It was tom.
    hogwartsUser.setEnabled(false);
    hogwartsUser.setRoles("user");

    String json = objectMapper.writeValueAsString(hogwartsUser);

    mockMvc.perform(put(baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value(3))
        .andExpect(jsonPath("$.data.username").value("tom123"))
        .andExpect(jsonPath("$.data.enabled").value(false))
        .andExpect(jsonPath("$.data.roles").value("user"));
  }

  @Test
  @DisplayName("Check updateUser with non-existent id (PUT)")
  void testUpdateUserErrorWithNonExistentId() throws Exception {
    var hogwartsUser = new HogwartsUser();
    hogwartsUser.setUsername("john123"); // Username is changed.
    hogwartsUser.setEnabled(true);
    hogwartsUser.setRoles("admin user");

    var json = objectMapper.writeValueAsString(hogwartsUser);

    mockMvc.perform(put(baseUrl + "/users/5").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check updateUser with invalid input (PUT)")
  void testUpdateUserErrorWithInvalidInput() throws Exception {
    HogwartsUser hogwartsUser = new HogwartsUser();
    hogwartsUser.setUsername(""); // Updated username is empty.
    hogwartsUser.setRoles(""); // Updated roles field is empty.

    String json = objectMapper.writeValueAsString(hogwartsUser);

    mockMvc.perform(put(baseUrl + "/users/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
        .andExpect(jsonPath("$.data.username").value("username is required."))
        .andExpect(jsonPath("$.data.roles").value("roles are required."));

    mockMvc.perform(get(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.username").value("john"));
  }

  @Test
  @DisplayName("Check updateUser with valid input (PUT): User with ROLE_user Updating Own Info")
  void testUpdateUserWithUserUpdatingOwnInfo() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    var ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    var hogwartsUser = new HogwartsUser();
    hogwartsUser.setUsername("eric123"); // Username is changed. It was eric.
    hogwartsUser.setEnabled(true);
    hogwartsUser.setRoles("user");

    var hogwartsUserJson = objectMapper.writeValueAsString(hogwartsUser);

    mockMvc.perform(put(baseUrl + "/users/2").contentType(MediaType.APPLICATION_JSON).content(hogwartsUserJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value(2))
        .andExpect(jsonPath("$.data.username").value("eric123"))
        .andExpect(jsonPath("$.data.enabled").value(true))
        .andExpect(jsonPath("$.data.roles").value("user"));
  }

  @Test
  @DisplayName("Check updateUser with valid input (PUT): User with ROLE_user Updating Another Users Info")
  void testUpdateUserWithUserUpdatingAnotherUsersInfo() throws Exception {
    ResultActions resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    MvcResult mvcResult = resultActions.andDo(print()).andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();
    JSONObject json = new JSONObject(contentAsString);
    String ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    HogwartsUser hogwartsUser = new HogwartsUser();
    hogwartsUser.setUsername("tom123"); // Username is changed. It was tom.
    hogwartsUser.setEnabled(false);
    hogwartsUser.setRoles("user");

    String hogwartsUserJson = objectMapper.writeValueAsString(hogwartsUser);

    mockMvc.perform(put(baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(hogwartsUserJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));
  }

  @Test
  @DisplayName("Check deleteUser with valid input (DELETE)")
  void testDeleteUserSuccess() throws Exception {
    mockMvc.perform(delete(baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"))
        .andExpect(jsonPath("$.data").isEmpty());
    mockMvc.perform(get(baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 2 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check deleteUser with non-existent id (DELETE)")
  void testDeleteUserErrorWithNonExistentId() throws Exception {
    mockMvc.perform(delete(baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("Check deleteUser with insufficient permission (DELETE)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testDeleteUserNoAccessAsRoleUser() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    var ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    mockMvc.perform(delete(baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
        .andExpect(jsonPath("$.message").value("No permission."))
        .andExpect(jsonPath("$.data").value("Access Denied"));

    mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(3)))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].username").value("john"));
  }

  @Test
  @DisplayName("Check changeUserPassword with valid input (PATCH)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPassword() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    var ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    // Given
    var passwordMap = new HashMap<String, String>();
    passwordMap.put("oldPassword", "654321");
    passwordMap.put("newPassword", "Abc12345");
    passwordMap.put("confirmNewPassword", "Abc12345");

    var passwordMapJson = objectMapper.writeValueAsString(passwordMap);

    mockMvc.perform(patch(baseUrl + "/users/2/change-password").contentType(MediaType.APPLICATION_JSON).content(passwordMapJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Change Password Success"));
  }

  @Test
  @DisplayName("Check changeUserPassword with wrong old password (PATCH)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPasswordWithWrongOldPassword() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    var ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    // Given
    var passwordMap = new HashMap<String, String>();
    passwordMap.put("oldPassword", "123456");
    passwordMap.put("newPassword", "Abc12345");
    passwordMap.put("confirmNewPassword", "Abc12345");

    var passwordMapJson = objectMapper.writeValueAsString(passwordMap);

    mockMvc.perform(patch(baseUrl + "/users/2/change-password").contentType(MediaType.APPLICATION_JSON).content(passwordMapJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED))
        .andExpect(jsonPath("$.message").value("username or password is incorrect."))
        .andExpect(jsonPath("$.data").value("Old password is incorrect."));
  }

  @Test
  @DisplayName("Check changeUserPassword with new password not matching confirm new password (PATCH)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPasswordWithNewPasswordNotMatchingConfirmNewPassword() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    var ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    // Given
    var passwordMap = new HashMap<String, String>();
    passwordMap.put("oldPassword", "654321");
    passwordMap.put("newPassword", "Abc12345");
    passwordMap.put("confirmNewPassword", "Abc123456");

    var passwordMapJson = objectMapper.writeValueAsString(passwordMap);

    mockMvc.perform(patch(baseUrl + "/users/2/change-password").contentType(MediaType.APPLICATION_JSON).content(passwordMapJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("New password and confirm new password do not match."));
  }

  @Test
  @DisplayName("Check changeUserPassword with new password not conforming to password policy (PATCH)")
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
  void testChangeUserPasswordWithNewPasswordNotConformingToPasswordPolicy() throws Exception {
    var resultActions = mockMvc.perform(post(baseUrl + "/users/login").with(httpBasic("eric", "654321"))); // httpBasic() is from spring-security-test.
    var mvcResult = resultActions.andDo(print()).andReturn();
    var contentAsString = mvcResult.getResponse().getContentAsString();
    var json = new JSONObject(contentAsString);
    var ericToken = "Bearer " + json.getJSONObject("data").getString("token");

    // Given
    var passwordMap = new HashMap<String, String>();
    passwordMap.put("oldPassword", "654321");
    passwordMap.put("newPassword", "short");
    passwordMap.put("confirmNewPassword", "short");

    var passwordMapJson = objectMapper.writeValueAsString(passwordMap);

    mockMvc.perform(patch(baseUrl + "/users/2/change-password").contentType(MediaType.APPLICATION_JSON).content(passwordMapJson).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, ericToken))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
        .andExpect(jsonPath("$.message").value("New password does not conform to password policy."));
  }
}
