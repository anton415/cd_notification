package ru.checkdev.notification.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.checkdev.notification.service.EurekaUriProvider;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.action.Action;
import ru.checkdev.notification.telegram.action.bind.*;
import ru.checkdev.notification.telegram.action.check.CheckAction;
import ru.checkdev.notification.telegram.action.info.InfoAction;
import ru.checkdev.notification.telegram.action.notify.NotifyAction;
import ru.checkdev.notification.telegram.action.notify.UnNotifyAction;
import ru.checkdev.notification.telegram.action.reg.*;
import ru.checkdev.notification.telegram.service.TgCall;

import java.util.List;
import java.util.Map;

/**
 * Класс создание экземпляр класса TgBotFake для профиля develop без использованием Telegram API
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 04.12.2023
 */
@Component
@Profile("develop")
@Slf4j
@RequiredArgsConstructor
public class TgConfigFake {

    private final SessionTg sessionTg = new SessionTg();
    private final TgCall tgCall;
    private final UserTelegramService userTelegramService;
    @Value("${tg.username}")
    private String username;
    @Value("${tg.token}")
    private String token;

    private final EurekaUriProvider uriProvider;
    private static final String SERVICE_ID = "site";

    @Bean
    public Bot initTg() {
        Map<String, List<Action>> actionMap = Map.of(
                "/start", List.of(new InfoAction(List.of(
                        "/start    - Доступные команды",
                        "/new      - Зарегистрировать нового пользователя",
                        "/check    - Связанный аккаунт",
                        "/forget   - Восстановить пароль",
                        "/notify   - Подписаться на уведомления",
                        "/unnotify - Отписаться от уведомлений",
                        "/bind     - Привязать аккаунт CheckDev к данному аккаунту Telegram",
                        "/unbind   - Отвязать аккаунт CheckDev от данного аккаунта Telegram"))),
                "/new", List.of(
                        new RegAskNameAction(userTelegramService),
                        new RegPutNameAction(sessionTg),
                        new RegAskEmailAction(userTelegramService),
                        new RegPutEmailAction(sessionTg),
                        new RegCheckEmailAction(sessionTg),
                        new RegSaveUserAction(sessionTg, tgCall, userTelegramService,
                                String.format("%s/login", uriProvider.getUri(SERVICE_ID)))
                ),
                "/check", List.of(new CheckAction(sessionTg, tgCall, userTelegramService)),
                "/notify", List.of(new NotifyAction(sessionTg, userTelegramService)),
                "/unnotify", List.of(new UnNotifyAction(sessionTg, userTelegramService)),
                "/bind", List.of(new BindAskEmailAction(userTelegramService),
                        new BindPutEmailAction(sessionTg),
                        new BindAskPasswordAction(),
                        new BindPutPasswordAction(sessionTg),
                        new BindAccountAction(sessionTg, tgCall, userTelegramService)),
                "/unbind", List.of(new UnbindAccountAction(userTelegramService))
        );
        TgBootFake menu = new TgBootFake(actionMap, username, token);
        return menu;
    }
}
