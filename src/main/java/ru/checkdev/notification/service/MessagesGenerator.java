package ru.checkdev.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.checkdev.notification.dto.*;

import java.util.StringJoiner;

/**
 * CheckDev пробное собеседование
 * Класс предназначен для генерации сообщения для рассылки.
 *
 * @author Dmitry Stepanov
 * @version 17.11.2023 23:12
 */

@Component
@RequiredArgsConstructor
public class MessagesGenerator {

    /**
     * Генерация сообщения для отправки при подписке на тему.
     *
     * @param interviewNotifyDTO InterviewNotifDTO
     * @return String
     */

    private final EurekaUriProvider uriProvider;
    private static final String SERVICE_ID = "site";

    public String getMessageSubscribeTopic(InterviewNotifyDTO interviewNotifyDTO) {
        return String.format(
                "Вы подписаны на тему:%s, из категории:%s.%s"
                        + "По вашей подписке создана новое собеседование.",
                interviewNotifyDTO.getTopicName(), interviewNotifyDTO.getCategoryName(),
                System.lineSeparator());
    }

    /**
     * Генерация сообщения для отправки при добавлении участника к собеседованию.
     *
     * @param wisherNotifyDTO WisherNotifyDTO
     * @return String message.
     */
    public String getMessageParticipateWisher(WisherNotifyDTO wisherNotifyDTO) {
        return "На ваше собеседование: "
                + wisherNotifyDTO.getInterviewTitle()
                + " добавился участник: "
                + wisherNotifyDTO.getUserName()
                + System.lineSeparator()
                + "Ссылка на собеседование: "
                + uriProvider.getUri(SERVICE_ID)
                + "/interview/"
                + wisherNotifyDTO.getInterviewId();
    }

    /**
     * Генерация сообщения для отправки участнику при отмене собеседования его автором.
     *
     * @param cancelInterviewDTO CancelInterviewNotificationDTO
     * @return String message.
     */
    public String getMessageCancelInterview(CancelInterviewNotificationDTO cancelInterviewDTO) {
        StringJoiner joiner = new StringJoiner("");
        return joiner.add("Собеседование ")
                .add(cancelInterviewDTO.getInterviewTitle())
                .add(", на которое вы откликнулись, было отменено создателем ")
                .add(cancelInterviewDTO.getSubmitterName())
                .add(". Причина отмены собеседования: \"")
                .add(cancelInterviewDTO.getReasonOfCancel())
                .add("\". Данное собеседование вам больше недоступно.").toString();
    }

    /**
     * Генерация сообщения для отправки участнику при выборе автором собеседования другого собеседника.
     *
     * @param wisherDismissedDTO WisherDismissedDTO
     * @return String message.
     */
    public String getMessageDismissedWisher(WisherDismissedDTO wisherDismissedDTO) {
        StringJoiner joiner = new StringJoiner("");
        return joiner.add("Пользователь ")
                .add(wisherDismissedDTO.getSubmitterName())
                .add(" одобрил на собеседование ")
                .add(wisherDismissedDTO.getInterviewTitle())
                .add(" другого собеседника. Данное собеседование вам больше недоступно.").toString();
    }

    public String getMessageApprovedWisher(WisherApprovedDTO wisherApprovedNotifyDTO) {
        return String.format("Вы приглашены на собеседование: %s.%sСсылка на собеседование: %s",
                wisherApprovedNotifyDTO.getInterviewTitle(),
                System.lineSeparator(),
                wisherApprovedNotifyDTO.getInterviewLink()
        );
    }
}
