package fr.mikeb.learning.hogwarts_artifacts_online.wizard;

import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.converter.WizardDtoToWizardConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.converter.WizardToWizardDtoConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.dto.WizardDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wizards")
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
  public Result findWizardById(@PathVariable Integer wizardId) {
    var foundWizard = wizardService.findById(wizardId);
    var foundWizardDto = wizardToWizardDtoConverter.convert(foundWizard);
    return new Result(true, StatusCode.SUCCESS, "Find One Success", foundWizardDto);
  }

  @GetMapping
  public Result findAllWizards() {
    var allWizards = wizardService.findAll().stream()
        .map(wizardToWizardDtoConverter::convert)
        .toList();
    return new Result(true, StatusCode.SUCCESS, "Find All Success", allWizards);
  }

  @PostMapping
  public Result addWizard(@Valid @RequestBody WizardDto wizardDto) {
    var newWizard = wizardDtoToWizardConverter.convert(wizardDto);
    var savedWizard = wizardService.save(newWizard);
    var savedWizardDto = wizardToWizardDtoConverter.convert(savedWizard);
    return new Result(true, StatusCode.SUCCESS, "Add Success", savedWizardDto);
  }
}
