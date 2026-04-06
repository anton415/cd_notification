package ru.checkdev.notification.telegram.action.forget;

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
class ForgetActionTest {
    /**
     * Поле заведено для отладки тестов
     * При указании данного ERROR_ID в качестве userId в моделях данных сервис бросает exception.
     */
    private static final Chat CHAT = new Chat(1L, "type");
    private static final String ERROR_ID = "-23";

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private ServiceInstance serviceInstance;
    private UserTelegramService userTelegramService;
    private EurekaUriProvider uriProvider;
    private SessionTg sessionTg;
    private Update update;
    private Message message;
    ForgetAction forgetAction;

    @BeforeEach
    void setUp() {
        uriProvider = new EurekaUriProvider(discoveryClient);
        sessionTg = new SessionTg();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        update = new Update();
        message = new Message();
        forgetAction = new ForgetAction(sessionTg, new FakeTgCallConsole(uriProvider), userTelegramService);
    }

    @Test
    void whenForgetActionNotChatIdThenMessageAccountNotRegistered() {
        update.setMessage(message);
        message.setChat(CHAT);
        forgetAction.handle(update);
        String text = "Данный аккаунт Telegram на сайте не зарегистрирован";

        BotApiMethod<Message> botApiMethod = forgetAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;

        assertThat(text).isEqualTo(sendMessage.getText());
    }

    @Test
    void whenForgetActionChatIdIsPresentThenMessageNewPassword() throws URISyntaxException {
        update.setMessage(message);
        message.setChat(CHAT);
        message.setText("password");
        UserTelegram userTelegram = new UserTelegram(0, 1, CHAT.getId(), false);
        userTelegramService.save(userTelegram);
        List<ServiceInstance> serviceInstances = Collections.singletonList(serviceInstance);
        Mockito.when(discoveryClient.getInstances(Mockito.anyString())).thenReturn(serviceInstances);
        Mockito.when(serviceInstance.getUri()).thenReturn(new URI("null"));

        BotApiMethod<Message> botApiMethod = forgetAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actual = sendMessage.getText();
        String passInMessage = getPassInMessage(actual);
        String text = "Ваш новый пароль:" + System.lineSeparator() + passInMessage;

        assertThat(text).isEqualTo(actual);
    }

    @Test
    void whenForgetActionWebClientExceptionThenMessageServiceError() {
        update.setMessage(message);
        message.setChat(CHAT);
        message.setText("password");
        UserTelegram userTelegram = new UserTelegram(0, Integer.parseInt(ERROR_ID), CHAT.getId(), false);
        userTelegramService.save(userTelegram);
        String expect = "Сервис не доступен попробуйте позже";

        BotApiMethod<Message> botApiMethod = forgetAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }

    private String getPassInMessage(String textMessage) {
        String startPassIndex = "Ваш новый пароль:" + System.lineSeparator();
        return textMessage.substring(startPassIndex.length());
    }
}