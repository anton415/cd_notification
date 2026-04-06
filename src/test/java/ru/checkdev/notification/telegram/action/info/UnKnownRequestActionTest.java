package ru.checkdev.notification.telegram.action.info;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.assertj.core.api.Assertions.assertThat;

class UnKnownRequestActionTest {

    @Test
    void handle() {
        Chat chat = new Chat(1L, "type");
        Update update = new Update();
        Message message = new Message();
        update.setMessage(message);
        message.setChat(chat);
        UnKnownRequestAction unKnownRequestAction = new UnKnownRequestAction();
        unKnownRequestAction.handle(update);
        String expect = String.format(
                "Команда не поддерживается! Список доступных команд: %s/start",
                System.lineSeparator());

        BotApiMethod botApiMethod = unKnownRequestAction.handle(update).get();
        SendMessage sendMessage = (SendMessage) botApiMethod;
        String actual = sendMessage.getText();

        assertThat(actual).isEqualTo(expect);
    }
}