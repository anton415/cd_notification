package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.assertj.core.api.Assertions.assertThat;

class BindAskPasswordActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private BindAskPasswordAction bindAskPasswordAction;
    private Message message;
    private Update update;

    @BeforeEach
    void setUp() {
        bindAskPasswordAction = new BindAskPasswordAction();
        message = new Message();
        update = new Update();
    }

    @Test
    @DisplayName("При запросе пароля возвращается сообщение с уточнением, что нужно ввести пароль аккаунта CheckDev")
    void whenAskPasswordThenReturnPromptWithAccountClarification() {
        message.setChat(CHAT);
        update.setMessage(message);

        SendMessage sendMessage = (SendMessage) bindAskPasswordAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo("Введите пароль аккаунта CheckDev:");
    }
}
