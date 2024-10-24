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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WizardServiceTest {
  @Mock
  WizardRepository wizardRepository;
  @InjectMocks
  WizardService wizardService;
  List<Wizard> wizards;

  @BeforeEach
  void setUp() {
    wizards = new ArrayList<>();

    var w1 = new Wizard();
    w1.setId(1);
    w1.setName("Albus Dumbledore");
    w1.addArtifact(new Artifact());
    w1.addArtifact(new Artifact());
    wizards.add(w1);

    var w2 = new Wizard();
    w2.setId(2);
    w2.setName("Harry Potter");
    w2.addArtifact(new Artifact());
    w2.addArtifact(new Artifact());
    wizards.add(w2);

    var w3 = new Wizard();
    w3.setId(3);
    w3.setName("Neville Longbottom");
    w3.addArtifact(new Artifact());
    wizards.add(w3);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testFindByIdSuccess() {
    // Given
    var wizard = wizards.getFirst();

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

    verify(wizardRepository, times(1)).findById(1);
  }

  @Test
  void testFindAllSuccess() {
    // Given
    given(wizardRepository.findAll()).willReturn(wizards);

    // When
    var expectedArtifacts = wizardService.findAll();

    // Then
    assertThat(expectedArtifacts.size()).isEqualTo(wizards.size());
    verify(wizardRepository, times(1)).findAll();
  }

  @Test
  void testSaveSuccess() {
    // Given
    var newWizard = new Wizard();
    newWizard.setName("Hermione Granger");

    given(wizardRepository.save(newWizard)).willReturn(newWizard);

    // When
    var savedWizard = wizardService.save(newWizard);

    // Then
    assertThat(savedWizard.getName()).isEqualTo(newWizard.getName());
    assertThat(savedWizard.getNumberOfArtifacts()).isEqualTo(0);
    verify(wizardRepository, times(1)).save(newWizard);
  }

  @Test
  void testUpdateSuccess() {
    // Given
    var oldWizard = wizards.get(1);

    var update = oldWizard;
    update.setName("Harry Potter-update");

    given(wizardRepository.findById(2)).willReturn(Optional.of(oldWizard));
    given(wizardRepository.save(oldWizard)).willReturn(oldWizard);

    // When
    var updatedArtifact = wizardService.update(2, update);

    // Then
    assertThat(updatedArtifact.getName()).isEqualTo(update.getName());
    verify(wizardRepository, times(1)).findById(2);
    verify(wizardRepository, times(1)).save(update);
  }

  @Test
  void testUpdateNotFound() {
    // Given
    var update = new Wizard();
    update.setName("Harry Potter-update");

    given(wizardRepository.findById(5)).willReturn(Optional.empty());

    // When
    assertThrows(
        NotFoundException.class,
        () -> wizardService.update(5, update)
    );

    // Then
    verify(wizardRepository, times(1)).findById(5);
  }

  @Test
  void testDeleteSuccess() {
    // Given
    var wizard = wizards.get(1);

    given(wizardRepository.findById(2)).willReturn(Optional.of(wizard));
    doNothing().when(wizardRepository).deleteById(2);

    // When
    wizardService.delete(2);

    // Then
    verify(wizardRepository, times(1)).findById(2);
    verify(wizardRepository, times(1)).deleteById(2);
  }

  @Test
  void testDeleteNotFound() {
    // Given
    given(wizardRepository.findById(5)).willReturn(Optional.empty());

    // When
    assertThrows(
        NotFoundException.class,
        () -> wizardService.delete(5)
    );

    // Then
    verify(wizardRepository, times(1)).findById(5);
  }
}