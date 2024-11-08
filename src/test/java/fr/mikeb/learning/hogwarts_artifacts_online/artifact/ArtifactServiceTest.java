package fr.mikeb.learning.hogwarts_artifacts_online.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.utils.IdWorker;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.ChatClient;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.Choice;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.Message;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.Wizard;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.dto.WizardDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class ArtifactServiceTest {
  @Mock
  ArtifactRepository artifactRepository;
  @Mock
  IdWorker idWorker;
  @Mock
  ChatClient chatClient;
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
    var thrown = catchThrowable(() -> artifactService.findById("1250808601744904192"));

    // Then - Assert expected outcomes
    assertThat(thrown)
        .isInstanceOf(NotFoundException.class)
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
  void testSummarizeSuccess() throws JsonProcessingException {
    // Given
    var artifactDtos = getArtifactDtos();
    var objectMapper = new ObjectMapper();
    var jsonArray = objectMapper.writeValueAsString(artifactDtos);

    var messages = List.of(
        new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
        new Message("user", jsonArray)
    );
    var chatRequest = new ChatRequest("gpt-3.5-turbo", messages);
    var chatResponse = new ChatResponse(List.of(new Choice(0, new Message("assistant", "A summary of two artifacts owned by Albus Dumbledore."))));
    given(chatClient.generate(chatRequest)).willReturn(chatResponse);

    // When
    var summary = artifactService.summarize(artifactDtos);

    // Then
    assertThat(summary).isEqualTo("A summary of two artifacts owned by Albus Dumbledore.");
    verify(chatClient, times(1)).generate(chatRequest);
  }

  private static List<ArtifactDto> getArtifactDtos() {
    var wizardDto = new WizardDto(1, "Albus Dombledore", 2);
    return List.of(
        new ArtifactDto("1250808601744904191", "Deluminator", "A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter. It is used to remove or absorb (as well as return) the light from any light source to provide cover to the user.", "ImageUrl", wizardDto),
        new ArtifactDto("1250808601744904193", "Elder Wand", "The Elder Wand, known throughout history as the Deathstick or the Wand of Destiny, is an extremely powerful wand made of elder wood with a core of Thestral tail hair.", "ImageUrl", wizardDto)
    );
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
    update.setName("Invisibility Cloak");
    update.setDescription("An invisibility cloak is used to make the wearer invisible.");
    update.setImgUrl("ImageUrl");

    given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.empty());

    // When
    assertThrows(
        NotFoundException.class,
        () -> artifactService.update("1250808601744904192", update)
    );

    // Then
    verify(artifactRepository, times(1)).findById("1250808601744904192");
  }

  @Test
  void testDeleteSuccess() {
    // Given
    var artifact = new Artifact();
    artifact.setId("1250808601744904192");
    artifact.setName("Invisibility Cloak");
    artifact.setDescription("An invisibility cloak is used to make the wearer invisible.");
    artifact.setImgUrl("ImageUrl");

    given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(artifact));
    doNothing().when(artifactRepository).deleteById("1250808601744904192");

    // When
    artifactService.delete("1250808601744904192");

    // Then
    verify(artifactRepository, times(1)).findById("1250808601744904192");
    verify(artifactRepository, times(1)).deleteById("1250808601744904192");
  }

  @Test
  void testDeleteNotFound() {
    // Given
    given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.empty());

    // When
    assertThrows(
        NotFoundException.class,
        () -> artifactService.delete("1250808601744904192")
    );

    // Then
    verify(artifactRepository, times(1)).findById("1250808601744904192");
  }
}