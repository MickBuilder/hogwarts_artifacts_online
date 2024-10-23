package fr.mikeb.learning.hogwarts_artifacts_online.artifact.converter;

import fr.mikeb.learning.hogwarts_artifacts_online.artifact.Artifact;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.converter.WizardToWizardDtoConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArtifactToArtifactDtoConverter implements Converter<Artifact, ArtifactDto> {

    private final WizardToWizardDtoConverter wizardToWizardDtoConverter;

    public ArtifactToArtifactDtoConverter(WizardToWizardDtoConverter wizardToWizardDtoConverter) {
        this.wizardToWizardDtoConverter = wizardToWizardDtoConverter;
    }

    @Override
    public ArtifactDto convert(Artifact source) {
      return new ArtifactDto(source.getId(),
          source.getName(),
          source.getDescription(),
          source.getImgUrl(),
          source.getOwner() != null ? this.wizardToWizardDtoConverter.convert(source.getOwner()) : null
      );
    }
}