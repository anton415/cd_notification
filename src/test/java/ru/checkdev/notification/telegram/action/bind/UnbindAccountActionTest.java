package ru.checkdev.notification.telegram.action.bind;

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

import static org.assertj.core.api.Assertions.assertThat;

class UnbindAccountActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private UnbindAccountAction unbindAccountAction;
    private Update update;
    private Message message;

    @BeforeEach
    void setUp() {
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        unbindAccountAction = new UnbindAccountAction(userTelegramService);
        update = new Update();
        message = new Message();
    }

    @Test
    void whenUnbindWithUserTelegramThenOk() {
        message.setChat(CHAT);
        update.setMessage(message);
        UserTelegram userTelegram = new UserTelegram(0, 0, 1L, false);
        userTelegramService.save(userTelegram);
        String expectMessage = "Ваш аккаунт CheckDev отвязан от текущего аккаунта Telegram";

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(userTelegramService.findByChatId(1L)).isEmpty();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    void whenUnbindWithoutUserTelegramThenMessageAccountIsNotBind() {
        message.setChat(CHAT);
        update.setMessage(message);
        String expectMessage = "К данному аккаунту телеграм не привязан аккаунт CheckDev";

        BotApiMethod botApiMethod = unbindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(actualMessage).isEqualTo(expectMessage);
    }

}