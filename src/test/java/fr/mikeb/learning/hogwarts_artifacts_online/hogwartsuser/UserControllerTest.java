package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
  @Autowired
  MockMvc mockMvc;
  @Value("${api.endpoint.base-url}")
  String baseUrl;
  @MockBean
  UserService userService;
  List<HogwartsUser> users;
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

  @AfterEach
  void tearDown() {
  }

  @Test
  void testFindAllUsersSuccess() throws Exception {
    // Given. Arrange inputs and targets. Define the behavior of Mock object userService.
    given(userService.findAll()).willReturn(this.users);

    // When and then
    mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(this.users.size())))
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
}