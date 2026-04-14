package ru.checkdev.notification.telegram.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Упрощенная реализация Circuit Breaker для вызовов auth-сервиса.
 */
public class CircuitBreaker {
    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreaker.class);

    private final int failureThreshold;
    private final AtomicInteger failureCount = new AtomicInteger();
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);

    public CircuitBreaker(int failureThreshold) {
        this.failureThreshold = Math.max(failureThreshold, 1);
    }

    public <T> Mono<T> execute(String method,
                               String url,
                               Supplier<Mono<T>> request,
                               Predicate<Throwable> breakerFailure) {
        return Mono.defer(() -> {
            if (state.get() == State.OPEN) {
                LOG.warn("Circuit breaker is OPEN for auth {} {}. Request skipped.", method, url);
                return Mono.error(new CircuitBreakerOpenException(
                        String.format("Circuit breaker is OPEN for auth %s %s. Request skipped.", method, url)
                ));
            }
            return request.get()
                    .doOnSuccess(response -> reset())
                    .doOnError(throwable -> {
                        if (!breakerFailure.test(throwable)) {
                            return;
                        }
                        int currentFailures = failureCount.incrementAndGet();
                        LOG.error(
                                "Auth {} {} failed. Circuit breaker failure count: {}",
                                method,
                                url,
                                currentFailures
                        );
                        if (currentFailures >= failureThreshold
                                && state.compareAndSet(State.CLOSED, State.OPEN)) {
                            LOG.warn(
                                    "Circuit breaker OPENED for auth {} {} after {} failures.",
                                    method,
                                    url,
                                    currentFailures
                            );
                        }
                    });
        });
    }

    private void reset() {
        failureCount.set(0);
        state.set(State.CLOSED);
    }

    private enum State {
        OPEN,
        CLOSED
    }

    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
