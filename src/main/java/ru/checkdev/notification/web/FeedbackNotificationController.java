package ru.checkdev.notification.web;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.checkdev.notification.dto.FeedbackNotificationDTO;
import ru.checkdev.notification.service.NotificationMessagesService;

@RestController
@RequestMapping("/feedback")
@AllArgsConstructor
public class FeedbackNotificationController {

    private final NotificationMessagesService notificationMessagesService;

    @PostMapping("/interview")
    public void sendFeedbackNotification(@RequestBody FeedbackNotificationDTO feedbackNotification) {
        notificationMessagesService.sendFeedbackNotification(feedbackNotification);
    }
}
