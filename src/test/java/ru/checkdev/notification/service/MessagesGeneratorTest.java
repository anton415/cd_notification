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
import ru.checkdev.notification.dto.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * CheckDev пробное собеседование
 *
 * @author Dmitry Stepanov
 * @version 18.11.2023 00:41
 */

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class MessagesGeneratorTest {

    @Mock
    private DiscoveryClient discoveryClient;
    @Mock
    private ServiceInstance serviceInstance;
    private EurekaUriProvider uriProvider;
    private MessagesGenerator messagesGenerator;

    @BeforeEach
    void setUp() {
        uriProvider = new EurekaUriProvider(discoveryClient);
        messagesGenerator = new MessagesGenerator(uriProvider);
    }

    @Test
    void generatorMessageSubscribeTopic() {
        InterviewNotifyDTO interviewNotifyDTO = InterviewNotifyDTO.of()
                .id(1)
                .title("title")
                .topicId(2)
                .topicName("topic")
                .categoryId(3)
                .categoryName("category")
                .build();
        String expected = String.format(
                "Вы подписаны на тему:%1$s, из категории:%2$s.%3$s"
                        + "По вашей подписке создана новое собеседование.",
                interviewNotifyDTO.getTopicName(),
                interviewNotifyDTO.getCategoryName(),
                System.lineSeparator());

        String actual = messagesGenerator.getMessageSubscribeTopic(interviewNotifyDTO);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void generatorMessagePublicParticipateWisher() throws URISyntaxException {
        WisherNotifyDTO wisherNotifyDTO = WisherNotifyDTO.of()
                .interviewId(1)
                .interviewTitle("titleInterview")
                .submitterId(2)
                .userId(3)
                .userName("Вася")
                .contactBy("contact")
                .build();
        List<ServiceInstance> serviceInstances = Collections.singletonList(serviceInstance);
        when(discoveryClient.getInstances(Mockito.anyString())).thenReturn(serviceInstances);
        when(serviceInstance.getUri()).thenReturn(new URI("null"));
        String expect = String.format(
                "На ваше собеседование: %s добавился участник: %s%nСсылка на собеседование: null/interview/%s",
                wisherNotifyDTO.getInterviewTitle(),
                wisherNotifyDTO.getUserName(),
                wisherNotifyDTO.getInterviewId());

        String actual = messagesGenerator.getMessageParticipateWisher(wisherNotifyDTO);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenGenerateMessageCancelInterview() {
        CancelInterviewNotificationDTO cancelInterviewDTO = CancelInterviewNotificationDTO.of()
                .interviewId(11)
                .interviewTitle("JPA")
                .submitterId(22)
                .submitterName("IVAN")
                .reasonOfCancel("Нет личного времени")
                .userId(33)
                .build();
        String expect = String.format(
                "Собеседование %s, на которое вы откликнулись, было отменено создателем %s. Причина отмены собеседования: \"%s\". Данное собеседование вам больше недоступно.",
                cancelInterviewDTO.getInterviewTitle(),
                cancelInterviewDTO.getSubmitterName(),
                cancelInterviewDTO.getReasonOfCancel());

        String actual = messagesGenerator.getMessageCancelInterview(cancelInterviewDTO);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenGenerateMessageDismissedWisher() {
        WisherDismissedDTO wisherDismissedDTO = WisherDismissedDTO.of()
                .interviewId(11)
                .interviewTitle("JPA")
                .submitterId(11)
                .submitterName("IVAN")
                .userId(33)
                .build();
        String expect = String.format(
                "Пользователь %s одобрил на собеседование %s другого собеседника. Данное собеседование вам больше недоступно.",
                wisherDismissedDTO.getSubmitterName(),
                wisherDismissedDTO.getInterviewTitle());

        String actual = messagesGenerator.getMessageDismissedWisher(wisherDismissedDTO);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void generatorMessagePublicApprovedWisherWisher() {
        WisherApprovedDTO wisherApprovedDTO = WisherApprovedDTO.of()
                .interviewId(1)
                .wisherId(1)
                .wisherUserId(2)
                .interviewTitle("interview")
                .interviewLink("www")
                .build();
        String expect = String.format(
                "Вы приглашены на собеседование: %s.",
                wisherApprovedDTO.getInterviewTitle()) + System.lineSeparator() + "Ссылка на собеседование: www";

        String actual = messagesGenerator.getMessageApprovedWisher(wisherApprovedDTO);

        assertThat(actual).isEqualTo(expect);
    }
}