package fr.mikeb.learning.hogwarts_artifacts_online.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.utils.IdWorker;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.ChatClient;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.Message;
import fr.mikeb.learning.hogwarts_artifacts_online.system.exception.NotFoundException;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@Transactional
public class ArtifactService {
  private final ArtifactRepository artifactRepository;
  private final IdWorker idWorker;
  private final ChatClient chatClient;

  // Define a map of functions that map criteria keys to their respective specifications
  private static final Map<String, Function<String, Specification<Artifact>>> SPEC_MAP = Map.of(
      "id", ArtifactSpecs::hasId,
      "name", ArtifactSpecs::containsName,
      "description", ArtifactSpecs::containsDescription,
      "ownerName", ArtifactSpecs::hasOwnerName
  );

  public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker, ChatClient chatClient) {
    this.artifactRepository = artifactRepository;
    this.idWorker = idWorker;
    this.chatClient = chatClient;
  }

  @Observed(name = "artifact", contextualName = "findByIdService")
  public Artifact findById(String artifactId) {
    return artifactRepository.findById(artifactId)
        .orElseThrow(() -> new NotFoundException("artifact", artifactId));
  }

  @Timed("findAllArtifactsService.time")
  public List<Artifact> findAll() {
    return artifactRepository.findAll();
  }

  public Page<Artifact> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) {
    var spec = searchCriteria.entrySet().stream()
        .filter(entry -> StringUtils.hasLength(entry.getValue()))
        .map(entry -> SPEC_MAP.getOrDefault(entry.getKey(), s -> null).apply(entry.getValue()))
        .filter(Objects::nonNull)
        .reduce(Specification::and)
        .orElse(Specification.where(null));

    return artifactRepository.findAll(spec, pageable);
  }

  public String summarize(List<ArtifactDto> artifacts) throws JsonProcessingException {
    var objectMapper = new ObjectMapper();
    var jsonArray = objectMapper.writeValueAsString(artifacts);

    var messages = List.of(
        new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
        new Message("user", jsonArray)
    );
    var chatRequest = new ChatRequest("gpt-3.5-turbo", messages);
    return chatClient.generate(chatRequest).choices().getFirst().message().content();
  }

  public Artifact save(Artifact newArtifact) {
    newArtifact.setId(idWorker.nextId() + "");
    return artifactRepository.save(newArtifact);
  }

  public Artifact update(String artifactId, Artifact update) {
    return artifactRepository.findById(artifactId)
        .map(oldArtifact -> {
          oldArtifact.setName(update.getName());
          oldArtifact.setDescription(update.getDescription());
          oldArtifact.setImgUrl(update.getImgUrl());

          return artifactRepository.save(oldArtifact);
        })
        .orElseThrow(() -> new NotFoundException("artifact", artifactId));
  }

  public void delete(String artifactId) {
    artifactRepository.findById(artifactId)
        .orElseThrow(() -> new NotFoundException("artifact", artifactId));

    artifactRepository.deleteById(artifactId);
  }

  public Page<Artifact> findAll(Pageable pageable) {
    return artifactRepository.findAll(pageable);
  }
}
