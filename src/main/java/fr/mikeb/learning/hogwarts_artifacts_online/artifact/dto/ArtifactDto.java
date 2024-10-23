package fr.mikeb.learning.hogwarts_artifacts_online.artifact.dto;

import fr.mikeb.learning.hogwarts_artifacts_online.wizard.dto.WizardDto;
import jakarta.validation.constraints.NotEmpty;

public record ArtifactDto(
    String id,
    @NotEmpty(message = "name is required.")
    String name,
    @NotEmpty(message = "description is required.")
    String description,
    @NotEmpty(message = "imgUrl is required.")
    String imgUrl,
    WizardDto owner
) {}
