package fr.mikeb.learning.hogwarts_artifacts_online.wizard;

import fr.mikeb.learning.hogwarts_artifacts_online.artifact.ArtifactRepository;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class WizardService {
  private final WizardRepository wizardRepository;
  private final ArtifactRepository artifactRepository;

  public WizardService(WizardRepository wizardRepository, ArtifactRepository artifactRepository) {
    this.wizardRepository = wizardRepository;
    this.artifactRepository = artifactRepository;
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

  public void assignArtifact(int wizardId, String artifactId) {
    // Find this artifact by Id
    var artifact = artifactRepository.findById(artifactId)
        .orElseThrow(() -> new NotFoundException("artifact", artifactId));
    // Find this wizard by Id
    var wizard = wizardRepository.findById(wizardId)
        .orElseThrow(() -> new NotFoundException("wizard", wizardId + ""));

    // Artifact assignment
    // We need to see if the artifact is already owned by some wizard.
    if (artifact.hasOwner()) {
      artifact.getOwner().removeArtifact(artifact);
    }
    wizard.addArtifact(artifact);
  }
}
