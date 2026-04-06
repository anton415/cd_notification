package ru.checkdev.notification.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.SubscribeCategory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscribeCategoryRepositoryTest {

    private static final SubscribeCategory SUBSCRIBE_CATEGORY = new SubscribeCategory(0, 1, 1);

    private SubscribeCategoryRepository repository;

    @BeforeEach
    void init() {
        repository = new SubscribeCategoryRepositoryFake();
        repository.save(SUBSCRIBE_CATEGORY);
    }

    @Test
    void whenFindSubscribeCategoryByUserId() {
        List<SubscribeCategory> result = repository.findByUserId(1);
        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(SUBSCRIBE_CATEGORY);
    }

    @Test
    void whenSubscribeCategoryNotFoundByUserId() {
        assertThat(repository.findByUserId(2)).isEmpty();
    }

    @Test
    void whenFindByUserIdAndCategoryId() {
        SubscribeCategory result = repository.findByUserIdAndCategoryId(1, 1);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(SUBSCRIBE_CATEGORY);
    }

    @Test
    void whenNotFoundByUserIdAndCategoryId() {
        SubscribeCategory result = repository.findByUserIdAndCategoryId(2, 2);
        assertThat(result).isNull();
    }
}
