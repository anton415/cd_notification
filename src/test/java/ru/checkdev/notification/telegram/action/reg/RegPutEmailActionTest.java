package ru.checkdev.notification.telegram.action.reg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.SessionTg;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dmitry Stepanov, user Dmitry
 * @since 27.11.2023
 */
class RegPutEmailActionTest {


    private static final Chat CHAT = new Chat(1L, "type");

    private RegPutEmailAction regPutEmailAction;
    private SessionTg sessionTg;
    private Message message;
    private Update update;

    @BeforeEach
    public void init() {
        sessionTg = new SessionTg();
        regPutEmailAction = new RegPutEmailAction(sessionTg);
        message = new Message();
        update = new Update();
    }

    @Test
    void whenPutEmailActionThenReturnSessionTgEmail() {
        message.setChat(CHAT);
        message.setText("newEmailUser");
        update.setMessage(message);
        String expect = message.getText();

        Optional<BotApiMethod> handleResult = regPutEmailAction.handle(update);
        String actual = sessionTg.get(String.valueOf(CHAT.getId()), "email", "");

        assertThat(handleResult).isEmpty();
        assertThat(actual).isEqualTo(expect);
    }
}