package fr.mikeb.learning.hogwarts_artifacts_online.artifact;

import fr.mikeb.learning.hogwarts_artifacts_online.artifact.converter.ArtifactDtoToArtifactConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.converter.ArtifactToArtifactDtoConverter;
import fr.mikeb.learning.hogwarts_artifacts_online.artifact.dto.ArtifactDto;
import fr.mikeb.learning.hogwarts_artifacts_online.system.Result;
import fr.mikeb.learning.hogwarts_artifacts_online.system.StatusCode;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/artifacts")
public class ArtifactController {
  private final ArtifactService artifactService;
  private final ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter;
  private final ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter;

  public ArtifactController(ArtifactService artifactService, ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter, ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter) {
    this.artifactService = artifactService;
    this.artifactToArtifactDtoConverter = artifactToArtifactDtoConverter;
    this.artifactDtoToArtifactConverter = artifactDtoToArtifactConverter;
  }

  @GetMapping("/{artifactId}")
  public Result findArtifactById(@PathVariable String artifactId) {
    var foundArtifact = artifactService.findById(artifactId);
    var converted = artifactToArtifactDtoConverter.convert(foundArtifact);
    return new Result(true, StatusCode.SUCCESS, "Find One Success", converted);
  }
  @GetMapping("")
  public Result findAllArtifacts() {
    var foundArtifacts = artifactService.findAll();
    var converted = foundArtifacts.stream()
        .map(artifactToArtifactDtoConverter::convert)
        .toList();
    return new Result(true, StatusCode.SUCCESS, "Find All Success", converted);
  }

  @PostMapping("")
  public Result addArtifact(@Valid @RequestBody ArtifactDto artifactDto) {
    var newArtifact = artifactDtoToArtifactConverter.convert(artifactDto);
    var savedArtifact = artifactService.save(newArtifact);
    var savedArtifactDto = artifactToArtifactDtoConverter.convert(savedArtifact);
    return new Result(true, StatusCode.SUCCESS, "Add Success", savedArtifactDto);
  }
}
