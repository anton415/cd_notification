package ru.checkdev.notification.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.service.SubscribeTopicService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscribeTopicControllerFakeTest {

    @Test
    public void whenFindTopicByUserId() {
        SubscribeTopic subscribeTopic = new SubscribeTopic(1, 2, 2);
        SubscribeTopicService service = new SubscribeTopicService(new SubscribeTopicRepositoryFake());
        service.save(subscribeTopic);

        ResponseEntity<List<Integer>> resp = new SubscribeTopicController(service)
                .findTopicByUserId(subscribeTopic.getUserId());

        assertThat(resp.getBody()).containsOnly(subscribeTopic.getUserId());
    }
}