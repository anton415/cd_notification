package ru.checkdev.notification.telegram.action.check;

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
import ru.checkdev.notification.telegram.service.TgCall;

import java.util.Optional;

/**
 * Telegram Action команда /check
 * Получить свое имя и email
 */
@AllArgsConstructor
@Slf4j
public class CheckAction implements Action {
    private static final String URL_AUTH_CURRENT = "/profiles/tg/";
    private final SessionTg sessionTg;
    private final TgCall tgCall;
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var out = new StringBuilder();
        String sl = System.lineSeparator();
        Optional<UserTelegram> chatIdOptional = userTelegramService.findByChatId(chatId);
        if (chatIdOptional.isEmpty()) {
            out.append("Данный аккаунт Telegram на сайте не зарегистрирован");
            return Optional.of(new SendMessage(chatId.toString(), out.toString()));
        }
        try {
            UserTelegram userTelegram = chatIdOptional.get();
            Profile profile = tgCall
                    .doGet(URL_AUTH_CURRENT + userTelegram.getUserId()).block();
            out.append("Имя:")
                    .append(sl)
                    .append(profile.getUsername())
                    .append(sl)
                    .append("Email:")
                    .append(sl)
                    .append(profile.getEmail())
                    .append(sl);
            sessionTg.put(chatId.toString(), "userId", Integer.toString(profile.getId()));
            return Optional.of(new SendMessage(chatId.toString(), out.toString()));
        } catch (Exception e) {
            log.error("WebClient doPost error: {}", e.getMessage());
            out.append("Сервис не доступен попробуйте позже");
            return Optional.of(new SendMessage(chatId.toString(), out.toString()));
        }
    }
}