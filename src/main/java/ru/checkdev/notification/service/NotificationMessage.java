package ru.checkdev.notification.service;


import java.util.List;

/**
 * Интерфейс описывает поведение отправки сообщений
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 17.11.2023
 */
public interface NotificationMessage<T, V, R> {
    List<R> sendMessage(List<T> targets, V message);

    R sendMessage(T target, V message);
}
