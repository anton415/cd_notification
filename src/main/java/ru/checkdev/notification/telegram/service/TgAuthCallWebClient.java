package ru.checkdev.notification.telegram.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.checkdev.notification.domain.Profile;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Класс реализует методы get и post для отправки сообщений через WebClient
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@org.springframework.context.annotation.Profile("default")
@Service
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TgAuthCallWebClient implements TgCall {
    @Value("${server.auth}")
    private String urlServiceAuth;
    @Value("${tg.auth.retry.attempts:3}")
    private int retryAttempts;
    @Value("${tg.auth.retry.delay-ms:1000}")
    private long retryDelayMs;

    /**
     * Метод get
     *
     * @param url URL http
     * @return Mono<Person>
     */
    @Override
    public Mono<Profile> doGet(String url) {
        return withRetry("GET", url, () -> WebClient.create(urlServiceAuth)
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Profile.class));
    }

    /**
     * Метод POST
     *
     * @param url     URL http
     * @param profile Body PersonDTO.class
     * @return Mono<Person>
     */
    @Override
    public Mono<Object> doPost(String url, Profile profile) {
        return withRetry("POST", url, () -> WebClient.create(urlServiceAuth)
                .post()
                .uri(url)
                .bodyValue(profile)
                .retrieve()
                .bodyToMono(Object.class));
    }

    @Override
    public Mono<Object> doPost(String url) {
        return withRetry("POST", url, () -> WebClient.create(urlServiceAuth)
                .post()
                .uri(url)
                .retrieve()
                .bodyToMono(Object.class));
    }

    /**
     * Метод с ретраймами
     *
     * @param method  Метод HTTP
     * @param url     URL http
     * @param request Запрос
     * @return Mono<T>
     */
    private <T> Mono<T> withRetry(String method, String url, Supplier<Mono<T>> request) {
        long attempts = Math.max(retryAttempts, 1);
        long delay = Math.max(retryDelayMs, 0L);
        return Mono.defer(request)
                .retryWhen(Retry.fixedDelay(attempts - 1, Duration.ofMillis(delay))
                        .filter(this::isRetryable)
                        .doBeforeRetry(signal -> log.warn(
                                "Retry auth {} {} attempt {} failed: {}",
                                method,
                                url,
                                signal.totalRetries() + 1,
                                signal.failure().getMessage()
                        ))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                .doOnError(err -> log.error(
                        "Auth {} {} failed: {}",
                        method,
                        url,
                        err.getMessage()
                ));
    }

    /**
     * Метод определяет, является ли ошибка ретраибельной
     * @param throwable
     * @return
     */
    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientRequestException) {
            return true;
        }
        return throwable instanceof WebClientResponseException responseException
                && responseException.getStatusCode().is5xxServerError();
    }
}
