package ru.checkdev.notification.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.SubscribeTopic;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscribeTopicRepositoryTest {

    private static final SubscribeTopic SUBSCRIBE_TOPIC = new SubscribeTopic(0, 1, 1);

    private SubscribeTopicRepository repository;

    @BeforeEach
    void init() {
        repository = new SubscribeTopicRepositoryFake();
        repository.save(SUBSCRIBE_TOPIC);
    }

    @Test
    void whenFindSubscribeCategoryByUserId() {
        List<SubscribeTopic> result = repository.findByUserId(1);
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(SUBSCRIBE_TOPIC);
    }

    @Test
    void whenSubscribeCategoryNotFoundByUserId() {
        assertThat(repository.findByUserId(2)).isEmpty();
    }

    @Test
    void whenFindByUserIdAndCategoryId() {
        SubscribeTopic result = repository.findByUserIdAndTopicId(1, 1);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(SUBSCRIBE_TOPIC);
    }

    @Test
    void whenNotFoundByUserIdAndCategoryId() {
        SubscribeTopic result = repository.findByUserIdAndTopicId(2, 2);
        assertThat(result).isNull();
    }
}
