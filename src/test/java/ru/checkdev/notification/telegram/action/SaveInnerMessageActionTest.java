package ru.checkdev.notification.telegram.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.repository.InnerMessageRepositoryFake;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.EurekaUriProvider;
import ru.checkdev.notification.service.InnerMessageService;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;

import static org.assertj.core.api.Assertions.assertThat;

class SaveInnerMessageActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    @Mock
    private EurekaUriProvider uriProvider;
    private Message message;
    private Update update;
    SessionTg sessionTg;
    UserTelegramService userTelegramService;
    InnerMessageService innerMessageService;
    SaveInnerMessageAction saveInnerMessageAction;

    @BeforeEach
    public void init() {
        sessionTg = new SessionTg();
        message = new Message();
        update = new Update();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        innerMessageService = new InnerMessageService(
                new InnerMessageRepositoryFake(), userTelegramService, uriProvider);
        saveInnerMessageAction = new SaveInnerMessageAction(sessionTg, innerMessageService);
    }

    @Test
    void handle() {
        message.setChat(CHAT);
        message.setText("test");
        update.setMessage(message);

        saveInnerMessageAction.handle(update);

        assertThat(innerMessageService.findByUserIdAndReadFalse(-1)).isNotEmpty();
        assertThat(innerMessageService.findByUserIdAndReadFalse(-1).get(0).getText()).isEqualTo("test");
    }
}