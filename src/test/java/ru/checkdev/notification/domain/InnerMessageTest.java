package ru.checkdev.notification.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class InnerMessageTest {
    private InnerMessage botMessage;

    @BeforeEach
    public void setUp() {
        botMessage = new InnerMessage(0, 1, "text",
                new Timestamp(System.currentTimeMillis()), false);
    }

    @Test
    void getId() {
        assertThat(botMessage.getId()).isEqualTo(0);
    }

    @Test
    void setId() {
        botMessage.setId(11);
        assertThat(botMessage.getId()).isEqualTo(11);
    }

    @Test
    void getText() {
        assertThat(botMessage.getText()).isEqualTo("text");
    }

    @Test
    void setText() {
        botMessage.setText("other text");
        assertThat(botMessage.getText()).isEqualTo("other text");
    }

    @Test
    void isRead() {
        assertThat(botMessage.isRead()).isEqualTo(false);
    }

    @Test
    void setRead() {
        botMessage.setRead(true);
        assertThat(botMessage.isRead()).isEqualTo(true);
    }

    @Test
    public void whenDefaultConstructorNotNull() {
        InnerMessage botMessage1 = new InnerMessage();
        assertThat(botMessage1).isNotNull();
    }

    @Test
    public void whenFieldsConstructorNotNull() {
        assertThat(botMessage.getUserId()).isNotZero();
        assertThat(botMessage.getText()).isNotNull();
        assertThat(botMessage.getCreated()).isNotNull();
    }

    @Test
    public void whenIDSetAndGetEquals() {
        botMessage.setId(1);
        assertThat(botMessage.getId()).isEqualTo(1);
    }
}