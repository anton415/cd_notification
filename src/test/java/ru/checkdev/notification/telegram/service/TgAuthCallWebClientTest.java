package ru.checkdev.notification.telegram.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.Profile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testing TgAuthCallWebClint
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 06.10.2023
 */
class TgAuthCallWebClientTest {
    private HttpServer server;
    private TgAuthCallWebClient tgAuthCallWebClient;

    @BeforeEach
    void init() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        String url = "http://localhost:" + server.getAddress().getPort();
        tgAuthCallWebClient = new TgAuthCallWebClient(url, 3, 10, 2);
    }

    @AfterEach
    void close() {
        server.stop(0);
    }

    @Test
    @DisplayName("Когда doGet неудачен два раза, то повторить попытку и вернуть профиль")
    void whenDoGetFailedTwiceThenRetryAndReturnProfile() {
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/person/100", exchange -> {
            int currentAttempt = attempts.incrementAndGet();
            if (currentAttempt < 3) {
                sendResponse(exchange, 503, "{\"error\":\"temporary\"}");
                return;
            }
            sendResponse(exchange, 200, """
                    {"id":100,"username":"username","email":"mail","password":"password","privacy":true}
                    """);
        });

        Profile actual = tgAuthCallWebClient.doGet("/person/100").block();

        assertThat(actual.getId()).isEqualTo(100);
        assertThat(actual.getUsername()).isEqualTo("username");
        assertThat(actual.getEmail()).isEqualTo("mail");
        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(actual.isPrivacy()).isTrue();
        assertThat(actual.getCreated()).isNull();
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Когда doPost всегда терпит неудачу, то повторить попытку трижды и выбросить исключение")
    void whenDoPostAlwaysFailsThenRetryThreeTimesAndThrowException() {
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/person/created", exchange -> {
            attempts.incrementAndGet();
            exchange.getRequestBody().readAllBytes();
            sendResponse(exchange, 503, "{\"error\":\"temporary\"}");
        });
        Profile profile = new Profile(
                0, "username", "mail", "password", true, Calendar.getInstance()
        );

        assertThatThrownBy(() -> tgAuthCallWebClient.doPost("/person/created", profile).block())
                .hasMessageContaining("503");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Когда doPost без тела неудачен два раза, то повторить попытку и вернуть ответ")
    void whenDoPostWithoutBodyFailedTwiceThenRetryAndReturnResponse() {
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/person/forgot", exchange -> {
            int currentAttempt = attempts.incrementAndGet();
            if (currentAttempt < 3) {
                sendResponse(exchange, 503, "{\"error\":\"temporary\"}");
                return;
            }
            sendResponse(exchange, 200, "{\"status\":\"ok\"}");
        });

        Object actual = tgAuthCallWebClient.doPost("/person/forgot").block();

        assertThat(actual).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) actual).get("status")).isEqualTo("ok");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Когда doGet возвращает ошибку клиента, то не повторять попытку")
    void whenDoGetClientErrorThenDoNotRetry() {
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/person/400", exchange -> {
            attempts.incrementAndGet();
            sendResponse(exchange, 400, "{\"error\":\"bad request\"}");
        });

        assertThatThrownBy(() -> tgAuthCallWebClient.doGet("/person/400").block())
                .hasMessageContaining("400");
        assertThat(attempts.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("Когда circuit breaker открыт, то следующий запрос не отправляется в auth")
    void whenCircuitBreakerIsOpenThenSkipNextRequest() {
        tgAuthCallWebClient = new TgAuthCallWebClient(
                "http://localhost:" + server.getAddress().getPort(),
                3,
                10,
                1
        );
        AtomicInteger attempts = new AtomicInteger();
        server.createContext("/person/503", exchange -> {
            attempts.incrementAndGet();
            sendResponse(exchange, 503, "{\"error\":\"temporary\"}");
        });

        assertThatThrownBy(() -> tgAuthCallWebClient.doGet("/person/503").block())
                .hasMessageContaining("503");
        assertThatThrownBy(() -> tgAuthCallWebClient.doGet("/person/503").block())
                .hasMessageContaining("Circuit breaker is OPEN");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("Когда ошибка клиента 400, то circuit breaker не должен открываться")
    void whenClientErrorThenCircuitBreakerShouldStayClosed() {
        tgAuthCallWebClient = new TgAuthCallWebClient(
                "http://localhost:" + server.getAddress().getPort(),
                3,
                10,
                1
        );
        AtomicInteger clientErrorAttempts = new AtomicInteger();
        AtomicInteger successAttempts = new AtomicInteger();
        server.createContext("/person/400-then-200", exchange -> {
            clientErrorAttempts.incrementAndGet();
            sendResponse(exchange, 400, "{\"error\":\"bad request\"}");
        });
        server.createContext("/person/200", exchange -> {
            successAttempts.incrementAndGet();
            sendResponse(exchange, 200, """
                    {"id":100,"username":"username","email":"mail","password":"password","privacy":true}
                    """);
        });

        assertThatThrownBy(() -> tgAuthCallWebClient.doGet("/person/400-then-200").block())
                .hasMessageContaining("400");

        Profile actual = tgAuthCallWebClient.doGet("/person/200").block();

        assertThat(actual.getId()).isEqualTo(100);
        assertThat(clientErrorAttempts.get()).isEqualTo(1);
        assertThat(successAttempts.get()).isEqualTo(1);
    }

    private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
