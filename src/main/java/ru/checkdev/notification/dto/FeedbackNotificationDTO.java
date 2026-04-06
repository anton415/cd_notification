package ru.checkdev.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackNotificationDTO {

    private int recipientId;
    private String senderName;
    private String interviewName;
    private int interviewId;
}
