package ru.checkdev.notification.telegram.service;

import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;

/**
 * Интерфейс описывает поведение боты отправки сообщений;
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 08.11.2023
 */
public interface TgCall {
    Mono<Profile> doGet(String url);

    Mono<Object> doPost(String url, Profile profile);

    public Mono<Object> doPost(String url);
}
