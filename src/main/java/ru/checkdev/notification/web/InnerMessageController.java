package ru.checkdev.notification.web;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.CategoryWithTopicDTO;
import ru.checkdev.notification.dto.InnerMessageDTO;
import ru.checkdev.notification.service.InnerMessageService;
import ru.checkdev.notification.service.NotificationMessagesService;
import ru.checkdev.notification.service.SubscribeCategoryService;
import ru.checkdev.notification.service.SubscribeTopicService;

import java.util.List;

@RestController
@RequestMapping("/messages")
@AllArgsConstructor
public class InnerMessageController {

    private final InnerMessageService messageService;
    private final SubscribeCategoryService categoryService;
    private final SubscribeTopicService topicService;
    private final NotificationMessagesService notificationMessagesService;

    @GetMapping("/{id}")
    public ResponseEntity<List<InnerMessage>> findMessage(@PathVariable int id) {
        List<InnerMessage> list = messageService.findByUserIdAndReadFalse(id);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/actual/{userId}")
    public ResponseEntity<List<InnerMessageDTO>> findMessageDTO(@PathVariable int userId) {
        List<InnerMessageDTO> result = messageService.findDTOByUserIdAndReadFalse(userId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Transactional
    @PostMapping("/newInterview")
    public ResponseEntity<Void> createMessage(
            @RequestBody CategoryWithTopicDTO categoryWithTopicDTO) {

        List<Integer> categorySubscribersIds =
                categoryService.findUserIdsByCategoryIdExcludeCurrent(
                        categoryWithTopicDTO.getCategoryId(),
                        categoryWithTopicDTO.getSubmitterId());

        List<Integer> topicSubscribersIds =
                topicService.findUserIdsByTopicIdExcludeCurrent(
                        categoryWithTopicDTO.getTopicId(),
                        categoryWithTopicDTO.getSubmitterId());

        messageService.saveMessagesForSubscribers(
                categoryWithTopicDTO,
                categorySubscribersIds, topicSubscribersIds);

        notificationMessagesService.sendMessagesToCategorySubscribers(
                categorySubscribersIds,
                categoryWithTopicDTO);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/message")
    public ResponseEntity<Void> sendMessage(@RequestBody InnerMessage innerMessage) {
        messageService.send(innerMessage);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable int messageId) {
        messageService.delete(messageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/setRead/{messageId}")
    public ResponseEntity<Void> setRead(@PathVariable int messageId) {
        messageService.setRead(messageId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/setReadAll/{userId}")
    public ResponseEntity<Void> setReadAllMessagesOfUser(@PathVariable int userId) {
        messageService.setReadAll(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
