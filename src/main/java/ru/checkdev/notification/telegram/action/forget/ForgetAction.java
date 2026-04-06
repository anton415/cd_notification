package ru.checkdev.notification.telegram.action.forget;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.Profile;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.action.Action;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgCall;

import java.util.Optional;

/**
 * Генерация нового пароля команда телеграм /forget
 */
@AllArgsConstructor
@Slf4j
public class ForgetAction implements Action {
    private static final String ERROR_OBJECT = "error";
    private static final String URL_AUTH_CURRENT = "/profiles/tg/";
    private static final String URL_AUTH_FORGOT = "/forgotTg";
    private final SessionTg sessionTg;
    private final TgCall tgCall;
    private final UserTelegramService userTelegramService;
    private final TgConfig tgConfig = new TgConfig(10);

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        Object result;
        var chatId = update.getMessage().getChatId();
        var text = "";
        String sl = System.lineSeparator();
        Optional<UserTelegram> chatIdOptional = userTelegramService.findByChatId(chatId);
        if (chatIdOptional.isEmpty()) {
            text = "Данный аккаунт Telegram на сайте не зарегистрирован";
            return Optional.of(new SendMessage(chatId.toString(), text));
        }
        try {
            UserTelegram userTelegram = chatIdOptional.get();
            Profile profile = tgCall.doGet(URL_AUTH_CURRENT + userTelegram.getUserId()).block();
            sessionTg.put(chatId.toString(), "userId", Integer.toString(profile.getId()));
            var password = tgConfig.getPassword();
            profile.setPassword(password);
            result = tgCall.doPost(URL_AUTH_FORGOT, profile).block();
            text = "Ваш новый пароль:" + sl + password;
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = "Сервис не доступен попробуйте позже";
            return Optional.of(new SendMessage(chatId.toString(), text));
        }
        var mapObject = tgConfig.getObjectToMap(result);
        if (mapObject.containsKey(ERROR_OBJECT)) {
            text = "Ошибка смены пароля: " + mapObject.get(ERROR_OBJECT);
            return Optional.of(new SendMessage(chatId.toString(), text));
        }
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}