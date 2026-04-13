package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.dto.ProfileTgDTO;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.service.TgCallStub;

import static org.assertj.core.api.Assertions.assertThat;

class UnbindAccountActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private SessionTg sessionTg;
    private TgCallStub tgCallStub;
    private UnbindAccountAction unbindAccountAction;
    private Update update;
    private Message message;

    @BeforeEach
    void setUp() {
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        sessionTg = new SessionTg();
        tgCallStub = new TgCallStub();
        unbindAccountAction = new UnbindAccountAction(sessionTg, tgCallStub, userTelegramService);
        update = new Update();
        message = new Message();
    }

    @Test
    void whenUnbindWithUserTelegramThenOk() {
        message.setChat(CHAT);
        update.setMessage(message);
        UserTelegram userTelegram = new UserTelegram(0, 10, 1L, false);
        userTelegramService.save(userTelegram);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "mail@mail.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "password");
        tgCallStub.withPostHandler((url, profile) -> new ProfileTgDTO(10, "User", profile.getEmail()));
        String expectMessage = "Ваш аккаунт CheckDev отвязан от текущего аккаунта Telegram";

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(userTelegramService.findByChatId(1L)).isEmpty();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    void whenUnbindWithWrongCredentialsThenReturnNotFoundMessage() {
        message.setChat(CHAT);
        update.setMessage(message);
        userTelegramService.save(new UserTelegram(0, 10, 1L, false));
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "mail@mail.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "wrong");
        String expectMessage = "Пользователь не найден";

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(actualMessage).isEqualTo(expectMessage);
        assertThat(userTelegramService.findByChatId(1L)).isPresent();
    }

    @Test
    void whenUnbindWithAnotherAccountCredentialsThenReturnErrorMessage() {
        message.setChat(CHAT);
        update.setMessage(message);
        userTelegramService.save(new UserTelegram(0, 10, 1L, false));
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "mail@mail.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "password");
        tgCallStub.withPostHandler((url, profile) -> new ProfileTgDTO(11, "Other", profile.getEmail()));
        String expectMessage = "Введены данные другого аккаунта CheckDev";

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(actualMessage).isEqualTo(expectMessage);
        assertThat(userTelegramService.findByChatId(1L)).isPresent();
    }

    @Test
    void whenUnbindServiceUnavailableThenReturnErrorMessage() {
        message.setChat(CHAT);
        update.setMessage(message);
        userTelegramService.save(new UserTelegram(0, 10, 1L, false));
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "mail@mail.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "password");
        tgCallStub.withPostHandler((url, profile) -> {
            throw new IllegalArgumentException("Service is error");
        });
        String expectMessage = String.format("Сервис недоступен, попробуйте позже%s%s",
                System.lineSeparator(), "/start");

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(actualMessage).isEqualTo(expectMessage);
        assertThat(userTelegramService.findByChatId(1L)).isPresent();
    }
}
