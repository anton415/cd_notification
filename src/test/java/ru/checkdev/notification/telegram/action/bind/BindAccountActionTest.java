package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.dto.ProfileTgDTO;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.service.TgCallStub;

import static org.assertj.core.api.Assertions.assertThat;

class BindAccountActionTest {
    /**
     * Поле заведено для отладки тестов
     * При указании данного email пользователя сервис бросает exception
     */
    private static final String ERROR_MAIL = "error@exception.er";
    private static final Chat CHAT = new Chat(1L, "type");

    private SessionTg sessionTg;
    private Update update;
    private Message message;
    private UserTelegramService userTelegramService;
    private TgCallStub tgCallStub;
    private BindAccountAction bindAccountAction;


    @BeforeEach
    void setUp() {
        sessionTg = new SessionTg();
        update = new Update();
        message = new Message();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        tgCallStub = new TgCallStub();
        bindAccountAction = new BindAccountAction(
                sessionTg, tgCallStub, userTelegramService);
    }

    @Test
    void whenBindThenMessageAccountHasBound() {
        message.setChat(CHAT);
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "email@email.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "password");
        tgCallStub.withPostHandler((url, profile) -> new ProfileTgDTO(-23, "FakeName", profile.getEmail()));
        String expectMessage = "Ваш аккаунт CheckDev успешно привязан к данному аккаунту Telegram";

        BotApiMethod botApiMethod = bindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(userTelegramService.findByChatId(1L)).isPresent();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    void whenExceptionAtBindingThenMessageServiceIsUnavailable() {
        message.setChat(CHAT);
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", ERROR_MAIL);
        tgCallStub.withPostHandler((url, profile) -> {
            throw new IllegalArgumentException("Service is error");
        });
        String expect = String.format("Сервис недоступен, попробуйте позже%s%s", System.lineSeparator(), "/start");

        BotApiMethod botApiMethod = bindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenBindAccountAlreadyBoundToAnotherTelegramThenReturnErrorMessage() {
        message.setChat(CHAT);
        update.setMessage(message);
        userTelegramService.save(new ru.checkdev.notification.domain.UserTelegram(0, -23, 2L, false));
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "email@email.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "password");
        tgCallStub.withPostHandler((url, profile) -> new ProfileTgDTO(-23, "FakeName", profile.getEmail()));
        String expect = "Данный аккаунт CheckDev уже привязан к другому аккаунту Telegram";

        BotApiMethod botApiMethod = bindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }
}
