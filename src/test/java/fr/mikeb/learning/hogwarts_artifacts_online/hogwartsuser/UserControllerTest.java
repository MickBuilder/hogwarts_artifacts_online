package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.dto.UserDto;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "dev")
class UserControllerTest {
  @Autowired
  MockMvc mockMvc;
  @Value("${api.endpoint.base-url}")
  String baseUrl;
  @MockBean
  UserService userService;
  List<HogwartsUser> users;
  @Autowired
  ObjectMapper objectMapper;
  @BeforeEach
  void setUp() {
    users = new ArrayList<>();

    var u1 = new HogwartsUser();
    u1.setId(1L);
    u1.setUsername("john");
    u1.setPassword("123456");
    u1.setEnabled(true);
    u1.setRoles("admin user");
    users.add(u1);

    var u2 = new HogwartsUser();
    u2.setId(2L);
    u2.setUsername("eric");
    u2.setPassword("654321");
    u2.setEnabled(true);
    u2.setRoles("user");
    users.add(u2);

    var u3 = new HogwartsUser();
    u3.setId(3L);
    u3.setUsername("tom");
    u3.setPassword("qwerty");
    u3.setEnabled(false);
    u3.setRoles("user");
    users.add(u3);
  }

  @Test
  void testFindAllUsersSuccess() throws Exception {
    // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
    given(userService.findAll()).willReturn(users);

    // When and then
    mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(users.size())))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].username").value("john"))
        .andExpect(jsonPath("$.data[1].id").value(2))
        .andExpect(jsonPath("$.data[1].username").value("eric"));
  }

  @Test
  void testFindUserByIdSuccess() throws Exception {
    // Given
    given(userService.findById(1)).willReturn(users.getFirst());

    // When and then
    mockMvc.perform(get(baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.username").value("john"))
        .andExpect(jsonPath("$.data.roles").value("admin user"));
  }

  @Test
  void testFindUserByIdNotFound() throws Exception {
    // Given
    given(userService.findById(6)).willThrow(new NotFoundException("user", "6"));

    // When and then
    mockMvc.perform(get(baseUrl + "/users/6").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 6 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testAddUserSuccess() throws Exception {
    var user = new HogwartsUser();
    user.setUsername("lily");
    user.setPassword("123456");
    user.setEnabled(true);
    user.setRoles("admin user"); // The delimiter is space.

    var json = objectMapper.writeValueAsString(user);
    user.setId(5L);

    // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
    given(userService.save(Mockito.any(HogwartsUser.class))).willReturn(user);

    // When and then
    mockMvc.perform(post(baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.username").value("lily"))
        .andExpect(jsonPath("$.data.enabled").value(true))
        .andExpect(jsonPath("$.data.roles").value("admin user"));
  }

  @Test
  void testUpdateUserSuccess() throws Exception {
    var update = new UserDto(3L, "tom123", false, "user");

    var updatedUser = new HogwartsUser();
    updatedUser.setId(3L);
    updatedUser.setUsername("tom123"); // Username is changed. It was tom.
    updatedUser.setEnabled(false);
    updatedUser.setRoles("user");

    var json = objectMapper.writeValueAsString(update);

    // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
    given(userService.update(eq(3L), Mockito.any(HogwartsUser.class))).willReturn(updatedUser);

    // When and then
    mockMvc.perform(put(baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Update Success"))
        .andExpect(jsonPath("$.data.id").value(3))
        .andExpect(jsonPath("$.data.username").value("tom123"))
        .andExpect(jsonPath("$.data.enabled").value(false))
        .andExpect(jsonPath("$.data.roles").value("user"));
  }

  @Test
  void testUpdateUserErrorWithNonExistentId() throws Exception {
    // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
    given(userService.update(
        eq(5L),
        Mockito.any(HogwartsUser.class))).willThrow(new NotFoundException("user", "5")
    );
    var userDto = new UserDto(5L, "tom123", false, "user");
    String json = objectMapper.writeValueAsString(userDto);

    // When and then
    mockMvc.perform(put(baseUrl + "/users/5").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testDeleteUserSuccess() throws Exception {
    // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
    doNothing().when(userService).delete(2);

    // When and then
    mockMvc.perform(delete(baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Delete Success"));
  }

  @Test
  void testDeleteUserErrorWithNonExistentId() throws Exception {
    // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
    doThrow(new NotFoundException("user", 5+"")).when(userService).delete(5);

    // When and then
    mockMvc.perform(delete(baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }
}