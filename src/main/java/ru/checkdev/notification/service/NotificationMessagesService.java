package ru.checkdev.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NotificationMessagesService {

    private final UserTelegramRepository userTelegramRepository;
    private final InnerMessageService innerMessageService;
    private final Bot bot;
    private final MessagesGenerator messagesGenerator;
    private final EurekaUriProvider uriProvider;
    private static final String SERVICE_ID = "site";

    /**
     * Метод находит chatId всех пользователей, подписанных на категорию и согласившихся на получение оповещений в телеграмм,
     * и передаёт каждый в метод sendNotificationToCategorySubscriber для рассылки оповещений.
     *
     * @param categorySubscribersIds список id подписчиков на категорию.
     * @param categoryWithTopicDTO DTO категории и темы собеседования для оповещения.
     */
    public void sendMessagesToCategorySubscribers(List<Integer> categorySubscribersIds,
                                                  CategoryWithTopicDTO categoryWithTopicDTO) {
        userTelegramRepository.findChatIdInUserIdsIfNotifiable(categorySubscribersIds)
                .forEach(chatId ->
                        sendNotificationToCategorySubscriber(chatId,
                                categoryWithTopicDTO));
    }

    /**
     * Метод формирует сообщение об обновлении в категории и отправляет пользователю телеграмм по указанному chatId.
     *
     * @param chatId id чата пользователя.
     * @param categoryWithTopicDTO DTO с данными для формирования сообщения.
     */
    public void sendNotificationToCategorySubscriber(long chatId, CategoryWithTopicDTO categoryWithTopicDTO) {
        bot.send(new SendMessage(
                        String.valueOf(chatId),
                        "В категории "
                                + categoryWithTopicDTO.getCategoryName()
                                + " появилось новое собеседование."
                                + System.lineSeparator()
                                + "Ссылка на собеседование: "
                                + uriProvider.getUri(SERVICE_ID)
                                + "/interview/" + categoryWithTopicDTO.getInterviewId()
                )
        );
    }

    /**
     * Метод формирует сообщение об отзыве и отправляет пользователю, которому оставлен отзыв.
     *
     * @param feedbackNotification данные для формирования отзыва конкретному пользователю.
     */
    public void sendFeedbackNotification(FeedbackNotificationDTO feedbackNotification) {
        var optionalChatId = userTelegramRepository
                .findChatIdByUserIdIfNotifiable(feedbackNotification.getRecipientId());
        var message = "Пользователь "
                + feedbackNotification.getSenderName()
                + " оставил Вам отзыв о собеседовании на тему "
                + feedbackNotification.getInterviewName()
                + System.lineSeparator()
                + "Ссылка на собеседование: "
                + uriProvider.getUri(SERVICE_ID)
                + "/interview/" + feedbackNotification.getInterviewId();
        optionalChatId.ifPresent(aLong -> bot.send(new SendMessage(String.valueOf(aLong), message)));
        InnerMessage innerMessage = InnerMessage.of()
                .userId(feedbackNotification.getRecipientId())
                .text(message)
                .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .read(false)
                .interviewId(feedbackNotification.getInterviewId())
                .build();
        innerMessageService.saveMessage(innerMessage);
    }

    /**
     * Метод формирует сообщение о приглашении на собеседование и отправляет пользователю.
     *
      * @param wisherApprovedDTO данные для формирования и отправки приглашения на собеседование.
     */
    public void sendApprovedNotification(WisherApprovedDTO wisherApprovedDTO) {
        var optionalChatId = userTelegramRepository
                .findChatIdByUserIdIfNotifiable(wisherApprovedDTO.getWisherUserId());
        var message = String.format("Вы приглашены на собеседование \"[%s](%s)\".%sСвяжитесь с автором: %s",
                wisherApprovedDTO.getInterviewTitle(),
                wisherApprovedDTO.getInterviewLink(),
                System.lineSeparator(),
                wisherApprovedDTO.getContactBy());
        InnerMessage innerMessage = InnerMessage.of()
                .userId(wisherApprovedDTO.getWisherUserId())
                .text(messagesGenerator.getMessageApprovedWisher(wisherApprovedDTO))
                .created(Timestamp.valueOf(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)))
                .read(false)
                .interviewId(wisherApprovedDTO.getInterviewId())
                .build();
        CompletableFuture.supplyAsync(() -> innerMessageService.saveMessage(innerMessage));
        if (optionalChatId.isPresent()) {
            var chatId = optionalChatId.get();
            var sendNotification = new SendMessage(String.valueOf(chatId), message);
            sendNotification.setParseMode("Markdown");
            bot.send(sendNotification);
        }
    }
}
