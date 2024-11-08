package fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto;

import java.util.List;

public record ChatRequest(
    String model,
    List<Message> messages
) { }
