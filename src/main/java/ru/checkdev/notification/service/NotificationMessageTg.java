package ru.checkdev.notification.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.telegram.Bot;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Метод отправляет сообщение все UserTelegram
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 17.11.2023
 */
@Service
@AllArgsConstructor
@Slf4j
public class NotificationMessageTg implements NotificationMessage<UserTelegram, String, InnerMessage> {
    private final Bot bot;

    /**
     * Метод отправляет сообщения подписанным на оповещения пользователям в телеграмм.
     *
     * @param targets List<UserTelegram>
     * @param message String message
     * @return List<InnerMessage>
     */
    @Override
    public List<InnerMessage> sendMessage(List<UserTelegram> targets, String message) {
        List<InnerMessage> innerMessages = new ArrayList<>();
        for (UserTelegram user : targets) {
            var innerMessage = InnerMessage.of()
                    .userId(user.getUserId())
                    .text(message)
                    .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                    .read(false)
                    .build();
            if (user.isNotifiable()) {
                var messageTg = getSendMessage(user.getChatId(), message);
                try {
                    bot.send(messageTg);
                    innerMessage.setRead(true);
                } catch (Exception e) {
                    log.error("Send message by UserID:{}, from telegram Error:{}", user.getUserId(), e);
                }
            }
            innerMessages.add(innerMessage);
        }
        return innerMessages;
    }

    /**
     * Метод отправляет сообщение одному пользователю в телеграмм, если он подписан на сообщения.
     *
     * @param target  UserTelegram
     * @param message String
     * @return InnerMessage
     */
    @Override
    public InnerMessage sendMessage(UserTelegram target, String message) {
        var innerMessage = InnerMessage.of()
                .userId(target.getUserId())
                .text(message)
                .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .read(false)
                .build();
        if (target.isNotifiable()) {
            var messageTg = getSendMessage(target.getChatId(), message);
            try {
                bot.send(messageTg);
                innerMessage.setRead(true);
            } catch (Exception e) {
                log.error("Send message by UserID:{}, from telegram Error:{}", target.getUserId(), e);
            }
        }
        return innerMessage;
    }

    private SendMessage getSendMessage(Long chatId, String message) {
        var chatIdString = String.valueOf(chatId);
        return new SendMessage(chatIdString, message);
    }
}
