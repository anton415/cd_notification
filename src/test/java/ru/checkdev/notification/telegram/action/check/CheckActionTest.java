package ru.checkdev.notification.telegram.action.check;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.EurekaUriProvider;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.service.FakeTgCallConsole;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class CheckActionTest {

    private static final Chat CHAT = new Chat(1L, "type");

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private ServiceInstance serviceInstance;
    private UserTelegramService userTelegramService;
    private EurekaUriProvider uriProvider;
    private SessionTg sessionTg;
    private CheckAction checkAction;
    private Update update;
    private Message message;

    @BeforeEach
    void setUp() {
        uriProvider = new EurekaUriProvider(discoveryClient);
        sessionTg = new SessionTg();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        checkAction = new CheckAction(sessionTg, new FakeTgCallConsole(uriProvider), userTelegramService);
        update = new Update();
        message = new Message();
    }

    @Test
    void whenNotChatId() {
        update.setMessage(message);
        message.setChat(CHAT);
        checkAction.handle(update);
        String text = "Данный аккаунт Telegram на сайте не зарегистрирован";

        BotApiMethod<Message> botApiMethod = checkAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(text).isEqualTo(sendMessage.getText());
    }

    @Test
    void whenHandleChatIdIsPresentThenReturnMessage() throws URISyntaxException {
        update.setMessage(message);
        message.setChat(CHAT);
        UserTelegram userTelegram = new UserTelegram(0, 1, CHAT.getId(), false);
        List<ServiceInstance> serviceInstances = Collections.singletonList(serviceInstance);
        Mockito.when(discoveryClient.getInstances(Mockito.anyString())).thenReturn(serviceInstances);
        Mockito.when(serviceInstance.getUri()).thenReturn(new URI("null"));
        userTelegramService.save(userTelegram);
        message.setChat(CHAT);
        String ls = System.lineSeparator();
        String text = "Имя:" + ls
                + "FakeName" + ls
                + "Email:" + ls
                + "FakeEmail" + ls;

        BotApiMethod<Message> botApiMethod = checkAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(text).isEqualTo(sendMessage.getText());
    }

    @Test
    void whenHandleChatIdIsPresentThenReturnServiceError() {
        update.setMessage(message);
        message.setChat(CHAT);
        UserTelegram userTelegram = new UserTelegram(0, -23, CHAT.getId(), false);
        userTelegramService.save(userTelegram);
        message.setChat(CHAT);
        String text = "Сервис не доступен попробуйте позже";

        BotApiMethod<Message> botApiMethod = checkAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(text).isEqualTo(sendMessage.getText());
    }
}