package fr.mikeb.learning.hogwarts_artifacts_online.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.converter.ArtifactDtoToArtifactConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.converter.ArtifactToArtifactDtoConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/artifacts")
public class ArtifactController {
  private final ArtifactService artifactService;
  private final ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter;
  private final ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter;
  private final MeterRegistry meterRegistry;

  public ArtifactController(ArtifactService artifactService, ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter, ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter, MeterRegistry meterRegistry) {
    this.artifactService = artifactService;
    this.artifactToArtifactDtoConverter = artifactToArtifactDtoConverter;
    this.artifactDtoToArtifactConverter = artifactDtoToArtifactConverter;
    this.meterRegistry = meterRegistry;
  }

  @GetMapping("/{artifactId}")
  public Result<ArtifactDto> findArtifactById(@PathVariable String artifactId) {
    var foundArtifact = artifactService.findById(artifactId);
    meterRegistry.counter("artifact.id." + artifactId).increment();
    var converted = artifactToArtifactDtoConverter.convert(foundArtifact);
    return new Result<>(true, StatusCode.SUCCESS, "Find One Success", converted);
  }

  @GetMapping
  public Result<Page<ArtifactDto>> findAllArtifacts(Pageable pageable) {
    var artifactPage = artifactService.findAll(pageable);
    var artifactDtoPage = artifactPage
        .map(artifactToArtifactDtoConverter::convert);
    return new Result<>(true, StatusCode.SUCCESS, "Find All Success", artifactDtoPage);
  }

  @PostMapping("/search")
  public Result<Page<ArtifactDto>> findArtifactsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
    var artifactPage = artifactService.findByCriteria(searchCriteria, pageable);
    var artifactDtoPage = artifactPage.map(artifactToArtifactDtoConverter::convert);
    return new Result<>(true, StatusCode.SUCCESS, "Search Success", artifactDtoPage);
  }

  @GetMapping("/summary")
  public Result<String> summarizeArtifacts() throws JsonProcessingException {
    var artifactDtos = artifactService.findAll().stream().map(artifactToArtifactDtoConverter::convert).toList();
    var summary = artifactService.summarize(artifactDtos);
    return new Result<>(true, StatusCode.SUCCESS, "Summarize Success", summary);
  }

  @PostMapping
  public Result<ArtifactDto> addArtifact(@Valid @RequestBody ArtifactDto artifactDto) {
    var newArtifact = artifactDtoToArtifactConverter.convert(artifactDto);
    var savedArtifact = artifactService.save(newArtifact);
    var savedArtifactDto = artifactToArtifactDtoConverter.convert(savedArtifact);
    return new Result<>(true, StatusCode.SUCCESS, "Add Success", savedArtifactDto);
  }

  @PutMapping("/{artifactId}")
  public Result<ArtifactDto> updateArtifact(@PathVariable String artifactId, @Valid @RequestBody ArtifactDto artifactDto) {
    var updateArtifact = artifactDtoToArtifactConverter.convert(artifactDto);
    var updatedArtifact = artifactService.update(artifactId, updateArtifact);
    var updatedArtifactDto = artifactToArtifactDtoConverter.convert(updatedArtifact);
    return new Result<>(true, StatusCode.SUCCESS, "Update Success", updatedArtifactDto);
  }

  @DeleteMapping("/{artifactId}")
  public Result<Void> deleteArtifact(@PathVariable String artifactId) {
    artifactService.delete(artifactId);
    return new Result<>(true, StatusCode.SUCCESS, "Delete Success");
  }
}
