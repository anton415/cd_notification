package ru.checkdev.notification.telegram.action.bind;

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
class BindAccountActionTest {
    /**
     * Поле заведено для отладки тестов
     * При указании данного email пользователя сервис бросает exception
     */
    private static final String ERROR_MAIL = "error@exception.er";
    private static final Chat CHAT = new Chat(1L, "type");

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private ServiceInstance serviceInstance;
    private EurekaUriProvider uriProvider;
    private SessionTg sessionTg;
    private Update update;
    private Message message;
    private UserTelegramService userTelegramService;
    private BindAccountAction bindAccountAction;


    @BeforeEach
    void setUp() {
        uriProvider = new EurekaUriProvider(discoveryClient);
        sessionTg = new SessionTg();
        update = new Update();
        message = new Message();
        userTelegramService = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
        bindAccountAction = new BindAccountAction(
                sessionTg, new FakeTgCallConsole(uriProvider), userTelegramService);
    }

    @Test
    void whenBindThenMessageAccountHasBound() throws URISyntaxException {
        message.setChat(CHAT);
        update.setMessage(message);
        List<ServiceInstance> serviceInstances = Collections.singletonList(serviceInstance);
        Mockito.when(discoveryClient.getInstances(Mockito.anyString())).thenReturn(serviceInstances);
        Mockito.when(serviceInstance.getUri()).thenReturn(new URI("null"));
        sessionTg.put(String.valueOf(CHAT.getId()), "email", "email@email.ru");
        sessionTg.put(String.valueOf(CHAT.getId()), "password", "password");
        String expectMessage = "Ваш аккаунт CheckDev успешно привязан к данному аккаунту Telegram";

        BotApiMethod botApiMethod = bindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actualMessage = sendMessage.getText();

        assertThat(userTelegramService.findByChatId(1L)).isPresent();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    void whenExceptionAtBindingThenMessageServiceIsUnavailable() {
        message.setChat(CHAT);
        update.setMessage(message);
        sessionTg.put(String.valueOf(CHAT.getId()), "email", ERROR_MAIL);
        String expect = String.format("Сервис недоступен, попробуйте позже%s%s", System.lineSeparator(), "/start");

        BotApiMethod botApiMethod = bindAccountAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }
}