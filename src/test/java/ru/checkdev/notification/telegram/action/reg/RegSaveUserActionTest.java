package ru.checkdev.notification.telegram.action.reg;

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

/**
 * @author Dmitry Stepanov, user Dmitry
 * @since 27.11.2023
 */
class RegSaveUserActionTest {
    /**
     * Поле заведено для отладки тестов
     * При указании данного email пользователя сервис бросает exception
     */
    private static final String ERROR_MAIL = "error@exception.er";
    private static final Chat CHAT = new Chat(1L, "type");
    private static final String URL_SITE_AUTH = "www";
    private SessionTg sessionTg;
    private Message message;
    private Update update;
    private UserTelegramService userTelegramService;
    private TgCallStub tgCallStub;
    private RegSaveUserAction regSaveUserAction;

    @BeforeEach
    void setUp() {
        sessionTg = new SessionTg();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        tgCallStub = new TgCallStub();
        regSaveUserAction =
                new RegSaveUserAction(sessionTg,
                        tgCallStub, userTelegramService, URL_SITE_AUTH);
        message = new Message();
        update = new Update();
    }

    @Test
    void whenSaveActionNotEmailThenReturnMessageRepeat() {
        message.setChat(CHAT);
        update.setMessage(message);
        String ls = System.lineSeparator();
        String text = "Пройдите регистрацию заново" + ls + "/new";

        BotApiMethod botApiMethod = regSaveUserAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(text).isEqualTo(sendMessage.getText());
    }

    @Test
    void whenCallBackThenOk() {
        message.setChat(CHAT);
        update.setMessage(message);
        String email = "email@email.ru";
        String name = "nameUser";
        sessionTg.put(String.valueOf(CHAT.getId()), "email", email);
        sessionTg.put(String.valueOf(CHAT.getId()), "name", name);
        tgCallStub.withPostHandler((url, profile) -> new ProfileTgDTO(-23, profile.getUsername(), profile.getEmail()));

        BotApiMethod botApiMethod = regSaveUserAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actual = sendMessage.getText();
        String ls = System.lineSeparator();
        String passwordInMessage = getPassInMessage(actual, URL_SITE_AUTH);
        String expect = new StringBuilder().append("Вы зарегистрированы: ").append(ls)
                .append("ФИО: ").append(name).append(ls)
                .append("Email: ").append(email).append(ls)
                .append("Пароль : ").append(passwordInMessage).append(ls)
                .append(URL_SITE_AUTH).toString();

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenCallBackThenErrorService() {
        message.setChat(CHAT);
        update.setMessage(message);
        String name = "nameUser";
        sessionTg.put(String.valueOf(CHAT.getId()), "email", ERROR_MAIL);
        sessionTg.put(String.valueOf(CHAT.getId()), "name", name);
        tgCallStub.withPostHandler((url, profile) -> {
            throw new IllegalArgumentException("Service is error");
        });
        String ls = System.lineSeparator();
        String expect = String.format("Сервис не доступен попробуйте позже%s%s", ls, "/start");

        BotApiMethod botApiMethod = regSaveUserAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }

    private String getPassInMessage(String textMessage, String urlSiteAuth) {
        String startDelimiter = "Пароль : ";
        int startPassIndex = textMessage.indexOf(startDelimiter) + startDelimiter.length();
        int endPassIndex = textMessage.lastIndexOf(System.lineSeparator() + urlSiteAuth);
        return textMessage.substring(startPassIndex, endPassIndex);
    }
}
