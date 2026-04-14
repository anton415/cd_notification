package ru.checkdev.notification.telegram.action.bind;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.TgBot;
import ru.checkdev.notification.telegram.action.Action;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnbindAskEmailActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private UnbindAskEmailAction unbindAskEmailAction;
    private Message message;
    private Update update;

    @BeforeEach
    void setUp() {
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(new SubscribeTopicRepositoryFake()));
        unbindAskEmailAction = new UnbindAskEmailAction(userTelegramService);
        message = new Message();
        update = new Update();
        TgBot.getBindingBy().clear();
    }

    @Test
    @DisplayName("Если аккаунт привязан, то /unbind запрашивает email (логин) аккаунта CheckDev")
    void whenAskEmailThenReturnPromptWithLoginClarification() {
        userTelegramService.save(new UserTelegram(0, 5, CHAT.getId(), false));
        message.setChat(CHAT);
        update.setMessage(message);

        SendMessage sendMessage = (SendMessage) unbindAskEmailAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo("Введите email (логин) аккаунта CheckDev:");
    }

    @Test
    @DisplayName("Если к Telegram ничего не привязано, то /unbind завершает сценарий сразу")
    void whenAccountIsNotBoundThenReturnMessageAndRemoveScenario() {
        message.setChat(CHAT);
        update.setMessage(message);
        TgBot.getBindingBy().put(String.valueOf(CHAT.getId()), List.<Action>of().iterator());

        SendMessage sendMessage = (SendMessage) unbindAskEmailAction.handle(update).get();

        assertThat(sendMessage.getText()).isEqualTo("К данному аккаунту Telegram не привязан аккаунт CheckDev");
        assertThat(TgBot.getBindingBy()).doesNotContainKey(String.valueOf(CHAT.getId()));
    }
}
