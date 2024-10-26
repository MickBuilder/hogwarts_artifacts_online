package fr.mikeb.learning.hogwarts_artifacts_online.hogwartsuser.dto;

import jakarta.validation.constraints.NotEmpty;

public record UserDto( Long id,
    @NotEmpty(message = "username is required.")
    String username,
    boolean enabled,
    @NotEmpty(message = "roles are required.")
    String roles
) { }
