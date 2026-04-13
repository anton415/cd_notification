package ru.checkdev.notification.telegram.action.reg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.dto.ProfileTgDTO;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.service.TgCall;

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
    private TgCallStub tgCallStub;
    private Message message;
    private Update update;

    @BeforeEach
    public void init() {
        sessionTg = new SessionTg();
        tgCallStub = new TgCallStub();
        regCheckEmailAction = new RegCheckEmailAction(sessionTg, tgCallStub);
        message = new Message();
        update = new Update();
    }

    @Test
    @DisplayName("При регистрации нового пользователя, если email указан некорректно, то возвращаем сообщение об ошибке и очищаем email в сессии")
    void whenRegCheckEmailActionEmailNotValidThenReturnMessageEmailIncorrect() {
        message.setChat(CHAT);
        message.setText("email.ru");
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", message.getText());
        String ls = System.lineSeparator();
        String expect = new StringBuilder().append("Email указан некорректно.").append(ls)
                .append("Попробуйте снова.").append(ls)
                .append("/new").toString();

        SendMessage sendMessage = (SendMessage) regCheckEmailAction.handle(update).get();
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
        assertThat(sessionTg.get(String.valueOf(CHAT.getId()), "email", "value"))
                .isEmpty();
    }


    @Test
    void whenRegCheckEmailActionEmailCorrectThenReturnEmptyMessage() {
        message.setChat(CHAT);
        message.setText("email@email.ru");
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", message.getText());
        tgCallStub.setResult(null);

        Optional<BotApiMethod> botApiMessage = regCheckEmailAction.handle(update);

        assertThat(botApiMessage).isEmpty();
    }

    @Test
    @DisplayName("При регистрации нового пользователя, если email уже занят, то возвращаем сообщение об ошибке и очищаем email в сессии")
    void whenRegCheckEmailActionEmailAlreadyExistsThenReturnMessageEmailBusy() {
        message.setChat(CHAT);
        message.setText("email@email.ru");
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", message.getText());
        tgCallStub.setResult(new ProfileTgDTO(1, "name", message.getText()));
        String ls = System.lineSeparator();
        String expect = new StringBuilder().append("Данный email уже занят.").append(ls)
                .append("Попробуйте снова.").append(ls)
                .append("/new").toString();

        SendMessage sendMessage = (SendMessage) regCheckEmailAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo(expect);
        assertThat(sessionTg.get(String.valueOf(CHAT.getId()), "email", "value"))
                .isEmpty();
    }

    @Test
    @DisplayName("При регистрации нового пользователя, если auth недоступен, то возвращаем сообщение об ошибке сервиса")
    void whenRegCheckEmailActionServiceUnavailableThenReturnServiceErrorMessage() {
        message.setChat(CHAT);
        message.setText("email@email.ru");
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", message.getText());
        tgCallStub.setError(new IllegalStateException("No Connection"));
        String ls = System.lineSeparator();
        String expect = String.format("Сервис не доступен попробуйте позже%s%s", ls, "/start");

        SendMessage sendMessage = (SendMessage) regCheckEmailAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo(expect);
        assertThat(sessionTg.get(String.valueOf(CHAT.getId()), "email", "value"))
                .isEmpty();
    }

    private static class TgCallStub implements TgCall {
        private Object result;
        private RuntimeException error;

        public void setResult(Object result) {
            this.result = result;
            this.error = null;
        }

        public void setError(RuntimeException error) {
            this.error = error;
            this.result = null;
        }

        @Override
        public Mono<Profile> doGet(String url) {
            return Mono.empty();
        }

        @Override
        public Mono<Object> doPost(String url, Profile profile) {
            if (error != null) {
                return Mono.error(error);
            }
            return Mono.justOrEmpty(result);
        }

        @Override
        public Mono<Object> doPost(String url) {
            if (error != null) {
                return Mono.error(error);
            }
            return Mono.justOrEmpty(result);
        }
    }
}
