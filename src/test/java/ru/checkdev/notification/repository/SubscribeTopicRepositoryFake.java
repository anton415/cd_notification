package ru.checkdev.notification.repository;

import org.springframework.test.fake.CrudRepositoryFake;
import ru.checkdev.notification.domain.SubscribeTopic;

import java.util.List;

/**
 * Переопределенной класс Repository для тестирования
 * SubscribeTopicRepository
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 17.11.2023
 */
public class SubscribeTopicRepositoryFake
        extends CrudRepositoryFake<SubscribeTopic, Integer>
        implements SubscribeTopicRepository {

    @Override
    public List<SubscribeTopic> findByUserId(int id) {
        return memory.values()
                .stream()
                .filter(subscribeTopic -> subscribeTopic.getUserId() == id)
                .toList();
    }

    @Override
    public SubscribeTopic findByUserIdAndTopicId(int userId, int topicId) {
        return memory.values()
                .stream()
                .filter(subscribeTopic -> subscribeTopic.getUserId() == userId)
                .filter(subscribeTopic -> subscribeTopic.getTopicId() == topicId)
                .findFirst().orElseGet(() -> null);
    }

    @Override
    public List<Integer> findUserIdByTopicIdExcludeCurrent(int topicId, int excludedUserId) {
        return memory.values()
                .stream()
                .filter(subscribeTopic -> subscribeTopic.getTopicId() == topicId
                        && subscribeTopic.getUserId() != excludedUserId)
                .map(SubscribeTopic::getUserId)
                .toList();
    }

    @Override
    public List<SubscribeTopic> findAll() {
        return memory.values()
                .stream()
                .toList();
    }
}
