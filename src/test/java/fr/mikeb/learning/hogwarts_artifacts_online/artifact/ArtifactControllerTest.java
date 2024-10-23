package fr.mikeb.learning.hogwarts_artifacts_online.artifact;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class ArtifactControllerTest {
  @Autowired
  MockMvc mockMvc;
  @MockBean
  ArtifactService artifactService;
  List<Artifact> artifacts;
  @Autowired
  ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    this.artifacts = new ArrayList<>();

    Artifact a1 = new Artifact();
    a1.setId("1250808601744904191");
    a1.setName("Deluminator");
    a1.setDescription("A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter. It is used to remove or absorb (as well as return) the light from any light source to provide cover to the user.");
    a1.setImgUrl("ImageUrl");
    this.artifacts.add(a1);

    Artifact a2 = new Artifact();
    a2.setId("1250808601744904192");
    a2.setName("Invisibility Cloak");
    a2.setDescription("An invisibility cloak is used to make the wearer invisible.");
    a2.setImgUrl("ImageUrl");
    this.artifacts.add(a2);

    Artifact a3 = new Artifact();
    a3.setId("1250808601744904193");
    a3.setName("Elder Wand");
    a3.setDescription("The Elder Wand, known throughout history as the Deathstick or the Wand of Destiny, is an extremely powerful wand made of elder wood with a core of Thestral tail hair.");
    a3.setImgUrl("ImageUrl");
    this.artifacts.add(a3);

    Artifact a4 = new Artifact();
    a4.setId("1250808601744904194");
    a4.setName("The Marauder's Map");
    a4.setDescription("A magical map of Hogwarts created by Remus Lupin, Peter Pettigrew, Sirius Black, and James Potter while they were students at Hogwarts.");
    a4.setImgUrl("ImageUrl");
    this.artifacts.add(a4);

    Artifact a5 = new Artifact();
    a5.setId("1250808601744904195");
    a5.setName("The Sword Of Gryffindor");
    a5.setDescription("A goblin-made sword adorned with large rubies on the pommel. It was once owned by Godric Gryffindor, one of the medieval founders of Hogwarts.");
    a5.setImgUrl("ImageUrl");
    this.artifacts.add(a5);

    Artifact a6 = new Artifact();
    a6.setId("1250808601744904196");
    a6.setName("Resurrection Stone");
    a6.setDescription("The Resurrection Stone allows the holder to bring back deceased loved ones, in a semi-physical form, and communicate with them.");
    a6.setImgUrl("ImageUrl");
    this.artifacts.add(a6);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testFindArtifactByIdSuccess() throws Exception {
    // Given
    given(artifactService.findById("1250808601744904191")).willReturn(artifacts.getFirst());

    // When and then
    mockMvc.perform(get("/api/v1/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value("1250808601744904191"))
        .andExpect(jsonPath("$.data.name").value("Deluminator"));
  }

  @Test
  void testFindArtifactByIdNotFound() throws Exception {
    // Given
    given(artifactService.findById("1250808601744904191")).willThrow(new ArtifactNotFoundException("1250808601744904191"));

    // When and then
    mockMvc.perform(get("/api/v1/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904191 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testFindAllArtifactsSuccess() throws Exception {
    // Given
    given(artifactService.findAll()).willReturn(artifacts);

    // When and then
    mockMvc.perform(get("/api/v1/artifacts").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(artifacts.size())))
        .andExpect(jsonPath("$.data[0].id").value("1250808601744904191"))
        .andExpect(jsonPath("$.data[0].name").value("Deluminator"))
        .andExpect(jsonPath("$.data[1].id").value("1250808601744904192"))
        .andExpect(jsonPath("$.data[1].name").value("Invisibility Cloak"));
  }

  @Test
  void testAddArtifactSuccess() throws Exception {
    // Given
    var artifactDto = new ArtifactDto(null,
        "Remembrall",
        "A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered.",
        "ImageUrl",
        null);
    String json = objectMapper.writeValueAsString(artifactDto);

    Artifact savedArtifact = new Artifact();
    savedArtifact.setId("1250808601744904197");
    savedArtifact.setName("Remembrall");
    savedArtifact.setDescription("A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered.");
    savedArtifact.setImgUrl("ImageUrl");

    given(this.artifactService.save(Mockito.any(Artifact.class))).willReturn(savedArtifact);

    // When and then
    mockMvc.perform(post("/api/v1/artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.name").value(savedArtifact.getName()))
        .andExpect(jsonPath("$.data.description").value(savedArtifact.getDescription()))
        .andExpect(jsonPath("$.data.imgUrl").value(savedArtifact.getImgUrl()));
  }
}