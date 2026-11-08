package com.scoop.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class ClaudeClient {
    private final String apiKey;
    private final String model;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();

    public ClaudeClient(@Value("${app.ia-api-key:}") String apiKey,
                        @Value("${app.claude-model}") String model,
                        @Value("${app.claude-api-url}") String apiUrl,
                        ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }

    public String complete(String prompt, int maxTokens) {
        if (apiKey.isBlank()) throw new IllegalStateException("Claude API key is not configured");
        JsonNode response = restClient.post().uri(apiUrl)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .body(Map.of("model", model, "max_tokens", maxTokens,
                        "messages", new Object[]{Map.of("role", "user", "content", prompt)}))
                .retrieve().body(JsonNode.class);
        JsonNode text = response == null ? null : response.path("content").path(0).path("text");
        if (text == null || !text.isTextual()) throw new RestClientException("Claude response did not contain text");
        return text.asText();
    }

    public JsonNode parse(String json) throws com.fasterxml.jackson.core.JsonProcessingException {
        return objectMapper.readTree(json);
    }
}
