package ru.checkdev.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscribeTopicServiceFakeTest {

    private SubscribeTopicService service;

    @BeforeEach
    void init() {
        service = new SubscribeTopicService(new SubscribeTopicRepositoryFake());
    }

    @Test
    public void whenGetAllSubTopicReturnContainsValue() {
        SubscribeTopic subscribeTopic = service.save(new SubscribeTopic(0, 1, 1));
        List<SubscribeTopic> result = service.findAll();
        assertThat(result).contains(subscribeTopic);
    }

    @Test
    public void requestByUserIdReturnCorrectValue() {
        SubscribeTopic subscribeTopic = service.save(new SubscribeTopic(1, 2, 2));
        List<Integer> result = service.findTopicByUserId(subscribeTopic.getUserId());
        assertThat(result).contains(2);
    }

    @Test
    public void whenDeleteTopicCatItIsNotExist() {
        SubscribeTopic subscribeTopic = service.save(new SubscribeTopic(2, 3, 3));
        subscribeTopic = service.delete(subscribeTopic);
        List<SubscribeTopic> result = service.findAll();
        assertThat(result).doesNotContain(subscribeTopic);
    }

    @Test
    public void whenFindUserIdsByTopicId() {
        service.save(new SubscribeTopic(1, 1, 1));
        service.save(new SubscribeTopic(2, 2, 1));
        List<Integer> result = service.findUserIdsByTopicIdExcludeCurrent(1, 3);
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void whenFindUserIdsByTopicIdExcludeFirst() {
        service.save(new SubscribeTopic(1, 1, 1));
        service.save(new SubscribeTopic(2, 2, 1));
        List<Integer> result = service.findUserIdsByTopicIdExcludeCurrent(1, 1);
        assertThat(result.size()).isEqualTo(1);
    }
}