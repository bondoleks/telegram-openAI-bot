package ua.bondoleks.telegram_openAI_bot.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonWebApp;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.bondoleks.telegram_openAI_bot.openai.ChatGptService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = Logger.getLogger(TelegramBot.class.getName());

    private final BotConfig config;
    private final ChatGptService chatGptService;

    @Value("${bot.webapp.link}")
    String webappLink;

    public TelegramBot(BotConfig config, ChatGptService chatGptService){
        this.config = config;
        this.chatGptService = chatGptService;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String lang = update.getMessage().getFrom().getLanguageCode();

            switch (messageText) {
                case "/start":
                    sendMessage(chatId, helloLocalizationMessage(lang));
                    defaultMenuWebApp(chatId);
                    break;
                default:
                    sendChatAction(chatId);

                    new Thread(() -> {
                        try {
                            String response = chatGptService.getResponseChatForUser(chatId, messageText);
                            sendMessage(chatId, response);
                        } catch (Exception e) {
                            LOGGER.severe("Error in ChatGptService: " + e.getMessage());
                            sendFallbackMessage(chatId);
                        }
                    }).start();
            }
        }
    }

    private String helloLocalizationMessage(String lang) {
        ResourceBundle bundle = ResourceBundle.getBundle("messagesTelegram", new Locale(checkLang(lang)));
        return bundle.getString("hello.message");
    }

    public String checkLang(String lang) {
        if (lang != null && !lang.isEmpty()) {
            return switch (lang) {
                case "uk", "pl", "fr", "de", "es", "kk" -> lang;
                default -> {
                    if (lang.equals("ru")) {
                        yield "uk";
                    }
                    yield "en";
                }
            };
        }
        return "en";
    }


    public void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        defaultInlineWebApp(message);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOGGER.severe("Error sending message: " + e.getMessage());
            sendFallbackMessage(chatId);
        }
    }

    private void defaultMenuWebApp(long chatId){
        WebAppInfo webAppInfo = new WebAppInfo();
        webAppInfo.setUrl(webappLink);
        MenuButtonWebApp menuButtonWebApp = MenuButtonWebApp.builder()
                .text("Button1")
                .webAppInfo(webAppInfo)
                .build();
        SetChatMenuButton setChatMenuButton = SetChatMenuButton.builder()
                .chatId(chatId)
                .menuButton(menuButtonWebApp)
                .build();
        try {
            execute(setChatMenuButton);
        } catch (TelegramApiException e) {
            LOGGER.severe("Error setting menu web app: " + e.getMessage());
            sendFallbackMessage(chatId);
        }
    }

    private void setInlineKeyboard(ReplyKeyboard inlineKeyboard, Object message) {
        if (message instanceof SendMessage) {
            ((SendMessage) message).setReplyMarkup(inlineKeyboard);
        } else if (message instanceof SendPhoto) {
            ((SendPhoto) message).setReplyMarkup(inlineKeyboard);
        } else {
            throw new IllegalArgumentException("Unsupported message type");
        }
    }

    private InlineKeyboardMarkup createDefaultInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineButtons = new ArrayList<>();
        List<InlineKeyboardButton> list = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Button2");

        WebAppInfo webAppInfo = new WebAppInfo();
        webAppInfo.setUrl(webappLink);

        button.setWebApp(webAppInfo);

        list.add(button);
        inlineButtons.add(list);

        inlineKeyboardMarkup.setKeyboard(inlineButtons);
        return inlineKeyboardMarkup;
    }

    private void defaultInlineWebApp(SendMessage message) {
        InlineKeyboardMarkup inlineKeyboardMarkup = createDefaultInlineKeyboard();
        setInlineKeyboard(inlineKeyboardMarkup, message);
    }

    private void defaultInlineWebApp(SendPhoto message) {
        InlineKeyboardMarkup inlineKeyboardMarkup = createDefaultInlineKeyboard();
        setInlineKeyboard(inlineKeyboardMarkup, message);
    }

    public void sendChatAction(long chatId) {
        SendChatAction chatAction = new SendChatAction();
        chatAction.setChatId(String.valueOf(chatId));
        chatAction.setAction(ActionType.TYPING);

        try {
            execute(chatAction);
        } catch (TelegramApiException e) {
            LOGGER.severe("Error sending chat action: " + e.getMessage());
            sendFallbackMessage(chatId);
        }
    }

    private void sendFallbackMessage(long chatId) {
        try {
            SendMessage fallbackMessage = new SendMessage();
            fallbackMessage.setChatId(String.valueOf(chatId));
            fallbackMessage.setText("Something went wrong, please try again later.");
            execute(fallbackMessage);
        } catch (TelegramApiException e) {
            LOGGER.severe("Error sending fallback message: " + e.getMessage());
        }
    }
}