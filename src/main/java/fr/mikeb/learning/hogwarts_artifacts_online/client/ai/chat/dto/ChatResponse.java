package fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto;

import java.util.List;

public record ChatResponse(
    List<Choice> choices
) { }
