package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;

import static org.assertj.core.api.Assertions.assertThat;

class BindAskEmailActionTest {
    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private BindAskEmailAction bindAskEmailAction;
    private Message message;
    private Update update;

    @BeforeEach
    void setUp() {
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        bindAskEmailAction = new BindAskEmailAction(userTelegramService);
        message = new Message();
        update = new Update();
    }

    @Test
    void whenBindAskEmailAndChatAlreadyBoundThenReturnMessage() {
        message.setChat(CHAT);
        update.setMessage(message);
        userTelegramService.save(new UserTelegram(0, 1, CHAT.getId(), false));

        SendMessage sendMessage = (SendMessage) bindAskEmailAction.handle(update).get();

        assertThat(sendMessage.getText())
                .isEqualTo("К данному аккаунту Telegram уже привязан аккаунт CheckDev");
    }

    @Test
    void whenBindAskEmailThenReturnPrompt() {
        message.setChat(CHAT);
        update.setMessage(message);

        SendMessage sendMessage = (SendMessage) bindAskEmailAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo("Введите email (логин) пользователя:");
    }
}
