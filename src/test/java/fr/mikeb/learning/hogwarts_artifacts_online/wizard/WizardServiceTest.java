package fr.mikeb.learning.hogwarts_artifacts_online.wizard;

import fr.mikeb.learning.hogwarts_artifacts_online.artifact.Artifact;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WizardServiceTest {
  @Mock
  WizardRepository wizardRepository;
  @InjectMocks
  WizardService wizardService;

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testFindByIdSuccess() {
    // Given
    var wizard = new Wizard();
    wizard.setId(1);
    wizard.setName("Albus Dumbledore");
    wizard.addArtifact(new Artifact());
    wizard.addArtifact(new Artifact());

    given(wizardRepository.findById(1)).willReturn(Optional.of(wizard));

    // When
    var returnedWizard = wizardService.findById(1);

    // Then
    assertThat(returnedWizard.getId()).isEqualTo(wizard.getId());
    assertThat(returnedWizard.getName()).isEqualTo(wizard.getName());
    assertThat(returnedWizard.getNumberOfArtifacts()).isEqualTo(2);
    verify(wizardRepository, times(1)).findById(1);
  }

  @Test
  void testFindByIdNotFound() {
    // Given
    given(wizardRepository.findById(1)).willReturn(Optional.empty());

    // When and then
    assertThrows(
        NotFoundException.class,
        () -> wizardService.findById(1)
    );

    // Then
    verify(wizardRepository, times(1)).findById(1);
  }
}