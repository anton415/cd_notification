package ru.checkdev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CheckDev пробное собеседование
 * DTO модель для взаимодействия с сервисом notification. Сообщает ID и Название Интервью, которое отменяется,
 * ID и Имя создателя интервью, причину отмены, а так же ID участника, кому придет это уведомление.
 *
 * @author Rustam Saidov
 * @version 12.02.2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "of")
public class CancelInterviewNotificationDTO {
    private int interviewId;
    private String interviewTitle;
    private int submitterId;
    private String submitterName;
    private String reasonOfCancel;
    private int userId;
}
