package ru.checkdev.notification.telegram.action.bind;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.dto.ProfileTgDTO;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.action.Action;
import ru.checkdev.notification.telegram.config.TgConfig;
import ru.checkdev.notification.telegram.service.TgCall;

import java.util.Optional;

/**
 * Команда телеграм бота /unbind -
 * отвязать аккаунт CheckDev от текущего аккаунта Telegram
 */

@AllArgsConstructor
@Slf4j
public class UnbindAccountAction implements Action {
    private static final String URL_PROFILE_BY_EMAIL_AND_PASS = "/profiles/tg/byEmailAndPassword";
    private final TgConfig tgConfig = new TgConfig(0);
    private final SessionTg sessionTg;
    private final TgCall tgCall;
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var ls = System.lineSeparator();
        var user = userTelegramService.findByChatId(chatId);
        if (user.isEmpty()) {
            return Optional.of(new SendMessage(
                    chatId.toString(),
                    "К данному аккаунту Telegram не привязан аккаунт CheckDev"
            ));
        }
        var email = sessionTg.get(chatId.toString(), "email", "");
        var password = sessionTg.get(chatId.toString(), "password", "");
        var profile = new ru.checkdev.notification.domain.Profile();
        profile.setEmail(email);
        profile.setPassword(password);
        try {
            var result = tgCall.doPost(URL_PROFILE_BY_EMAIL_AND_PASS, profile).block();
            if (result == null) {
                return Optional.of(new SendMessage(chatId.toString(), "Пользователь не найден"));
            }
            var profileTg = tgConfig.getMapper().convertValue(result, ProfileTgDTO.class);
            if (user.get().getUserId() != profileTg.getId()) {
                return Optional.of(new SendMessage(
                        chatId.toString(),
                        "Указанный аккаунт CheckDev не привязан к данному аккаунту Telegram"
                ));
            }
            userTelegramService.delete(user.get());
            return Optional.of(new SendMessage(
                    chatId.toString(),
                    "Ваш аккаунт CheckDev отвязан от текущего аккаунта Telegram"
            ));
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            return Optional.of(new SendMessage(
                    chatId.toString(),
                    String.format("Сервис недоступен, попробуйте позже%s%s", ls, "/start")
            ));
        }
    }
}
