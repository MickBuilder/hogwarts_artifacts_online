package fr.mikeb.learning.hogwarts_artifacts_online.system.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class UsableMemoryHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        var path = Path.of(".").toFile(); // Path used to compute available disk space; also use Path instead of File object cause handle utf8
        long diskUsableInBytes = path.getUsableSpace();
        boolean isHealth = diskUsableInBytes >= 10 * 1024 * 1024; // 10 MB
        Status status = isHealth ? Status.UP : Status.DOWN; // UP means there is enough usable memory.
        return Health
                .status(status)
                .withDetail("usable memory", diskUsableInBytes) // In addition to reporting the status, we can attach additional key-value details using the withDetail(key, value)
                .withDetail("threshold", 10 * 1024 * 1024)
                .build();
    }

}