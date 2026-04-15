package ru.checkdev.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.CancelInterviewNotificationDTO;
import ru.checkdev.notification.dto.CategoryWithTopicDTO;
import ru.checkdev.notification.dto.FeedbackNotificationDTO;
import ru.checkdev.notification.dto.InterviewNotifyDTO;
import ru.checkdev.notification.dto.WisherApprovedDTO;
import ru.checkdev.notification.dto.WisherDismissedDTO;
import ru.checkdev.notification.dto.WisherNotifyDTO;
import ru.checkdev.notification.web.FeedbackNotificationController;
import ru.checkdev.notification.web.InnerMessageController;
import ru.checkdev.notification.web.NotificationInterviewController;
import ru.checkdev.notification.web.NotificationWisherController;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Компонент для потребления событий из Kafka и обработки их с помощью соответствующих контроллеров.
 */
@Component
public class NotificationEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private static final String TOPIC_NEW_INTERVIEW = "notification.new-interview";
    private static final String TOPIC_INNER_MESSAGE = "notification.inner-message";
    private static final String TOPIC_FEEDBACK = "notification.feedback";
    private static final String TOPIC_SUBSCRIBE_TOPIC = "notification.subscribe-topic";
    private static final String TOPIC_PARTICIPATE = "notification.participate";
    private static final String TOPIC_CANCEL_INTERVIEW = "notification.cancel-interview";
    private static final String TOPIC_PARTICIPANT_DISMISSED = "notification.participant-dismissed";
    private static final String TOPIC_APPROVED_WISHER = "notification.approved-wisher";

    private final ObjectMapper objectMapper;
    private final InnerMessageController innerMessageController;
    private final FeedbackNotificationController feedbackNotificationController;
    private final NotificationInterviewController notificationInterviewController;
    private final NotificationWisherController notificationWisherController;

    public NotificationEventConsumer(ObjectMapper objectMapper,
                                     InnerMessageController innerMessageController,
                                     FeedbackNotificationController feedbackNotificationController,
                                     NotificationInterviewController notificationInterviewController,
                                     NotificationWisherController notificationWisherController) {
        this.objectMapper = objectMapper;
        this.innerMessageController = innerMessageController;
        this.feedbackNotificationController = feedbackNotificationController;
        this.notificationInterviewController = notificationInterviewController;
        this.notificationWisherController = notificationWisherController;
    }

    @KafkaListener(topics = TOPIC_NEW_INTERVIEW)
    public void consumeNewInterview(String message) {
        consume(message, CategoryWithTopicDTO.class, innerMessageController::createMessage, TOPIC_NEW_INTERVIEW);
    }

    @KafkaListener(topics = TOPIC_INNER_MESSAGE)
    public void consumeInnerMessage(String message) {
        consume(message, InnerMessage.class, innerMessageController::sendMessage, TOPIC_INNER_MESSAGE);
    }

    @KafkaListener(topics = TOPIC_FEEDBACK)
    public void consumeFeedbackNotification(String message) {
        consume(message, FeedbackNotificationDTO.class,
                feedbackNotificationController::sendFeedbackNotification, TOPIC_FEEDBACK);
    }

    @KafkaListener(topics = TOPIC_SUBSCRIBE_TOPIC)
    public void consumeSubscribeTopicNotification(String message) {
        consume(message, InterviewNotifyDTO.class,
                notificationInterviewController::sendMessageSubscribeTopic, TOPIC_SUBSCRIBE_TOPIC);
    }

    @KafkaListener(topics = TOPIC_PARTICIPATE)
    public void consumeParticipateNotification(String message) {
        consume(message, WisherNotifyDTO.class,
                notificationInterviewController::sendMessageSubmitterInterview, TOPIC_PARTICIPATE);
    }

    @KafkaListener(topics = TOPIC_CANCEL_INTERVIEW)
    public void consumeCancelInterviewNotification(String message) {
        consume(message, CancelInterviewNotificationDTO.class,
                notificationInterviewController::sendMessageCancelInterview, TOPIC_CANCEL_INTERVIEW);
    }

    @KafkaListener(topics = TOPIC_PARTICIPANT_DISMISSED)
    public void consumeParticipantDismissedNotification(String message) {
        try {
            WisherDismissedDTO[] payload = objectMapper.readValue(message, WisherDismissedDTO[].class);
            notificationInterviewController.sendMessageCancelInterview(Arrays.asList(payload));
        } catch (Exception e) {
            LOG.error("Kafka message processing failed for topic {}", TOPIC_PARTICIPANT_DISMISSED, e);
        }
    }

    @KafkaListener(topics = TOPIC_APPROVED_WISHER)
    public void consumeApprovedWisherNotification(String message) {
        consume(message, WisherApprovedDTO.class,
                notificationWisherController::sendMessageApprovedWisher, TOPIC_APPROVED_WISHER);
    }

    private <T> void consume(String message,
                             Class<T> targetClass,
                             Consumer<T> consumer,
                             String topic) {
        try {
            consumer.accept(objectMapper.readValue(message, targetClass));
        } catch (Exception e) {
            LOG.error("Kafka message processing failed for topic {}", topic, e);
        }
    }
}
