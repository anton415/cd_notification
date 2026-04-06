package ru.checkdev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CheckDev пробное собеседование
 * Wisher DTO модель для взаимодействия с сервисом notification
 *
 * @author Dmitry Stepanov
 * @version 19.11.2023 01:08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "of")
public class WisherNotifyDTO {
    private int interviewId;
    private String interviewTitle;
    private int submitterId;
    private int userId;
    private String userName;
    private String contactBy;
}

