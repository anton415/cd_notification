package ru.checkdev.notification.telegram.action.bind;

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

import java.util.Optional;

/**
 * Класс реализует пункт меню телеграм бота /bind -
 * привязать аккаунт CheckDev к текущему аккаунту Telegram.
 * # 1 BindAskEmailAction - спрашиваем email.
 * # 2 BindPutEmailAction - сохраняется введенный email пользователя.
 * # 3 BindAskPasswordAction - спрашиваем пароль.
 * # 4 BindPutPasswordAction - сохраняется введенный пароль.
 * # 5
 * BindAccountAction - пятый шаг, привязка аккаунта CheckDev,
 * найденного по email и паролю, к аккаунту Telegram
 */

@AllArgsConstructor
@Slf4j
public class BindAccountAction implements Action {
    private static final String URL_PROFILE_BY_EMAIL_AND_PASS = "/profiles/tg/byEmailAndPassword";
    private final TgConfig tgConfig = new TgConfig(0);
    private final SessionTg sessionTg;
    private final TgCall tgCall;
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        Object result;
        var text = "";
        var ls = System.lineSeparator();
        var chatId = update.getMessage().getChatId();
        var email = sessionTg.get(chatId.toString(), "email", "");
        var password = sessionTg.get(chatId.toString(), "password", "");
        var profile = new Profile();
        profile.setEmail(email);
        profile.setPassword(password);
        try {
            result = tgCall.doPost(URL_PROFILE_BY_EMAIL_AND_PASS, profile).block();
            if (result == null) {
                text = "Пользователь не найден";
            } else {
                var profileTg = tgConfig.getMapper().convertValue(result, ProfileTgDTO.class);
                boolean alreadyExist = !userTelegramService.save(new UserTelegram(0, profileTg.getId(), chatId, false));
                text = alreadyExist ? "Данный аккаунт CheckDev уже привязан к другому аккаунту Telegram"
                        : "Ваш аккаунт CheckDev успешно привязан к данному аккаунту Telegram";
            }
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            text = String.format("Сервис недоступен, попробуйте позже%s%s", ls, "/start");
        }
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}
