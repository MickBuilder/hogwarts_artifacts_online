package fr.mikeb.learning.hogwarts_artifacts_online.wizard;

import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import fr.mikeb.learning.hogwarts_artifacts_online.wizard.converter.WizardToWizardDtoConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wizards")
public class WizardController {
  private final WizardService wizardService;
  private final WizardToWizardDtoConverter wizardToWizardDtoConverter;

  public WizardController(WizardService wizardService, WizardToWizardDtoConverter wizardToWizardDtoConverter) {
    this.wizardService = wizardService;
    this.wizardToWizardDtoConverter = wizardToWizardDtoConverter;
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
}
