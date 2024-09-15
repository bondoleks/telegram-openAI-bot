package ua.bondoleks.telegram_openAI_bot.openai;

import lombok.Builder;
import ua.bondoleks.telegram_openAI_bot.openai.api.Message;

import java.util.List;

@Builder
public record ChatHistory(
        List<Message> chatMessages
) {
}
