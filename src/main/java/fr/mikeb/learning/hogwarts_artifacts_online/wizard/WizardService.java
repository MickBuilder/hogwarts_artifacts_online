package fr.mikeb.learning.hogwarts_artifacts_online.wizard;

import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class WizardService {
  private final WizardRepository wizardRepository;

  public WizardService(WizardRepository wizardRepository) {
    this.wizardRepository = wizardRepository;
  }

  public Wizard findById(int wizardId) {
    return wizardRepository.findById(wizardId)
        .orElseThrow(() -> new NotFoundException("wizard", wizardId + ""));
  }

  public List<Wizard> findAll() {
    return wizardRepository.findAll();
  }

  public Wizard save(Wizard wizard) {
    return wizardRepository.save(wizard);
  }

  public Wizard update(int wizardId, Wizard newWizard) {
    return wizardRepository.findById(wizardId).map(wizard -> {
      wizard.setName(newWizard.getName());
      return wizardRepository.save(wizard);
    }).orElseThrow(() -> new NotFoundException("wizard", wizardId + ""));
  }

  public void delete(int wizardId) {
    var wizardToBeDeleted = wizardRepository.findById(wizardId)
        .orElseThrow(() -> new NotFoundException("wizard", wizardId + ""));

    // Before deletion, we will unassign this wizard's owned artifacts.
    wizardToBeDeleted.removeAllArtifacts();
    wizardRepository.deleteById(wizardId);
  }
}
