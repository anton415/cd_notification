package ru.checkdev.notification.service;

import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.domain.UserTelegram;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация NotificationMessage для тестов
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 28.11.2023
 */
public class NotificationMessageTgFake implements NotificationMessage<UserTelegram, String, InnerMessage> {
    /**
     * Метод отправляет сообщения пользователям в телеграмм
     *
     * @param targets List<UserTelegram>
     * @param message String message
     * @return List<InnerMessage>
     */
    @Override
    public List<InnerMessage> sendMessage(List<UserTelegram> targets, String message) {
        List<InnerMessage> innerMessages = new ArrayList<>();
        for (UserTelegram user : targets) {
            InnerMessage innerMessage = InnerMessage.of()
                    .userId(user.getUserId())
                    .text(message)
                    .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                    .read(true)
                    .build();
            innerMessages.add(innerMessage);
        }
        return innerMessages;
    }

    /**
     * Метод отправляет сообщения одному пользователю в телеграмм
     *
     * @param target  UserTelegram
     * @param message String
     * @return InnerMessage
     */
    @Override
    public InnerMessage sendMessage(UserTelegram target, String message) {
        return InnerMessage.of()
                .userId(target.getUserId())
                .text(message)
                .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .read(true)
                .build();
    }
}
