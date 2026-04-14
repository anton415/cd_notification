package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.SessionTg;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BindPutPasswordActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private BindPutPasswordAction bindPutPasswordAction;
    private SessionTg sessionTg;
    private Message message;
    private Update update;

    @BeforeEach
    void setUp() {
        sessionTg = new SessionTg();
        bindPutPasswordAction = new BindPutPasswordAction(sessionTg);
        message = new Message();
        update = new Update();
    }

    @Test
    void whenPutPasswordActionThenSavePasswordInSession() {
        message.setChat(CHAT);
        message.setText("secret");
        update.setMessage(message);

        Optional<BotApiMethod> handleResult = bindPutPasswordAction.handle(update);

        assertThat(handleResult).isEmpty();
        assertThat(sessionTg.get(String.valueOf(CHAT.getId()), "password", ""))
                .isEqualTo("secret");
    }
}
