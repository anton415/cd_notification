package ru.checkdev.notification.telegram.action.notify;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

/**
 * Telegram Action команда /notify
 * Подписаться на уведомления в телеграмм.
 */
@AllArgsConstructor
@Slf4j
public class NotifyAction implements Action {
    private final SessionTg sessionTg;
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var out = new StringBuilder();
        String ls = System.lineSeparator();
        Optional<UserTelegram> tgUserOptional = userTelegramService.findByChatId(chatId);
        if (tgUserOptional.isEmpty()) {
            out.append("Данный аккаунт Telegram не зарегистрирован на сайте.").append(ls)
                    .append("Для регистрации, пожалуйста, воспользуйтесь командой /start");
            return Optional.of(new SendMessage(chatId.toString(), out.toString()));
        }

        UserTelegram userTelegram = tgUserOptional.get();
        sessionTg.put(chatId.toString(), "userId", Integer.toString(userTelegram.getUserId()));
        if (userTelegram.isNotifiable()) {
            out.append("Уведомления в телеграмм уже включены.");
            return Optional.of(new SendMessage(chatId.toString(), out.toString()));
        }

        userTelegramService.setNotifiableByChatId(userTelegram.getChatId());
        out.append("Вы подписались на уведомления с сайта телеграмм бота.");
        return Optional.of(new SendMessage(chatId.toString(), out.toString()));
    }
}