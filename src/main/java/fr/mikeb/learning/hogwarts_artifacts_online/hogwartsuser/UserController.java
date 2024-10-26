package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser;

import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.converter.UserDtoToUserConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.converter.UserToUserDtoConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.dto.UserDto;
import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import org.springframework.web.bind.annotation.GetMapping;
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
}
