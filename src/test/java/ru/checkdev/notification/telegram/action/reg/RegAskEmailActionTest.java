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
class RegAskEmailActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    private UserTelegramService userTelegramService;
    private RegAskEmailAction askEmailAction;
    private Message message;
    private Update update;

    @BeforeEach
    public void init() {
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        askEmailAction = new RegAskEmailAction(userTelegramService);
        message = new Message();
        update = new Update();
    }

    @Test
    void whenAskEmailActionChatIdIsPresentThenReturnMessageUserIsPresent() {
        message.setChat(CHAT);
        update.setMessage(message);
        UserTelegram userTelegram = new UserTelegram(0, 1, CHAT.getId(), false);
        userTelegramService.save(userTelegram);
        String expect = "Данный аккаунт Telegram уже зарегистрирован на сайте";

        SendMessage sendMessage = (SendMessage) askEmailAction.handle(update).get();
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenAskEmailActionChatIdIsEmptyThenReturnMessageEnterEmail() {
        message.setChat(CHAT);
        update.setMessage(message);
        String expect = "Введите email для регистрации:";

        SendMessage sendMessage = (SendMessage) askEmailAction.handle(update).get();
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }
}