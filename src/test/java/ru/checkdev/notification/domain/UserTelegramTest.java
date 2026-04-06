package ru.checkdev.notification.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTelegramTest {
    private UserTelegram userTelegram;

    @BeforeEach
    public void setUp() {
        userTelegram = new UserTelegram(1, 10, 555L, false);
    }

    @Test
    void getId() {
        assertThat(userTelegram.getId()).isEqualTo(1);
    }

    @Test
    void setId() {
        userTelegram.setId(2);
        assertThat(userTelegram.getId()).isEqualTo(2);
    }

    @Test
    void getChatId() {
        assertThat(userTelegram.getChatId()).isEqualTo(555L);
    }

    @Test
    void setChatId() {
        userTelegram.setChatId(333L);
        assertThat(userTelegram.getChatId()).isEqualTo(333L);
    }

    @Test
    void isNotifiable() {
        assertThat(userTelegram.isNotifiable()).isFalse();
    }

    @Test
    void setNotifiable() {
        userTelegram.setNotifiable(true);
        assertThat(userTelegram.isNotifiable()).isTrue();
    }

}