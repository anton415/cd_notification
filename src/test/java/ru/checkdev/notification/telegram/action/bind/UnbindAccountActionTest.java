package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.dto.ProfileTgDTO;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.service.TgCall;

import static org.assertj.core.api.Assertions.assertThat;

class UnbindAccountActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private SessionTg sessionTg;
    private TgCallStub tgCallStub;
    private UserTelegramService userTelegramService;
    private UnbindAccountAction unbindAccountAction;
    private Update update;
    private Message message;

    @BeforeEach
    void setUp() {
        sessionTg = new SessionTg();
        tgCallStub = new TgCallStub();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        unbindAccountAction = new UnbindAccountAction(sessionTg, tgCallStub, userTelegramService);
        update = new Update();
        message = new Message();
    }

    @Test
    @DisplayName("Если введённые логин и пароль соответствуют привязанному аккаунту, то отвязываем его")
    void whenUnbindWithMatchedUserTelegramThenOk() {
        message.setChat(CHAT);
        update.setMessage(message);
        UserTelegram userTelegram = new UserTelegram(0, 5, 1L, false);
        userTelegramService.save(userTelegram);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "mail@test.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "secret");
        tgCallStub.setResult(new ProfileTgDTO(5, "name", "mail@test.ru"));

        SendMessage sendMessage = (SendMessage) unbindAccountAction.handle(update).get();

        assertThat(userTelegramService.findByChatId(1L)).isEmpty();
        assertThat(sendMessage.getText()).isEqualTo("Ваш аккаунт CheckDev отвязан от текущего аккаунта Telegram");
    }

    @Test
    @DisplayName("Если к текущему Telegram аккаунту ничего не привязано, то возвращаем сообщение и не обращаемся к auth")
    void whenUnbindWithoutUserTelegramThenMessageAccountIsNotBind() {
        message.setChat(CHAT);
        update.setMessage(message);

        SendMessage sendMessage = (SendMessage) unbindAccountAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo("К данному аккаунту Telegram не привязан аккаунт CheckDev");
        assertThat(tgCallStub.wasCalled()).isFalse();
    }

    @Test
    @DisplayName("Если логин и пароль не находят пользователя CheckDev, то отвязка не происходит")
    void whenUserNotFoundThenReturnMessageUserNotFound() {
        message.setChat(CHAT);
        update.setMessage(message);
        userTelegramService.save(new UserTelegram(0, 5, 1L, false));
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "mail@test.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "secret");

        SendMessage sendMessage = (SendMessage) unbindAccountAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo("Пользователь не найден");
        assertThat(userTelegramService.findByChatId(1L)).isPresent();
    }

    @Test
    @DisplayName("Если логин и пароль принадлежат другому аккаунту CheckDev, то отвязка запрещена")
    void whenProfileDoesNotMatchBoundAccountThenReturnMismatchMessage() {
        message.setChat(CHAT);
        update.setMessage(message);
        userTelegramService.save(new UserTelegram(0, 5, 1L, false));
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "mail@test.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "secret");
        tgCallStub.setResult(new ProfileTgDTO(9, "name", "mail@test.ru"));

        SendMessage sendMessage = (SendMessage) unbindAccountAction.handle(update).get();

        assertThat(sendMessage.getText())
                .isEqualTo("Указанный аккаунт CheckDev не привязан к данному аккаунту Telegram");
        assertThat(userTelegramService.findByChatId(1L)).isPresent();
    }

    @Test
    @DisplayName("Если auth недоступен, то возвращаем сообщение о недоступности сервиса")
    void whenServiceUnavailableThenReturnServiceErrorMessage() {
        message.setChat(CHAT);
        update.setMessage(message);
        userTelegramService.save(new UserTelegram(0, 5, 1L, false));
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "mail@test.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "secret");
        tgCallStub.setError(new IllegalStateException("No connection"));

        SendMessage sendMessage = (SendMessage) unbindAccountAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo(String.format(
                "Сервис недоступен, попробуйте позже%s%s", System.lineSeparator(), "/start"
        ));
        assertThat(userTelegramService.findByChatId(1L)).isPresent();
    }

    private static class TgCallStub implements TgCall {
        private Object result;
        private RuntimeException error;
        private boolean called;

        public void setResult(Object result) {
            this.result = result;
            this.error = null;
        }

        public void setError(RuntimeException error) {
            this.error = error;
            this.result = null;
        }

        public boolean wasCalled() {
            return called;
        }

        @Override
        public Mono<Profile> doGet(String url) {
            return Mono.empty();
        }

        @Override
        public Mono<Object> doPost(String url, Profile profile) {
            called = true;
            if (error != null) {
                return Mono.error(error);
            }
            return Mono.justOrEmpty(result);
        }

        @Override
        public Mono<Object> doPost(String url) {
            called = true;
            if (error != null) {
                return Mono.error(error);
            }
            return Mono.justOrEmpty(result);
        }
    }
}
