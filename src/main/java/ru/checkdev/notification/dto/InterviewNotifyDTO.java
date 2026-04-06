package ru.checkdev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель DTO описывает Собеседование для сервиса Notification
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 17.11.2023
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "of")
public class InterviewNotifyDTO {
    private int id;
    private int submitterId;
    private String title;
    private int topicId;
    private String topicName;
    private int categoryId;
    private String categoryName;
}