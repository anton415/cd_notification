package ru.checkdev.notification.telegram.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
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
public class TgAuthCallWebClient implements TgCall {
    private static final Logger LOG = LoggerFactory.getLogger(TgAuthCallWebClient.class);
    private static final String AUTH_RETRY = "authRetry";
    private static final String AUTH_CIRCUIT_BREAKER = "authCircuitBreaker";
    private static final int DEFAULT_CIRCUIT_BREAKER_FAILURE_THRESHOLD = 2;
    private static final Duration DEFAULT_CIRCUIT_BREAKER_OPEN_STATE = Duration.ofMinutes(1);

    private final WebClient webClient;
    private final Retry retry;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker;

    public TgAuthCallWebClient(
            @Value("${server.auth}") String urlServiceAuth,
            @Value("${resilience4j.retry.instances.authRetry.maxAttempts:3}") int retryAttempts,
            @Value("${resilience4j.retry.instances.authRetry.waitDuration:500ms}")
            Duration retryWaitDuration,
            @Value(
                    "${resilience4j.circuitbreaker.instances.authCircuitBreaker."
                            + "failureRateThreshold:50}"
            ) float failureRateThreshold,
            @Value(
                    "${resilience4j.circuitbreaker.instances.authCircuitBreaker."
                            + "waitDurationInOpenState:10000ms}"
            ) Duration waitDurationInOpenState,
            @Value(
                    "${resilience4j.circuitbreaker.instances.authCircuitBreaker."
                            + "slidingWindowSize:100}"
            ) int slidingWindowSize,
            @Value(
                    "${resilience4j.circuitbreaker.instances.authCircuitBreaker."
                            + "permittedNumberOfCallsInHalfOpenState:10}"
            ) int permittedNumberOfCallsInHalfOpenState
    ) {
        this(
                WebClient.create(urlServiceAuth),
                buildRetry(retryAttempts, retryWaitDuration),
                buildCircuitBreaker(
                        failureRateThreshold,
                        waitDurationInOpenState,
                        slidingWindowSize,
                        permittedNumberOfCallsInHalfOpenState
                )
        );
    }

    public TgAuthCallWebClient(String urlServiceAuth, int retryAttempts, long retryDelayMs) {
        this(
                urlServiceAuth,
                retryAttempts,
                retryDelayMs,
                DEFAULT_CIRCUIT_BREAKER_FAILURE_THRESHOLD
        );
    }

    public TgAuthCallWebClient(String urlServiceAuth, int retryAttempts, long retryDelayMs, int circuitBreakerFailureThreshold) {
        this(
                WebClient.create(urlServiceAuth),
                buildRetry(retryAttempts, Duration.ofMillis(Math.max(retryDelayMs, 0L))),
                buildTestCircuitBreaker(circuitBreakerFailureThreshold)
        );
    }

    private TgAuthCallWebClient( WebClient webClient, Retry retry, CircuitBreaker circuitBreaker) {
        this.webClient = webClient;
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
    }

    /**
     * Метод get
     *
     * @param url URL http
     * @return Mono<Person>
     */
    @Override
    public Mono<Profile> doGet(String url) {
        return execute("GET", url, () -> webClient
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
        return execute("POST", url, () -> webClient
                .post()
                .uri(url)
                .bodyValue(profile)
                .retrieve()
                .bodyToMono(Object.class));
    }

    @Override
    public Mono<Object> doPost(String url) {
        return execute("POST", url, () -> webClient
                .post()
                .uri(url)
                .retrieve()
                .bodyToMono(Object.class));
    }

    private <T> Mono<T> execute(String method, String url, Supplier<Mono<T>> request) {
        return Mono.defer(request)
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnError(err -> logFailure(method, url, err));
    }

    /**
     * Метод определяет, является ли ошибка ретраибельной
     * @param throwable
     * @return
     */
    private static boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientRequestException) {
            return true;
        }
        return throwable instanceof WebClientResponseException responseException
                && responseException.getStatusCode().is5xxServerError();
    }

    /**
     * Метод определяет, является ли ошибка ошибкой в работе сircuit breaker
     * @param throwable
     * @return  
     */
    private static boolean isCircuitBreakerFailure(Throwable throwable) {
        return isRetryable(throwable);
    }

    /**
     * Метод логирует ошибки, возникающие при выполнении запросов, с учетом типа ошибки (CallNotPermittedException или другие)
     * @param method
     * @param url
     * @param throwable
     */
    private void logFailure(String method, String url, Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            LOG.warn("Auth {} {} skipped by circuit breaker: {}", method, url, throwable.getMessage());
            return;
        }
        LOG.error("Auth {} {} failed: {}", method, url, throwable.getMessage());
    }

    /**
     * Метод строит конфигурацию для Retry, 
     * учитывая количество попыток, время ожидания между попытками и типы исключений, 
     * при которых следует выполнять повторные попытки
     * @param retryAttempts
     * @param retryWaitDuration
     * @return
     */
    private static Retry buildRetry(int retryAttempts, Duration retryWaitDuration) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(Math.max(retryAttempts, 1))
                .waitDuration(safeDuration(retryWaitDuration))
                .retryOnException(TgAuthCallWebClient::isRetryable)
                .failAfterMaxAttempts(true)
                .build();
        return Retry.of(AUTH_RETRY, config);
    }

    private static CircuitBreaker buildCircuitBreaker(
            float failureRateThreshold,
            Duration waitDurationInOpenState,
            int slidingWindowSize,
            int permittedNumberOfCallsInHalfOpenState
    ) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(normalizeFailureRate(failureRateThreshold))
                .waitDurationInOpenState(safeDuration(waitDurationInOpenState))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(Math.max(slidingWindowSize, 1))
                .permittedNumberOfCallsInHalfOpenState(
                        Math.max(permittedNumberOfCallsInHalfOpenState, 1)
                )
                .recordException(TgAuthCallWebClient::isCircuitBreakerFailure)
                .build();
        return io.github.resilience4j.circuitbreaker.CircuitBreaker.of(
                AUTH_CIRCUIT_BREAKER,
                config
        );
    }

    private static CircuitBreaker buildTestCircuitBreaker(int circuitBreakerFailureThreshold) {
        int failureThreshold = Math.max(circuitBreakerFailureThreshold, 1);
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(100)
                .waitDurationInOpenState(DEFAULT_CIRCUIT_BREAKER_OPEN_STATE)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(failureThreshold)
                .minimumNumberOfCalls(failureThreshold)
                .permittedNumberOfCallsInHalfOpenState(1)
                .recordException(TgAuthCallWebClient::isCircuitBreakerFailure)
                .build();
        return io.github.resilience4j.circuitbreaker.CircuitBreaker.of(
                AUTH_CIRCUIT_BREAKER,
                config
        );
    }

    /**
     * Метод нормализует значение порога отказов, гарантируя, что оно находится в пределах от 1 до 100, что является допустимым диапазоном для конфигурации CircuitBreaker
     * @param failureRateThreshold
     * @return
     */
    private static float normalizeFailureRate(float failureRateThreshold) {
        return Math.min(Math.max(failureRateThreshold, 1), 100);
    }

    /**
     * Метод проверяет, что переданная длительность не является null и не отрицательной, возвращая безопасное значение для конфигурации CircuitBreaker
     * @param duration
     * @return
     */
    private static Duration safeDuration(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return Duration.ZERO;
        }
        return duration;
    }
}
