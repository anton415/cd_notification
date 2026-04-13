package ru.checkdev.notification.telegram.action.check;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.service.TgCallStub;

import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

class CheckActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private SessionTg sessionTg;
    private TgCallStub tgCallStub;
    private CheckAction checkAction;
    private Update update;
    private Message message;

    @BeforeEach
    void setUp() {
        sessionTg = new SessionTg();
        tgCallStub = new TgCallStub();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        checkAction = new CheckAction(sessionTg, tgCallStub, userTelegramService);
        update = new Update();
        message = new Message();
    }

    @Test
    void whenNotChatId() {
        update.setMessage(message);
        message.setChat(CHAT);
        checkAction.handle(update);
        String text = "Данный аккаунт Telegram на сайте не зарегистрирован";

        BotApiMethod<Message> botApiMethod = checkAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(text).isEqualTo(sendMessage.getText());
    }

    @Test
    void whenHandleChatIdIsPresentThenReturnMessage() {
        update.setMessage(message);
        message.setChat(CHAT);
        UserTelegram userTelegram = new UserTelegram(0, 1, CHAT.getId(), false);
        userTelegramService.save(userTelegram);
        tgCallStub.withGetHandler(url -> new Profile(1, "FakeName", "FakeEmail",
                "FakePassword", true, Calendar.getInstance()));
        message.setChat(CHAT);
        String ls = System.lineSeparator();
        String text = "ФИО:" + ls
                + "FakeName" + ls
                + "Email:" + ls
                + "FakeEmail" + ls;

        BotApiMethod<Message> botApiMethod = checkAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(text).isEqualTo(sendMessage.getText());
    }

    @Test
    void whenHandleChatIdIsPresentThenReturnServiceError() {
        update.setMessage(message);
        message.setChat(CHAT);
        UserTelegram userTelegram = new UserTelegram(0, -23, CHAT.getId(), false);
        userTelegramService.save(userTelegram);
        tgCallStub.withGetHandler(url -> {
            throw new IllegalArgumentException("Service is error");
        });
        message.setChat(CHAT);
        String text = "Сервис не доступен попробуйте позже";

        BotApiMethod<Message> botApiMethod = checkAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(text).isEqualTo(sendMessage.getText());
    }
}
