package ru.checkdev.notification.telegram.service;

import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;

public class TgCallNoConnect implements TgCall {
    @Override
    public Mono<Profile> doGet(String url) {
        throw new UnsupportedOperationException("No Connection");
    }

    @Override
    public Mono<Object> doPost(String url, Profile profile) {
        throw new UnsupportedOperationException("No Connection");
    }

    @Override
    public Mono<Object> doPost(String url) {
        throw new UnsupportedOperationException("No Connection");
    }
}
