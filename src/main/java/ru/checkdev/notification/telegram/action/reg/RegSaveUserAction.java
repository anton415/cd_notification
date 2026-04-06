package ru.checkdev.notification.telegram.action.reg;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.dto.ProfileTgDTO;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.action.Action;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgCall;

import java.util.Calendar;
import java.util.Optional;

/**
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот.
 * # 1 RegAskNameAction - спрашивает имя.
 * # 2 RegPutNameAction - запоминает введенное имя пользователя.
 * # 3 RegAskEmailAction - спрашиваем email
 * # 4 RegPutEmailAction - запоминает введенное Email пользователя.
 * # 5 RegCheckEmailAction - проверяем введенный email пользователя.
 * # 6
 * RegSaveUserAction
 * Шестой вызов регистрации сохраняем нового пользователя в системе.
 */
@AllArgsConstructor
@Slf4j
public class RegSaveUserAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_REGISTRATION = "/registration";
    private final TgConfig tgConfig = new TgConfig(10);
    private final SessionTg sessionTg;
    private final TgCall tgCall;
    private final UserTelegramService userTelegramService;
    private final String urlSiteAuth;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        Object result;
        var text = "";
        var chatId = update.getMessage().getChatId();
        var email = sessionTg.get(chatId.toString(), "email", "");
        if (email.equals("")) {
            text = "Пройдите регистрацию заново" + System.lineSeparator() + "/new";
            return Optional.of(new SendMessage(chatId.toString(), text));
        }
        var ls = System.lineSeparator();
        var username = sessionTg.get(chatId.toString(), "name", "");
        var password = tgConfig.getPassword();
        var profile = new Profile(0, username, email,
                password, true, Calendar.getInstance());
        try {
            result = tgCall.doPost(URL_AUTH_REGISTRATION, profile).block();
            var mapObject = tgConfig.getObjectToMap(result);
            if (mapObject.containsKey(ERROR_OBJECT)) {
                text = "Ошибка регистрации: " + mapObject.get(ERROR_OBJECT);
                return Optional.of(new SendMessage(chatId.toString(), text));
            }
            var profileTg = tgConfig.getMapper().convertValue(result, ProfileTgDTO.class);
            int userId = profileTg.getId();
            profile.setId(userId);
            sessionTg.put(chatId.toString(), "userId", Integer.toString(userId));
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = String.format("Сервис не доступен попробуйте позже%s%s", ls, "/start");
            return Optional.of(new SendMessage(chatId.toString(), text));
        }
        text = new StringBuilder().append("Вы зарегистрированы: ").append(ls)
                .append("Имя: ").append(profile.getUsername()).append(ls)
                .append("Email: ").append(profile.getEmail()).append(ls)
                .append("Пароль : ").append(password).append(ls)
                .append(urlSiteAuth).toString();
        userTelegramService.save(new UserTelegram(0, profile.getId(), chatId, false));
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}