package ru.checkdev.notification.telegram;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Интерфейс описывает поведение для обмена сообщениями с ботом.
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 04.12.2023
 */
public interface Bot {
    String getBotUsername();

    String getBotToken();

    void onUpdateReceived(Update update);

    void send(BotApiMethod msg);
}
