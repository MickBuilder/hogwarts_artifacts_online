package fr.mikeb.learning.hogwarts_artifacts_online;

import fr.mikeb.learning.hogwarts_artifacts_online.artifact.utils.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HogwartsArtifactsOnlineApplication {

  public static void main(String[] args) {
    SpringApplication.run(HogwartsArtifactsOnlineApplication.class, args);
  }

  @Bean
  public IdWorker idWorker() {
    return new IdWorker(1, 1);
  }
}
