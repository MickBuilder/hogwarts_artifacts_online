package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  UserRepository userRepository;
  @Mock
  PasswordEncoder passwordEncoder;
  @InjectMocks
  UserService userService;
  List<HogwartsUser> hogwartsUsers;
  @BeforeEach
  void setUp() {
    var u1 = new HogwartsUser();
    u1.setId(1L);
    u1.setUsername("john");
    u1.setPassword("123456");
    u1.setEnabled(true);
    u1.setRoles("admin user");

    var u2 = new HogwartsUser();
    u2.setId(2L);
    u2.setUsername("eric");
    u2.setPassword("654321");
    u2.setEnabled(true);
    u2.setRoles("user");

    var u3 = new HogwartsUser();
    u3.setId(3L);
    u3.setUsername("tom");
    u3.setPassword("qwerty");
    u3.setEnabled(false);
    u3.setRoles("user");

    hogwartsUsers = new ArrayList<>();
    hogwartsUsers.add(u1);
    hogwartsUsers.add(u2);
    hogwartsUsers.add(u3);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testFindAllSuccess() {
    // Given
    given(userRepository.findAll()).willReturn(hogwartsUsers);

    // When
    var users = userService.findAll();

    // Then
    assertThat(users.size()).isEqualTo(hogwartsUsers.size());
    verify(userRepository, times(1)).findAll();
  }

  @Test
  void testFindByIdSuccess() {
    // Given
    var user = hogwartsUsers.getFirst();
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    // When
    var userFound = userService.findById(1L);

    // Then
    assertThat(userFound.getId()).isEqualTo(user.getId());
    assertThat(userFound.getUsername()).isEqualTo(user.getUsername());
    assertThat(userFound.getRoles()).isEqualTo(user.getRoles());
    verify(userRepository, times(1)).findById(1L);
  }

  @Test
  void testFindByIdNotFound() {
    // Given
    given(userRepository.findById(6L)).willReturn(Optional.empty());

    // When and Then
    assertThrows(
        NotFoundException.class,
        () -> userService.findById(6L)
    );
    verify(userRepository, times(1)).findById(6L);
  }

  @Test
  void testSaveSuccess() {
    // Given
    var newUser = new HogwartsUser();
    newUser.setUsername("lily");
    newUser.setPassword("123456");
    newUser.setEnabled(true);
    newUser.setRoles("user");

    given(passwordEncoder.encode(newUser.getPassword())).willReturn("Encoded Password");
    given(userRepository.save(newUser)).willReturn(newUser);

    // When
    var returnedUser = userService.save(newUser);

    // Then
    assertThat(returnedUser.getUsername()).isEqualTo(newUser.getUsername());
    assertThat(returnedUser.getPassword()).isEqualTo(newUser.getPassword());
    assertThat(returnedUser.isEnabled()).isEqualTo(newUser.isEnabled());
    assertThat(returnedUser.getRoles()).isEqualTo(newUser.getRoles());
    verify(userRepository, times(1)).save(newUser);
  }

  @Test
  void testUpdateByUserSuccess() {
    // Given
    var oldUser = new HogwartsUser();
    oldUser.setId(2L);
    oldUser.setUsername("eric");
    oldUser.setPassword("654321");
    oldUser.setEnabled(true);
    oldUser.setRoles("user");

    var update = new HogwartsUser();
    update.setUsername("eric - update");
    update.setPassword("654321");
    update.setEnabled(true);
    update.setRoles("user admin");

    given(userRepository.findById(2L)).willReturn(Optional.of(oldUser));
    given(userRepository.save(oldUser)).willReturn(oldUser);

    // When
    var updatedUser = userService.update(2, update);

    // Then
    assertThat(updatedUser.getId()).isEqualTo(2);
    assertThat(updatedUser.getUsername()).isEqualTo(update.getUsername());
    assertThat(updatedUser.isEnabled()).isEqualTo(update.isEnabled());
    assertThat(updatedUser.getRoles()).isEqualTo(update.getRoles());
    verify(userRepository, times(1)).findById(2L);
    verify(userRepository, times(1)).save(oldUser);
  }

  @Test
  void testUpdateNotFound() {
    // Given
    var update = new HogwartsUser();
    update.setUsername("john - update");
    update.setPassword("123456");
    update.setEnabled(true);
    update.setRoles("admin user");

    given(userRepository.findById(6L)).willReturn(Optional.empty());

    // When
    var thrown = assertThrows(NotFoundException.class, () -> userService.update(6, update));

    // Then
    assertThat(thrown)
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Could not find user with Id 6 :(");
    verify(userRepository, times(1)).findById(6L);
  }

  @Test
  void testDeleteSuccess() {
    // Given
    var user = new HogwartsUser();
    user.setId(1L);
    user.setUsername("john");
    user.setPassword("123456");
    user.setEnabled(true);
    user.setRoles("admin user");

    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    doNothing().when(userRepository).deleteById(1L);

    // When
    userService.delete(1L);

    // Then
    verify(userRepository, times(1)).deleteById(1L);
  }

  @Test
  void testDeleteNotFound() {
    // Given
    given(userRepository.findById(1L)).willReturn(Optional.empty());

    // When
    var thrown = assertThrows(NotFoundException.class, () -> userService.delete(1));

    // Then
    assertThat(thrown)
        .isInstanceOf(NotFoundException.class)
        .hasMessage("Could not find user with Id 1 :(");
    verify(userRepository, times(1)).findById(1L);
  }
}