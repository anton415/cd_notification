package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;

import static org.assertj.core.api.Assertions.assertThat;

class BindAskEmailActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private BindAskEmailAction bindAskEmailAction;
    private Message message;
    private Update update;

    @BeforeEach
    void setUp() {
        bindAskEmailAction = new BindAskEmailAction(new UserTelegramService(
                new UserTelegramRepositoryFake(new SubscribeTopicRepositoryFake())));
        message = new Message();
        update = new Update();
    }

    @Test
    @DisplayName("При запросе email возвращается сообщение с уточнением, что нужно ввести email (логин) аккаунта CheckDev")
    void whenAskEmailThenReturnPromptWithLoginClarification() {
        message.setChat(CHAT);
        update.setMessage(message);

        SendMessage sendMessage = (SendMessage) bindAskEmailAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo("Введите email (логин) аккаунта CheckDev:");
    }
}
