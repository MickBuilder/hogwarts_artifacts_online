package fr.mikeb.learning.hogwarts_artifacts_online.wizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.Artifact;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.dto.WizardDto;
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
class WizardControllerTest {
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ObjectMapper objectMapper;
  @MockBean
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
  void testFindWizardByIdSuccess() throws Exception {
    // Given
    given(wizardService.findById(1)).willReturn(wizards.getFirst());

    // When and then
    mockMvc.perform(get("/api/v1/wizards/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find One Success"))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.name").value("Albus Dumbledore"))
        .andExpect(jsonPath("$.data.numberOfArtifacts").value(2));
  }

  @Test
  void testFindWizardByIdNotFound() throws Exception {
    // Given
    given(wizardService.findById(1)).willThrow(new NotFoundException("wizard", "1"));

    // When and then
    mockMvc.perform(get("/api/v1/wizards/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(false))
        .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Could not find wizard with Id 1 :("))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  void testFindAllWizardsSuccess() throws Exception {
    // Given
    given(wizardService.findAll()).willReturn(wizards);

    // When and then
    mockMvc.perform(get("/api/v1/wizards").accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Find All Success"))
        .andExpect(jsonPath("$.data", Matchers.hasSize(wizards.size())))
        .andExpect(jsonPath("$.data[0].id").value(1))
        .andExpect(jsonPath("$.data[0].name").value("Albus Dumbledore"))
        .andExpect(jsonPath("$.data[1].id").value(2))
        .andExpect(jsonPath("$.data[1].name").value("Harry Potter"))
        .andExpect(jsonPath("$.data[2].numberOfArtifacts").value(1));
  }

  @Test
  void testAddWizardSuccess() throws Exception {
    // Given
    var wizardDto = new WizardDto(null,
        "Hermione Granger",
        null);
    var json = objectMapper.writeValueAsString(wizardDto);

    var savedWizard = new Wizard();
    savedWizard.setId(4);
    savedWizard.setName("Hermione Granger");

    given(this.wizardService.save(Mockito.any(Wizard.class))).willReturn(savedWizard);

    // When and then
    mockMvc.perform(post("/api/v1/wizards").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.flag").value(true))
        .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
        .andExpect(jsonPath("$.message").value("Add Success"))
        .andExpect(jsonPath("$.data.id").isNotEmpty())
        .andExpect(jsonPath("$.data.name").value(savedWizard.getName()))
        .andExpect(jsonPath("$.data.numberOfArtifacts").value(0));
  }
}