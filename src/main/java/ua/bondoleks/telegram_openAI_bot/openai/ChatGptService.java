package ua.bondoleks.telegram_openAI_bot.openai;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ua.bondoleks.telegram_openAI_bot.openai.api.ChatCompletionRequest;
import ua.bondoleks.telegram_openAI_bot.openai.api.Message;
import ua.bondoleks.telegram_openAI_bot.openai.api.OpenAIClient;

@Service
@AllArgsConstructor
public class ChatGptService {

    private final OpenAIClient openAIClient;
    private final ChatGptHistoryService chatGptHistoryService;

    private static final String OPEN_AI_VERSION = "gpt-4o";
    private static final String OPEN_AI_ROLE = "user";

    @Nonnull
    public String getResponseChatForUser(
            Long userId,
            String userTextInput
    ) {
        chatGptHistoryService.createHistoryIfNotExist(userId);
        var history = chatGptHistoryService.addMessageToHistory(
                userId,
                Message.builder()
                        .content(userTextInput)
                        .role(OPEN_AI_ROLE)
                        .build()
        );

        var request = ChatCompletionRequest.builder()
                .model(OPEN_AI_VERSION)
                .messages(history.chatMessages())
                .build();
        var response = openAIClient.createChatCompletion(request);

        var messageFromGpt = response.choices().get(0)
                .message();

        chatGptHistoryService.addMessageToHistory(userId, messageFromGpt);

        return messageFromGpt.content();
    }
}
