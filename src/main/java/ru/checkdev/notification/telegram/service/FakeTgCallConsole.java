package ru.checkdev.notification.telegram.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.dto.ProfileTgDTO;
import ru.checkdev.notification.service.EurekaUriProvider;

import java.util.Calendar;

/**
 * Отправка ботом сообщений в консоль.
 * Для профиля develop
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 08.11.2023
 */
@org.springframework.context.annotation.Profile("develop")
@Service
@Slf4j
@RequiredArgsConstructor
public class FakeTgCallConsole implements TgCall {
    private static final String URL_PROFILE_BY_EMAIL = "/profiles/tg/byEmail";
    private static final String URL_PROFILE_BY_EMAIL_AND_PASSWORD = "/profiles/tg/byEmailAndPassword";
    private static final String OCCUPIED_MAIL = "occupied@email.ru";
    /**
     * Поле заведено для отладки тестов
     * При указании данного email пользователя сервис бросает exception
     */
    private static final String ERROR_MAIL = "error@exception.er";
    /**
     * Поле заведено для отладки тестов
     * При указании данного ERROR_ID в качестве userId в моделях данных сервис бросает exception.
     */
    private static final String ERROR_ID = "-23";

    private final EurekaUriProvider uriProvider;
    private static final String SERVICE_ID = "auth";

    @Override
    public Mono<Profile> doGet(String url) {
        if (url.endsWith(ERROR_ID)) {
            throw new IllegalArgumentException("Service is error");
        }
        Profile profile = new Profile(0, "FakeName", "FakeEmail",
                "FakePassword", true, Calendar.getInstance());
        log.info("Fake TgCall doGet method. Request URL: {}{}, Response model: {}", uriProvider.getUri(SERVICE_ID), url, profile);
        return Mono.just(profile);
    }

    @Override
    public Mono<Object> doPost(String url, Profile profile) {
        if (ERROR_MAIL.equals(profile.getEmail())) {
            throw new IllegalArgumentException("Service is error");
        }
        if (URL_PROFILE_BY_EMAIL.equals(url)) {
            if (OCCUPIED_MAIL.equals(profile.getEmail())) {
                ProfileTgDTO profileTgDTO = new ProfileTgDTO(-23, "FakeName", profile.getEmail());
                log.info("Fake TgCall doPost method. Request URL: {}{}, model: {}", uriProvider.getUri(SERVICE_ID), url, profile);
                return Mono.just(profileTgDTO);
            }
            return Mono.empty();
        }
        if (URL_PROFILE_BY_EMAIL_AND_PASSWORD.equals(url)) {
            ProfileTgDTO profileTgDTO = new ProfileTgDTO(-23, "FakeName", profile.getEmail());
            log.info("Fake TgCall doPost method. Request URL: {}{}, model: {}", uriProvider.getUri(SERVICE_ID), url, profile);
            return Mono.just(profileTgDTO);
        }
        ProfileTgDTO profileTgDTO = new ProfileTgDTO(-23, profile.getUsername(), profile.getEmail());
        log.info("Fake TgCall doPost method. Request URL: {}{}, model: {}", uriProvider.getUri(SERVICE_ID), url, profile);
        return Mono.just(profileTgDTO);
    }

    @Override
    public Mono<Object> doPost(String url) {
        if (url.endsWith(ERROR_ID)) {
            throw new IllegalArgumentException("Service is error");
        }
        ProfileTgDTO profileTgDTO = new ProfileTgDTO(-23, "FakeName", "fake@mail.ru");
        log.info("Fake TgCall doPost method. Request URL: {}{}, model: {}", uriProvider.getUri(SERVICE_ID), url);
        return Mono.just(profileTgDTO);
    }
}
