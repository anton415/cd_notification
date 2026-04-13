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
        var text = "";
        var chatId = update.getMessage().getChatId();
        var email = sessionTg.get(chatId.toString(), "email", "");
        var sl = System.lineSeparator();
        if (!tgConfig.isEmail(email)) {
            text = new StringBuilder().append("Email: ").append(email)
                    .append(" не корректный.").append(sl)
                    .append("попробуйте снова.").append(sl)
                    .append("/new").toString();
            sessionTg.put(chatId.toString(), "email", "");
            bindingActions().remove(chatId.toString());
            return Optional.of(new SendMessage(chatId.toString(), text));
        }
        Profile profile = new Profile();
        profile.setEmail(email);
        try {
            Object result = tgCall.doPost(URL_PROFILE_BY_EMAIL, profile).block();
            if (result != null) {
                text = new StringBuilder().append("Пользователь с почтой ")
                        .append(email)
                        .append(" уже зарегистрирован.").append(sl)
                        .append("/new").toString();
                sessionTg.put(chatId.toString(), "email", "");
                bindingActions().remove(chatId.toString());
                return Optional.of(new SendMessage(chatId.toString(), text));
            }
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            bindingActions().remove(chatId.toString());
            text = String.format("Сервис недоступен, попробуйте позже%s%s", sl, "/start");
            return Optional.of(new SendMessage(chatId.toString(), text));
        }
        return Optional.empty();
    }
}
