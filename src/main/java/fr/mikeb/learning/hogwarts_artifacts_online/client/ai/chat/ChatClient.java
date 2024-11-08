package fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat;

import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatRequest;
import fr.mikeb.learning.hogwarts_artifacts_online.client.ai.chat.dto.ChatResponse;

public interface ChatClient {
  ChatResponse generate(ChatRequest chatRequest);
}
