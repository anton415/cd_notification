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

class BindPutEmailActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private BindPutEmailAction bindPutEmailAction;
    private SessionTg sessionTg;
    private Message message;
    private Update update;

    @BeforeEach
    void setUp() {
        sessionTg = new SessionTg();
        bindPutEmailAction = new BindPutEmailAction(sessionTg);
        message = new Message();
        update = new Update();
    }

    @Test
    void whenPutEmailActionThenSaveEmailInSession() {
        message.setChat(CHAT);
        message.setText("mail@test.ru");
        update.setMessage(message);

        Optional<BotApiMethod> handleResult = bindPutEmailAction.handle(update);

        assertThat(handleResult).isEmpty();
        assertThat(sessionTg.get(String.valueOf(CHAT.getId()), "email", ""))
                .isEqualTo("mail@test.ru");
    }
}
