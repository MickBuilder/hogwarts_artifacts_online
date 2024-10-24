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

}
