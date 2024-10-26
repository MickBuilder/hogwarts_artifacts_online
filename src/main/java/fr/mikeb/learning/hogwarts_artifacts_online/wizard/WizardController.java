package fr.mikeb.learning.hogwarts_artifacts_online.wizard;

import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.converter.WizardDtoToWizardConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.converter.WizardToWizardDtoConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.dto.WizardDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/wizards")
public class WizardController {
  private final WizardService wizardService;
  private final WizardToWizardDtoConverter wizardToWizardDtoConverter;
  private final WizardDtoToWizardConverter wizardDtoToWizardConverter;

  public WizardController(WizardService wizardService, WizardToWizardDtoConverter wizardToWizardDtoConverter, WizardDtoToWizardConverter wizardDtoToWizardConverter) {
    this.wizardService = wizardService;
    this.wizardToWizardDtoConverter = wizardToWizardDtoConverter;
    this.wizardDtoToWizardConverter = wizardDtoToWizardConverter;
  }

  @GetMapping("/{wizardId}")
  public Result<WizardDto> findWizardById(@PathVariable int wizardId) {
    var foundWizard = wizardService.findById(wizardId);
    var foundWizardDto = wizardToWizardDtoConverter.convert(foundWizard);
    return new Result<>(true, StatusCode.SUCCESS, "Find One Success", foundWizardDto);
  }

  @GetMapping
  public Result<List<WizardDto>> findAllWizards() {
    var allWizards = wizardService.findAll().stream()
        .map(wizardToWizardDtoConverter::convert)
        .toList();
    return new Result<>(true, StatusCode.SUCCESS, "Find All Success", allWizards);
  }

  @PostMapping
  public Result<WizardDto> addWizard(@Valid @RequestBody WizardDto wizardDto) {
    var newWizard = wizardDtoToWizardConverter.convert(wizardDto);
    var savedWizard = wizardService.save(newWizard);
    var savedWizardDto = wizardToWizardDtoConverter.convert(savedWizard);
    return new Result<>(true, StatusCode.SUCCESS, "Add Success", savedWizardDto);
  }

  @PutMapping("/{wizardId}")
  public Result<WizardDto> updateWizard(@PathVariable int wizardId, @Valid @RequestBody WizardDto wizardDto) {
    var updateWizard = wizardDtoToWizardConverter.convert(wizardDto);
    var updatedWizard = wizardService.update(wizardId, updateWizard);
    var updatedWizardDto = wizardToWizardDtoConverter.convert(updatedWizard);
    return new Result<>(true, StatusCode.SUCCESS, "Update Success", updatedWizardDto);
  }

  @PutMapping("/{wizardId}/artifacts/{artifactId}")
  public Result<Void> assignArtifact(@PathVariable int wizardId, @PathVariable String artifactId) {
    this.wizardService.assignArtifact(wizardId, artifactId);
    return new Result<>(true, StatusCode.SUCCESS, "Artifact Assignment Success");
  }

  @DeleteMapping("/{artifactId}")
  public Result<Void> deleteArtifact(@PathVariable int artifactId) {
    wizardService.delete(artifactId);
    return new Result<>(true, StatusCode.SUCCESS, "Delete Success");
  }
}
