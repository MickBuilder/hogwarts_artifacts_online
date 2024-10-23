package fr.mikeb.learning.hogwarts_artifacts_online.artifact;

import fr.mikeb.learning.hogwarts_artifacts_online.artifact.utils.IdWorker;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.Wizard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArtifactServiceTest {
  @Mock
  ArtifactRepository artifactRepository;
  @Mock
  IdWorker idWorker;
  @InjectMocks
  ArtifactService artifactService;
  List<Artifact> artifacts;

  @BeforeEach
  void setUp() {
    Artifact a1 = new Artifact();
    a1.setId("1250808601744904191");
    a1.setName("Deluminator");
    a1.setDescription("A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter. It is used to remove or absorb (as well as return) the light from any light source to provide cover to the user.");
    a1.setImgUrl("imageUrl");

    Artifact a2 = new Artifact();
    a2.setId("1250808601744904192");
    a2.setName("Invisibility Cloak");
    a2.setDescription("An invisibility cloak is used to make the wearer invisible.");
    a2.setImgUrl("imageUrl");

    artifacts = new ArrayList<>();
    artifacts.add(a1);
    artifacts.add(a2);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testFindByIdSuccess() {
    // Given - Arrange inputs and targets - Define the behavior of Mock object artifactRepository
    var artifact = new Artifact();
    artifact.setId("1250808601744904192");
    artifact.setName("Invisibility Cloak");
    artifact.setDescription("An invisibility cloak is used to make the wearer invisible.");
    artifact.setImgUrl("ImageUrl");

    var wizard = new Wizard();
    wizard.setId(2);
    wizard.setName("Harry Potter");

    artifact.setOwner(wizard);

    given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(artifact));

    // When - Act on the target behavior - When steps should cover the method to be tested
    var returnedArtifact = artifactService.findById("1250808601744904192");

    // Then - Assert expected outcomes
    assertThat(returnedArtifact.getId()).isEqualTo(artifact.getId());
    assertThat(returnedArtifact.getName()).isEqualTo(artifact.getName());
    assertThat(returnedArtifact.getDescription()).isEqualTo(artifact.getDescription());
    assertThat(returnedArtifact.getImgUrl()).isEqualTo(artifact.getImgUrl());
    verify(artifactRepository, times(1)).findById("1250808601744904192");
  }

  @Test
  void testFindByIdNotFound() {
    // Given - Arrange inputs and targets - Define the behavior of Mock object artifactRepository
    given(artifactRepository.findById(Mockito.any(String.class))).willReturn(Optional.empty());

    // When - Act on the target behavior - When steps should cover the method to be tested
    var thrown = catchThrowable(() -> {
      artifactService.findById("1250808601744904192");
    });

    // Then - Assert expected outcomes
    assertThat(thrown)
        .isInstanceOf(ArtifactNotFoundException.class)
        .hasMessage("Could not find artifact with Id 1250808601744904192 :(");
    verify(artifactRepository, times(1)).findById("1250808601744904192");
  }

  @Test
  void testFindAllSuccess() {
    // Given
    given(artifactRepository.findAll()).willReturn(artifacts);

    // When
    var expectedArtifacts = artifactService.findAll();

    // Then
    assertThat(expectedArtifacts.size()).isEqualTo(artifacts.size());
    verify(artifactRepository, times(1)).findAll();
  }

  @Test
  void testSaveSuccess() {
    // Given
    var newArtifact = new Artifact();
    newArtifact.setName("Artifact 3");
    newArtifact.setDescription("Description...");
    newArtifact.setImgUrl("ImageUrl...");

    given(idWorker.nextId()).willReturn(123456L);
    given(artifactRepository.save(newArtifact)).willReturn(newArtifact);

    // When
    var savedArtifact = artifactService.save(newArtifact);

    // Then
    assertThat(savedArtifact.getId()).isEqualTo("123456");
    assertThat(savedArtifact.getName()).isEqualTo(newArtifact.getName());
    assertThat(savedArtifact.getDescription()).isEqualTo(newArtifact.getDescription());
    assertThat(savedArtifact.getImgUrl()).isEqualTo(newArtifact.getImgUrl());
    verify(artifactRepository, times(1)).save(newArtifact);
  }

  @Test
  void testUpdateSuccess() {
    // Given
    var oldArtifact = new Artifact();
    oldArtifact.setId("1250808601744904192");
    oldArtifact.setName("Invisibility Cloak");
    oldArtifact.setDescription("An invisibility cloak is used to make the wearer invisible.");
    oldArtifact.setImgUrl("ImageUrl");

    var update = oldArtifact;
    update.setDescription("A new description");

    given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(oldArtifact));
    given(artifactRepository.save(oldArtifact)).willReturn(oldArtifact);

    // When
    var updatedArtifact = artifactService.update("1250808601744904192", update);

    // Then
    assertThat(updatedArtifact.getId()).isEqualTo(update.getId());
    assertThat(updatedArtifact.getDescription()).isEqualTo(update.getDescription());
    verify(artifactRepository, times(1)).findById("1250808601744904192");
    verify(artifactRepository, times(1)).save(update);
  }

  @Test
  void testUpdateNotFound() {
    // Given
    var update = new Artifact();
    update.setId("1250808601744904192");
    update.setName("Invisibility Cloak");
    update.setDescription("An invisibility cloak is used to make the wearer invisible.");
    update.setImgUrl("ImageUrl");

    given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.empty());

    // When
    assertThrows(
        ArtifactNotFoundException.class,
        () -> artifactService.update("1250808601744904192", update)
    );

    // Then
    verify(artifactRepository, times(1)).findById("1250808601744904192");
  }
}