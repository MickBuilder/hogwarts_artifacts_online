package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.converter;

import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.HogwartsUser;
import fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.dto.UserDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserToUserDtoConverter implements Converter<HogwartsUser, UserDto> {

    @Override
    public UserDto convert(HogwartsUser source) {
        // We are not setting password in DTO.
      return new UserDto(source.getId(),
          source.getUsername(),
          source.isEnabled(),
          source.getRoles());
    }

}