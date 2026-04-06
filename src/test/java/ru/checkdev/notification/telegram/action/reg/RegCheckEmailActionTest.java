package ru.checkdev.notification.telegram.action.reg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
class RegCheckEmailActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private RegCheckEmailAction regCheckEmailAction;
    private SessionTg sessionTg;
    private Message message;
    private Update update;

    @BeforeEach
    public void init() {
        sessionTg = new SessionTg();
        regCheckEmailAction = new RegCheckEmailAction(sessionTg);
        message = new Message();
        update = new Update();
    }

    @Test
    void whenRegCheckEmailActionEmailNotValidThenReturnMessageEmailIncorrect() {
        message.setChat(CHAT);
        message.setText("email.ru");
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", message.getText());
        String ls = System.lineSeparator();
        String expect = new StringBuilder().append("Email: ")
                .append(message.getText())
                .append(" не корректный.").append(ls)
                .append("попробуйте снова.").append(ls)
                .append("/new").toString();

        SendMessage sendMessage = (SendMessage) regCheckEmailAction.handle(update).get();
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }


    @Test
    void whenRegCheckEmailActionEmailCorrectThenReturnEmptyMessage() {
        message.setChat(CHAT);
        message.setText("email@email.ru");
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", message.getText());

        Optional<BotApiMethod> botApiMessage = regCheckEmailAction.handle(update);

        assertThat(botApiMessage).isEmpty();
    }
}