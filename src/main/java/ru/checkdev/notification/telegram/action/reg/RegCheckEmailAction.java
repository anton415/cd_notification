package ru.checkdev.notification.telegram.action.reg;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.action.Action;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgCall;

import java.util.Optional;

/**
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот.
 * # 1 RegAskNameAction - спрашивает имя.
 * # 2 RegPutNameAction - запоминает введенное имя пользователя.
 * # 3 RegAskEmailAction - спрашиваем email
 * # 4 RegPutEmailAction - запоминает введенное Email пользователя.
 * # 5
 * RegCheckEmailAction
 * Пятый вызов регистрации проверяем введенный email пользователя.
 */
@AllArgsConstructor
@Slf4j
public class RegCheckEmailAction implements Action {
    private static final String URL_PROFILE_BY_EMAIL = "/profiles/tg/byEmail";
    private final SessionTg sessionTg;
    private final TgCall tgCall;
    private final TgConfig tgConfig = new TgConfig(10);

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var chatIdText = chatId.toString();
        var email = sessionTg.get(chatIdText, "email", "");
        var sl = System.lineSeparator();
        if (!tgConfig.isEmail(email)) {
            // Очищаем email, чтобы повторная регистрация начиналась с нового ввода
            var text = new StringBuilder().append("Email указан некорректно.").append(sl)
                    .append("Попробуйте снова.").append(sl)
                    .append("/new").toString();
            sessionTg.put(chatIdText, "email", "");
            return Optional.of(new SendMessage(chatIdText, text));
        }
        try {
            // Проверяем, не зарегистрирован ли уже пользователь с таким email
            var profile = new Profile();
            profile.setEmail(email);
            var result = tgCall.doPost(URL_PROFILE_BY_EMAIL, profile).block();
            if (result != null) {
                var text = new StringBuilder().append("Данный email уже занят.").append(sl)
                        .append("Попробуйте снова.").append(sl)
                        .append("/new").toString();
                sessionTg.put(chatIdText, "email", "");
                return Optional.of(new SendMessage(chatIdText, text));
            }
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            sessionTg.put(chatIdText, "email", "");
            var text = String.format("Сервис не доступен попробуйте позже%s%s", sl, "/start");
            return Optional.of(new SendMessage(chatIdText, text));
        }
        return Optional.empty();
    }
}
