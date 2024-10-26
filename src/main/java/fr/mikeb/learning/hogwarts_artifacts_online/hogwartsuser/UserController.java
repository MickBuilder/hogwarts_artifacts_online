package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.converter.UserDtoToUserConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.converter.UserToUserDtoConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.dto.UserDto;
import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {
  private final UserService userService;
  private final UserToUserDtoConverter userToUserDtoConverter;
  private final UserDtoToUserConverter userDtoToUserConverter;

  public UserController(UserService userService, UserToUserDtoConverter userToUserDtoConverter, UserDtoToUserConverter userDtoToUserConverter) {
    this.userService = userService;
    this.userToUserDtoConverter = userToUserDtoConverter;
    this.userDtoToUserConverter = userDtoToUserConverter;
  }

  @GetMapping
  public Result<List<UserDto>> findAllUsers() {
    var users =  userService.findAll().stream()
        .map(userToUserDtoConverter::convert)
        .toList();

    return new Result<>(true, StatusCode.SUCCESS, "Find All Success", users);
  }

  @GetMapping("/{userId}")
  public Result<UserDto> findUserById(@PathVariable long userId) {
    var user = userService.findById(userId);
    var userDto = userToUserDtoConverter.convert(user);
    return new Result<>(true, StatusCode.SUCCESS, "Find One Success", userDto);
  }

  @PostMapping
  public Result<UserDto> addUser(@Valid @RequestBody HogwartsUser user) {
    var savedUser = userService.save(user);
    var savedUserDto = userToUserDtoConverter.convert(savedUser);
    return new Result<>(true, StatusCode.SUCCESS, "Add Success", savedUserDto);
  }

  @PutMapping("/{userId}")
  public Result<UserDto> updateUser(@PathVariable long userId, @Valid @RequestBody UserDto updateDto) {
    var update = userDtoToUserConverter.convert(updateDto);
    var updatedUser = userService.update(userId, update);
    var updatedUserDto = userToUserDtoConverter.convert(updatedUser);
    return new Result<>(true, StatusCode.SUCCESS, "Update Success", updatedUserDto);
  }
}
