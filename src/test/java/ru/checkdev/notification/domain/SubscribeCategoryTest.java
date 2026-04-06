package ru.checkdev.notification.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscribeCategoryTest {
    private SubscribeCategory subscribeCategory;

    @BeforeEach
    public void setUp() {
        subscribeCategory = new SubscribeCategory(0, 1, 2);
    }

    @Test
    public void testGetId() {
        assertThat(subscribeCategory.getId()).isEqualTo(0);
    }

    @Test
    public void testSetId() {
        subscribeCategory.setId(10);
        assertThat(subscribeCategory.getId()).isEqualTo(10);
    }

    @Test
    public void testGetUserId() {
        assertThat(subscribeCategory.getUserId()).isEqualTo(1);
    }

    @Test
    public void testSetUserId() {
        subscribeCategory.setUserId(11);
        assertThat(subscribeCategory.getUserId()).isEqualTo(11);
    }

    @Test
    public void testGetCategoryId() {
        assertThat(subscribeCategory.getCategoryId()).isEqualTo(2);
    }

    @Test
    public void testSetCategoryId() {
        subscribeCategory.setCategoryId(12);
        assertThat(subscribeCategory.getCategoryId()).isEqualTo(12);
    }

    @Test
    public void whenDefaultConstructorNotNull() {
        SubscribeCategory subscribeCategory = new SubscribeCategory();
        assertThat(subscribeCategory).isNotNull();
    }

    @Test
    public void whenFieldsConstructorNotNull() {
        SubscribeCategory subscribeCategory = new SubscribeCategory(0, 1, 1);
        assertThat(subscribeCategory.getUserId()).isNotZero();
        assertThat(subscribeCategory.getCategoryId()).isNotZero();
    }

    @Test
    public void whenIDSetAndGetEquals() {
        SubscribeCategory subscribeCategory = new SubscribeCategory(0, 1, 1);
        subscribeCategory.setId(1);
        assertThat(subscribeCategory.getId()).isEqualTo(1);
    }
}