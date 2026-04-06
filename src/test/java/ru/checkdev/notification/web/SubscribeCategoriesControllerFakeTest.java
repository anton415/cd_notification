package ru.checkdev.notification.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.checkdev.notification.domain.SubscribeCategory;
import ru.checkdev.notification.repository.SubscribeCategoryRepositoryFake;
import ru.checkdev.notification.service.SubscribeCategoryService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscribeCategoriesControllerFakeTest {

    @Test
    public void whenFindCategoriesByUserId() {
        SubscribeCategory subscribeCategory = new SubscribeCategory(1, 2, 2);
        SubscribeCategoryService categoryService = new SubscribeCategoryService(
                new SubscribeCategoryRepositoryFake());
        categoryService.save(subscribeCategory);
        SubscribeCategoriesController controller = new SubscribeCategoriesController(categoryService);

        ResponseEntity<List<Integer>> resp = controller.findCategoriesByUserId(subscribeCategory.getUserId());

        assertThat(resp.getBody()).containsOnly(subscribeCategory.getCategoryId());
    }
}