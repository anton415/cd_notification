package ru.checkdev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CheckDev пробное собеседование
 * * DTO модель для взаимодействия с сервисом site. Сообщает ID и Название Интервью,
 * * ID и Имя создателя интервью, а так же ID участника, кому придет это уведомление.
 *
 * @author Rustam Saidov
 * @version 11.03.2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "of")
public class WisherDismissedDTO {
    private int interviewId;
    private String interviewTitle;
    private int submitterId;
    private String submitterName;
    private int userId;
}