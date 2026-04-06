package ru.checkdev.notification.telegram.action.notifi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.action.notify.NotifyAction;

import static org.assertj.core.api.Assertions.assertThat;

class NotifyActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private SessionTg sessionTg;
    private UserTelegramService userTelegramService;
    private NotifyAction action;
    private Update update;
    private Message message;

    @BeforeEach
    void init() {
        sessionTg = new SessionTg();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        action = new NotifyAction(sessionTg, userTelegramService);
        update = new Update();
        message = new Message();
    }

    @Test
    void whenNoChatIdRegisteredThenReturnMessageAccountNotRegistered() {
        message.setChat(CHAT);
        update.setMessage(message);
        String expected = "Данный аккаунт Telegram не зарегистрирован на сайте."
                + System.lineSeparator()
                + "Для регистрации, пожалуйста, воспользуйтесь командой /start";

        BotApiMethod<Message> actualMessage = action.handle(update).get();
        String actual = ((SendMessage) actualMessage).getText();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void whenUserAlreadyNotifiableThenReturnMessageAlreadyNotifiableAndSessionTgSaveUserId() {
        message.setChat(CHAT);
        update.setMessage(message);
        UserTelegram userTelegram = new UserTelegram(0, 1, CHAT.getId(), true);
        userTelegramService.save(userTelegram);

        BotApiMethod<Message> handledMessage = action.handle(update).get();
        String actualMessage = ((SendMessage) handledMessage).getText();
        String actualUserId = sessionTg.get(String.valueOf(CHAT.getId()), "userId", "");

        assertThat(actualMessage).isEqualTo("Уведомления в телеграмм уже включены.");
        assertThat(actualUserId).isEqualTo(String.valueOf(userTelegram.getUserId()));
    }

    @Test
    void whenHandleMessageThenReturnNotificationsActivatedMessageAndSessionTgSaveUserIdAndNotifiableChanged() {
        message.setChat(CHAT);
        update.setMessage(message);
        UserTelegram userTelegram = new UserTelegram(0, 1, CHAT.getId(), false);
        userTelegramService.save(userTelegram);

        BotApiMethod<Message> handledMessage = action.handle(update).get();
        String actualMessage = ((SendMessage) handledMessage).getText();
        String actualUserId = sessionTg.get(String.valueOf(CHAT.getId()), "userId", "");
        UserTelegram tgUserFromDB = userTelegramService.findByUserId(userTelegram.getUserId()).get();

        assertThat(actualMessage).isEqualTo("Вы подписались на уведомления с сайта телеграмм бота.");
        assertThat(actualUserId).isEqualTo(String.valueOf(userTelegram.getUserId()));
        assertThat(tgUserFromDB.isNotifiable()).isTrue();
    }

}