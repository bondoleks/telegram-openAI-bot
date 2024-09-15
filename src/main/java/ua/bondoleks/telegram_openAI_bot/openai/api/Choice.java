package ua.bondoleks.telegram_openAI_bot.openai.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Choice (
    @JsonProperty("message") Message message
) {}
