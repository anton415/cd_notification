package ru.checkdev.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.SubscribeCategory;
import ru.checkdev.notification.repository.SubscribeCategoryRepositoryFake;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscribeCategoryServiceFakeTest {

    private SubscribeCategoryService service;

    @BeforeEach
    void init() {
        service = new SubscribeCategoryService(new SubscribeCategoryRepositoryFake());
    }

    @Test
    public void whenGetAllSubCatReturnContainsValue() {
        SubscribeCategory subscribeCategory1 = service.save(new SubscribeCategory(2, 1, 1));
        SubscribeCategory subscribeCategory2 = service.save(new SubscribeCategory(1, 2, 2));
        assertThat(service.findAll()).contains(subscribeCategory1, subscribeCategory2);
    }

    @Test
    public void whenDeleteSubCatItIsNotExist() {
        SubscribeCategory subscribeCategory = service.save(new SubscribeCategory(2, 3, 3));
        service.delete(subscribeCategory);
        assertThat(service.findAll()).doesNotContain(subscribeCategory);
    }

    @Test
    public void requestByUserIdReturnCorrectValue() {
        SubscribeCategory subscribeCategory = service.save(new SubscribeCategory(1, 2, 2));
        List<Integer> actual = service.findCategoriesByUserId(subscribeCategory.getUserId());
        assertThat(actual).isEqualTo(List.of(2));
    }

    @Test
    public void whenFindUserIdsByCategoryId() {
        service.save(new SubscribeCategory(1, 1, 4));
        service.save(new SubscribeCategory(2, 2, 4));
        List<Integer> actual = service.findUserIdsByCategoryIdExcludeCurrent(4, 3);
        assertThat(actual.size()).isEqualTo(2);
    }

    @Test
    public void whenFindUserIdsByCategoryIdExcludeFirst() {
        service.save(new SubscribeCategory(1, 1, 4));
        service.save(new SubscribeCategory(2, 2, 4));
        List<Integer> actual = service.findUserIdsByCategoryIdExcludeCurrent(4, 1);
        assertThat(actual.size()).isEqualTo(1);
    }
}