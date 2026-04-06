package ru.checkdev.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.telegram.Bot;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationMessageTgTest {
    private static final String MESSAGE = "message";

    @Mock
    private Bot mockBot;
    private NotificationMessageTg service;

    @BeforeEach
    void setUp() {
        service = new NotificationMessageTg(mockBot);
    }

    @Test
    void whenSendMessageAndTargetListIsEmptyThenGetEmptyList() {
        List<UserTelegram> targets = new ArrayList<>();
        List<InnerMessage> actual = service.sendMessage(targets, MESSAGE);
        assertThat(actual).isEmpty();
    }

    @Test
    void whenSendMessageThenGetListOfInnerMessagesAndReadValueTrueIfUserNotifiedByBot() {
        UserTelegram userFirstNotifiable = new UserTelegram(1, 1, 1111L, true);
        UserTelegram userFirstUnNotifiable = new UserTelegram(2, 2, 2222L, false);
        UserTelegram userSecondNotifiable = new UserTelegram(3, 3, 3333L, true);
        List<UserTelegram> targets = List.of(
                userFirstNotifiable, userFirstUnNotifiable, userSecondNotifiable
        );
        List<InnerMessage> expected = List.of(
                createInnerMessage(userFirstNotifiable, MESSAGE, userFirstNotifiable.isNotifiable()),
                createInnerMessage(userFirstUnNotifiable, MESSAGE, userFirstUnNotifiable.isNotifiable()),
                createInnerMessage(userSecondNotifiable, MESSAGE, userSecondNotifiable.isNotifiable())
        );

        List<InnerMessage> actual = service.sendMessage(targets, MESSAGE);

        verify(mockBot, times(2)).send(any(BotApiMethod.class));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void whenSendMessageAndUserNotifiableThenGetInnerMessagesAndReadValueTrue() {
        UserTelegram target = new UserTelegram(1, 1, 1111L, true);
        InnerMessage expected = createInnerMessage(target, MESSAGE, target.isNotifiable());

        InnerMessage actual = service.sendMessage(target, MESSAGE);

        verify(mockBot, times(1)).send(any(BotApiMethod.class));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void whenSendMessageAndUserNotNotifiableThenGetInnerMessagesAndReadValueFalse() {
        UserTelegram target = new UserTelegram(1, 1, 1111L, false);
        InnerMessage expected = createInnerMessage(target, MESSAGE, target.isNotifiable());

        InnerMessage actual = service.sendMessage(target, MESSAGE);

        verify(mockBot, times(0)).send(any(BotApiMethod.class));
        assertThat(actual).isEqualTo(expected);
    }

    private InnerMessage createInnerMessage(UserTelegram user, String innerMessage, boolean read) {
        return InnerMessage.of()
                .userId(user.getUserId())
                .text(innerMessage)
                .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .read(read)
                .build();
    }

}