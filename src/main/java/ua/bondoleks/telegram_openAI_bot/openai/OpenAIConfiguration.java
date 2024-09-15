package ua.bondoleks.telegram_openAI_bot.openai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua.bondoleks.telegram_openAI_bot.openai.api.OpenAIClient;

@Configuration
public class OpenAIConfiguration {

    @Bean
    public OpenAIClient openAIClient(
            @Value("${openai.token}") String botToken,
            RestTemplateBuilder restTemplateBuilder
    ) {
        return new OpenAIClient(botToken, restTemplateBuilder.build());
    }

}
