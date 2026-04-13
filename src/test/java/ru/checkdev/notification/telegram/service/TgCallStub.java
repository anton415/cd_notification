package ru.checkdev.notification.telegram.service;

import reactor.core.publisher.Mono;
import ru.checkdev.notification.domain.Profile;

import java.util.function.BiFunction;
import java.util.function.Function;

public class TgCallStub implements TgCall {
    private Function<String, Profile> getHandler = url -> null;
    private BiFunction<String, Profile, Object> postHandler = (url, profile) -> null;
    private Function<String, Object> postWithoutBodyHandler = url -> null;

    public TgCallStub withGetHandler(Function<String, Profile> getHandler) {
        this.getHandler = getHandler;
        return this;
    }

    public TgCallStub withPostHandler(BiFunction<String, Profile, Object> postHandler) {
        this.postHandler = postHandler;
        return this;
    }

    public TgCallStub withPostWithoutBodyHandler(Function<String, Object> postWithoutBodyHandler) {
        this.postWithoutBodyHandler = postWithoutBodyHandler;
        return this;
    }

    @Override
    public Mono<Profile> doGet(String url) {
        return Mono.justOrEmpty(getHandler.apply(url));
    }

    @Override
    public Mono<Object> doPost(String url, Profile profile) {
        return Mono.justOrEmpty(postHandler.apply(url, profile));
    }

    @Override
    public Mono<Object> doPost(String url) {
        return Mono.justOrEmpty(postWithoutBodyHandler.apply(url));
    }
}
