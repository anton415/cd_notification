package ru.checkdev.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.CategoryWithTopicDTO;
import ru.checkdev.notification.dto.FeedbackNotificationDTO;
import ru.checkdev.notification.dto.WisherApprovedDTO;
import ru.checkdev.notification.repository.UserTelegramRepository;
import ru.checkdev.notification.telegram.Bot;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationMessagesServiceTest {

    @Mock
    private UserTelegramRepository userTelegramRepository;
    @Mock
    private InnerMessageService innerMessageService;
    @Mock
    private Bot mockBot;
    @Mock
    private MessagesGenerator messagesGenerator;
    @Mock
    private EurekaUriProvider uriProvider;
    private NotificationMessagesService service;

    @BeforeEach
    void setUp() {
        service = new NotificationMessagesService(
                userTelegramRepository, innerMessageService, mockBot, messagesGenerator, uriProvider);
    }

    @Test
    void whenSendMessagesToCategorySubscribersThenRepositoryFindListOfChatId() {
        CategoryWithTopicDTO dto = new CategoryWithTopicDTO(
                1, "category", 1, "topic", 1, 1);
        service.sendMessagesToCategorySubscribers(new ArrayList<>(), dto);
        verify(userTelegramRepository, times(1))
                .findChatIdInUserIdsIfNotifiable(any(List.class));
    }

    @Test
    void whenSendNotificationToCategorySubscriberThenSendMessageSentByBot() {
        long chatId = 1111L;
        CategoryWithTopicDTO dto = new CategoryWithTopicDTO(
                1, "category", 1, "topic", 1, 1);
        String expectedMessage = "В категории "
                + dto.getCategoryName()
                + " появилось новое собеседование."
                + System.lineSeparator()
                + "Ссылка на собеседование: "
                + "null/interview/"
                + dto.getInterviewId();
        when(uriProvider.getUri(Mockito.anyString())).thenReturn("null");
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        service.sendNotificationToCategorySubscriber(chatId, dto);

        verify(mockBot, times(1)).send(captor.capture());
        assertThat(captor.getValue().getChatId()).isEqualTo(String.valueOf(chatId));
        assertThat(captor.getValue().getText()).isEqualTo(expectedMessage);
    }

    @Test
    void whenSendFeedbackNotificationAndUserExistsThenMessageSavedAndBotSendMessage() {
        FeedbackNotificationDTO dto = new FeedbackNotificationDTO(
                2, "sName", "iName", 3);
        long chatId = 1111L;
        String expectedBotMessage = "Пользователь "
                + dto.getSenderName()
                + " оставил Вам отзыв о собеседовании на тему "
                + dto.getInterviewName()
                + System.lineSeparator()
                + "Ссылка на собеседование: "
                + "null/interview/"
                + dto.getInterviewId();
        InnerMessage expectedInnerMessage = createInnerMessage(
                dto.getRecipientId(),
                expectedBotMessage,
                dto.getInterviewId());
        when(userTelegramRepository.findChatIdByUserIdIfNotifiable(any(Integer.class)))
                .thenReturn(Optional.of(chatId));
        when(uriProvider.getUri(Mockito.anyString())).thenReturn("null");
        ArgumentCaptor<SendMessage> sMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        ArgumentCaptor<InnerMessage> iMessageCaptor = ArgumentCaptor.forClass(InnerMessage.class);

        service.sendFeedbackNotification(dto);

        verify(mockBot, times(1)).send(sMessageCaptor.capture());
        verify(innerMessageService, times(1)).saveMessage(iMessageCaptor.capture());
        assertThat(sMessageCaptor.getValue().getChatId()).isEqualTo(String.valueOf(chatId));
        assertThat(sMessageCaptor.getValue().getText()).isEqualTo(expectedBotMessage);
        assertThat(iMessageCaptor.getValue()).isEqualTo(expectedInnerMessage);
    }

    @Test
    void whenSendFeedbackNotificationAndUserNotExistsThenMessageSaved() {
        FeedbackNotificationDTO dto = new FeedbackNotificationDTO(
                2, "sName", "iName", 3);
        String expectedMessage = "Пользователь "
                + dto.getSenderName()
                + " оставил Вам отзыв о собеседовании на тему "
                + dto.getInterviewName()
                + System.lineSeparator()
                + "Ссылка на собеседование: "
                + "null/interview/"
                + dto.getInterviewId();
        InnerMessage expectedInnerMessage = createInnerMessage(
                dto.getRecipientId(),
                expectedMessage,
                dto.getInterviewId());
        when(userTelegramRepository.findChatIdByUserIdIfNotifiable(any(Integer.class)))
                .thenReturn(Optional.empty());
        ArgumentCaptor<InnerMessage> iMessageCaptor = ArgumentCaptor.forClass(InnerMessage.class);

        service.sendFeedbackNotification(dto);

        verify(mockBot, times(0)).send(any(BotApiMethod.class));
        verify(innerMessageService, times(1)).saveMessage(iMessageCaptor.capture());
        assertThat(iMessageCaptor.getValue()).isEqualTo(expectedInnerMessage);
    }

    @Test
    void whenSendApprovedNotificationAndUserExistsThenMessageSavedAndBotSendMessage() {
        WisherApprovedDTO dto = new WisherApprovedDTO(
                2, 3, 4, "iTitle", "link", "contact");
        long chatId = 1111L;
        String generatedMessage = String.format("Вы приглашены на собеседование: %s.%sСсылка на собеседование: %s",
                dto.getInterviewTitle(),
                System.lineSeparator(),
                dto.getInterviewLink());
        String expectedBotMessage = String.format("Вы приглашены на собеседование \"[%s](%s)\".%sСвяжитесь с автором: %s",
                dto.getInterviewTitle(),
                dto.getInterviewLink(),
                System.lineSeparator(),
                dto.getContactBy());
        InnerMessage expectedInnerMessage = createInnerMessage(
                dto.getWisherUserId(),
                generatedMessage,
                dto.getInterviewId());
        when(userTelegramRepository.findChatIdByUserIdIfNotifiable(any(Integer.class)))
                .thenReturn(Optional.of(chatId));
        when(messagesGenerator.getMessageApprovedWisher(dto)).thenReturn(generatedMessage);
        ArgumentCaptor<SendMessage> sMessageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        ArgumentCaptor<InnerMessage> iMessageCaptor = ArgumentCaptor.forClass(InnerMessage.class);
        when(innerMessageService.saveMessage(iMessageCaptor.capture())).thenReturn(any());

        service.sendApprovedNotification(dto);
        await().until(() -> iMessageCaptor.getValue() != null);

        verify(mockBot, times(1)).send(sMessageCaptor.capture());
        verify(innerMessageService, times(1)).saveMessage(any(InnerMessage.class));
        assertThat(sMessageCaptor.getValue().getChatId()).isEqualTo(String.valueOf(chatId));
        assertThat(sMessageCaptor.getValue().getText()).isEqualTo(expectedBotMessage);
        assertThat(sMessageCaptor.getValue().getParseMode()).isEqualTo("Markdown");
        assertThat(iMessageCaptor.getValue()).isEqualTo(expectedInnerMessage);
    }

    @Test
    void whenSendApprovedNotificationAndUserNotExistsThenMessageSaved() {
        WisherApprovedDTO dto = new WisherApprovedDTO(
                2, 3, 4, "iTitle", "link", "contact");
        String generatedMessage = String.format("Вы приглашены на собеседование: %s.%sСсылка на собеседование: %s",
                dto.getInterviewTitle(),
                System.lineSeparator(),
                dto.getInterviewLink());
        InnerMessage expectedInnerMessage = createInnerMessage(
                dto.getWisherUserId(),
                generatedMessage,
                dto.getInterviewId());
        when(userTelegramRepository.findChatIdByUserIdIfNotifiable(any(Integer.class)))
                .thenReturn(Optional.empty());
        when(messagesGenerator.getMessageApprovedWisher(dto)).thenReturn(generatedMessage);
        ArgumentCaptor<InnerMessage> iMessageCaptor = ArgumentCaptor.forClass(InnerMessage.class);
        when(innerMessageService.saveMessage(iMessageCaptor.capture())).thenReturn(any());

        service.sendApprovedNotification(dto);
        await().until(() -> iMessageCaptor.getValue() != null);

        verify(mockBot, times(0)).send(any(BotApiMethod.class));
        verify(innerMessageService, times(1)).saveMessage(any(InnerMessage.class));
        assertThat(iMessageCaptor.getValue()).isEqualTo(expectedInnerMessage);
    }

    private InnerMessage createInnerMessage(int userId, String message, int interviewId) {
        return InnerMessage.of()
                .userId(userId)
                .text(message)
                .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .read(false)
                .interviewId(interviewId)
                .build();
    }

}