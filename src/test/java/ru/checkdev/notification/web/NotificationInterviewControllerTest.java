package ru.checkdev.notification.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.dto.InterviewNotifyDTO;
import ru.checkdev.notification.dto.WisherNotifyDTO;
import ru.checkdev.notification.repository.InnerMessageRepositoryFake;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;
import ru.checkdev.notification.service.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Dmitry Stepanov, user Dmitry
 * @since 28.11.2023
 */

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class NotificationInterviewControllerTest {

    private final SubscribeTopicRepositoryFake subscribeTopicRepositoryFake = new SubscribeTopicRepositoryFake();
    private final InnerMessageRepositoryFake innerMessageRepositoryFake = new InnerMessageRepositoryFake();
    private final NotificationMessageTgFake notificationMessage = new NotificationMessageTgFake();
    private final UserTelegramRepositoryFake userTelegramRepositoryFake =
            new UserTelegramRepositoryFake(subscribeTopicRepositoryFake);
    private final UserTelegramService userTelegramService =
            new UserTelegramService(userTelegramRepositoryFake);

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private ServiceInstance serviceInstance;
    private MessagesGenerator messagesGenerator;
    private NotificationInterviewController notifyController;
    private InnerMessageService innerMessageService;

    @BeforeEach
    void setUp() {
        EurekaUriProvider uriProvider = new EurekaUriProvider(discoveryClient);
        messagesGenerator = new MessagesGenerator(uriProvider);
        innerMessageService = new InnerMessageService(
                innerMessageRepositoryFake, userTelegramService, uriProvider);
        notifyController = new NotificationInterviewController(
                userTelegramService, innerMessageService, notificationMessage, messagesGenerator);
    }

    @Test
    void whenSendMessageSubscribeTopicThenReturnStatusOkBodyListOneMessage() {
        InterviewNotifyDTO interviewNotify = InterviewNotifyDTO.of()
                .id(1)
                .submitterId(2)
                .title("interview1")
                .topicId(2)
                .topicName("topic2")
                .categoryId(2)
                .categoryName("category2")
                .build();
        UserTelegram userTelegram = new UserTelegram(0, 5, 5L, false);
        UserTelegram userTelegramSubmit = new UserTelegram(
                1, interviewNotify.getSubmitterId(), interviewNotify.getSubmitterId(), false);
        subscribeTopicRepositoryFake.save(new SubscribeTopic(0, userTelegram.getUserId(), interviewNotify.getTopicId()));
        subscribeTopicRepositoryFake.save(new SubscribeTopic(1, userTelegramSubmit.getUserId(), interviewNotify.getTopicId()));
        userTelegramService.save(userTelegram);
        userTelegramService.save(userTelegramSubmit);
        String messageExpect = messagesGenerator.getMessageSubscribeTopic(interviewNotify);
        InnerMessage innerMessageExpect = InnerMessage.of()
                .userId(userTelegram.getUserId())
                .text(messageExpect)
                .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .read(true)
                .build();

        ResponseEntity<List<InnerMessage>> expect = ResponseEntity.ok(List.of(innerMessageExpect));
        ResponseEntity<List<InnerMessage>> actual = notifyController.sendMessageSubscribeTopic(interviewNotify);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenSendMessageSubscribeTopicThenReturnStatusOkBodyListEmpty() {
        InterviewNotifyDTO interviewNotify = InterviewNotifyDTO.of()
                .id(1)
                .submitterId(2)
                .title("interview1")
                .topicId(2)
                .topicName("topic2")
                .categoryId(2)
                .categoryName("category2")
                .build();

        ResponseEntity<List<InnerMessage>> expect = ResponseEntity.ok(emptyList());
        ResponseEntity<List<InnerMessage>> actual = notifyController.sendMessageSubscribeTopic(interviewNotify);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenSendMessageSubmitterInterviewThenReturnStatusOkBodyInnerMessage() throws URISyntaxException {
        WisherNotifyDTO wisherNotifyDTO = WisherNotifyDTO.of()
                .interviewId(1)
                .interviewTitle("interview1")
                .submitterId(2)
                .userId(3)
                .contactBy("@contact")
                .build();
        List<ServiceInstance> serviceInstances = Collections.singletonList(serviceInstance);
        Mockito.when(discoveryClient.getInstances(Mockito.anyString())).thenReturn(serviceInstances);
        Mockito.when(serviceInstance.getUri()).thenReturn(new URI("null"));
        UserTelegram userTelegramSubmit = new UserTelegram(
                0, wisherNotifyDTO.getSubmitterId(), wisherNotifyDTO.getSubmitterId(), false);
        userTelegramRepositoryFake.save(userTelegramSubmit);
        String messageExpect = messagesGenerator.getMessageParticipateWisher(wisherNotifyDTO);
        InnerMessage innerMessageExpect = InnerMessage.of()
                .id(1)
                .userId(wisherNotifyDTO.getSubmitterId())
                .text(messageExpect)
                .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .read(false)
                .interviewId(wisherNotifyDTO.getInterviewId())
                .build();

        ResponseEntity<InnerMessage> expect = ResponseEntity.ok(innerMessageExpect);
        ResponseEntity<InnerMessage> actual = notifyController.sendMessageSubmitterInterview(wisherNotifyDTO);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenUserNotInTgThenSaveInner() throws URISyntaxException {
        WisherNotifyDTO wisherNotifyDTO = WisherNotifyDTO.of()
                .interviewId(1)
                .interviewTitle("interview1")
                .submitterId(2)
                .userId(3)
                .contactBy("@contact")
                .build();
        List<ServiceInstance> serviceInstances = Collections.singletonList(serviceInstance);
        Mockito.when(discoveryClient.getInstances(Mockito.anyString())).thenReturn(serviceInstances);
        Mockito.when(serviceInstance.getUri()).thenReturn(new URI("null"));

        ResponseEntity<InnerMessage> actual = notifyController.sendMessageSubmitterInterview(wisherNotifyDTO);
        List<InnerMessage> messages = innerMessageService.findByUserIdAndReadFalse(wisherNotifyDTO.getSubmitterId());

        assertThat(messages.iterator().next()).isEqualTo(actual.getBody());
    }
}