package ru.checkdev.notification.telegram.action.reg;

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

/**
 * @author Dmitry Stepanov, user Dmitry
 * @since 27.11.2023
 */
class RegAskNameActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private RegAskNameAction regAskNameAction;
    private Message message;
    private Update update;

    @BeforeEach
    public void init() {
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        regAskNameAction = new RegAskNameAction(userTelegramService);
        message = new Message();
        update = new Update();
    }

    @Test
    void whenAskNameActionChatIdIsPresentThenReturnMessageUserIsPresent() {
        message.setChat(CHAT);
        update.setMessage(message);
        UserTelegram userTelegram = new UserTelegram(0, 1, CHAT.getId(), false);
        userTelegramService.save(userTelegram);
        String expect = "Данный аккаунт Telegram уже зарегистрирован на сайте";

        SendMessage sendMessage = (SendMessage) regAskNameAction.handle(update).get();
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenAskNameActionChatIdIsEmptyThenReturnMessageEnterName() {
        message.setChat(CHAT);
        update.setMessage(message);
        String expect = "Введите имя для регистрации нового пользователя:";

        SendMessage sendMessage = (SendMessage) regAskNameAction.handle(update).get();
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }
}