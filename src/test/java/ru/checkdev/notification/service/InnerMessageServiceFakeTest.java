package ru.checkdev.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.CategoryWithTopicDTO;
import ru.checkdev.notification.repository.InnerMessageRepositoryFake;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepository;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class InnerMessageServiceFakeTest {

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private ServiceInstance serviceInstance;
    private EurekaUriProvider uriProvider;
    private InnerMessageRepositoryFake innerMessageRepository;

    @BeforeEach
    void setUp() {
        uriProvider = new EurekaUriProvider(discoveryClient);
        innerMessageRepository = new InnerMessageRepositoryFake();
    }

    @Test
    public void whenSaveBotMessageAndGetTheSame() {
        UserTelegramRepository userTelegramRepositoryFake = new UserTelegramRepositoryFake(
                new SubscribeTopicRepositoryFake());
        UserTelegramService userTelegramService = new UserTelegramService(userTelegramRepositoryFake);
        InnerMessageService innerMessageService = new InnerMessageService(
                innerMessageRepository, userTelegramService, uriProvider);
        InnerMessage botMessage = innerMessageService.saveMessage(
                new InnerMessage(1, 10, "text",
                        new Timestamp(System.currentTimeMillis()), false)
        );

        List<InnerMessage> result = innerMessageService.findByUserIdAndReadFalse(botMessage.getUserId());

        assertThat(result).contains(botMessage);
    }

    @Test
    public void whenSaveTopicMessagesForSubscribers() throws URISyntaxException {
        CategoryWithTopicDTO categoryWithTopic = new CategoryWithTopicDTO(
                1, "Category_1", 1, "Topic_1", 1, 3);
        List<Integer> categorySubscribersIds = List.of(1);
        List<Integer> topicSubscribersIds = List.of(2);
        InnerMessageService service = new InnerMessageService(
                innerMessageRepository, null, uriProvider);
        List<ServiceInstance> serviceInstances = Collections.singletonList(serviceInstance);
        Mockito.when(discoveryClient.getInstances(Mockito.anyString())).thenReturn(serviceInstances);
        Mockito.when(serviceInstance.getUri()).thenReturn(new URI("null"));

        service.saveMessagesForSubscribers(categoryWithTopic, categorySubscribersIds, topicSubscribersIds);
        List<InnerMessage> topicMessages = service.findByUserIdAndReadFalse(2);

        assertThat(topicMessages.get(0).getText())
                .isEqualTo("Появилось новое собеседование по теме Topic_1."
                        + System.lineSeparator()
                        + "Ссылка на собеседование: null/interview/1");
    }
}