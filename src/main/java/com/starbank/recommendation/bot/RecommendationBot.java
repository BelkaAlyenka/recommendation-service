package com.starbank.recommendation.bot;

import com.starbank.recommendation.dto.RecommendationDto;
import com.starbank.recommendation.dto.RecommendationResponseDto;
import com.starbank.recommendation.repository.readonly.UserReadOnlyEntity;
import com.starbank.recommendation.repository.readonly.UserRepository;
import com.starbank.recommendation.service.RecommendationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;

import java.util.List;

@Component
public class RecommendationBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final String botToken;
    private final String botName;
    private final UserRepository userRepository;
    private final RecommendationService recommendationService;

    public RecommendationBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.name}") String botName,
            UserRepository userRepository,
            RecommendationService recommendationService) {
        this.botToken = botToken;
        this.botName = botName;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText().trim();
            long chatId = message.getChatId();

            if (messageText.equals("/start") || messageText.equalsIgnoreCase("/help")) {
                sendTextMessage(chatId, """
                        Приветствуем в сервисе рекомендаций StarBank!
                                                
                        Доступные команды:
                        /recommend <username> — Получить персональные предложения по продуктам.
                        """);
                return;
            }

            if (messageText.startsWith("/recommend")) {
                String[] parts = messageText.split("\\s+");
                if (parts.length < 2) {
                    sendTextMessage(chatId, "Пожалуйста, укажите Ваше имя. Пример: /recommend ivan_ivanov");
                    return;
                }

                String username = parts[1];
                processRecommendationCommand(chatId, username);
                return;
            }

            sendTextMessage(chatId, "Неизвестная команда. Используйте /recommend <username> для получения рекомендаций.");
        }
    }

    private void processRecommendationCommand(long chatId, String username) {
        List<UserReadOnlyEntity> users = userRepository.findByUsername(username);

        if (users == null || users.isEmpty() || users.size() > 1) {
            sendTextMessage(chatId, "Пользователь не найден");
            return;
        }

        UserReadOnlyEntity user = users.get(0);

        RecommendationResponseDto recommendations = recommendationService.getRecommendations(user.getId());
        List<RecommendationDto> productList = recommendations.recommendations();

        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("Здравствуйте ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");
        responseBuilder.append("Новые продукты для вас:\n");

        if (productList == null || productList.isEmpty()) {
            responseBuilder.append("• На данный момент нет доступных предложений.");
        } else {
            for (RecommendationDto product : productList) {
                responseBuilder.append("• ").append(product.name()).append(" — ").append(product.text()).append("\n");
            }
        }

        sendTextMessage(chatId, responseBuilder.toString().trim());
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}