package ua.bondoleks.telegram_openAI_bot.openai;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ua.bondoleks.telegram_openAI_bot.openai.api.CreateTranscriptionRequest;
import ua.bondoleks.telegram_openAI_bot.openai.api.OpenAIClient;

import java.io.File;

@Service
@AllArgsConstructor
public class TranscribeVoiceToTextService {

    private final OpenAIClient openAIClient;

    public String transcribe(File audioFile) {
        var response = openAIClient.createTranscription(CreateTranscriptionRequest.builder()
                        .audioFile(audioFile)
                        .model("whisper-1")
                .build());
        return response.text();
    }

}
