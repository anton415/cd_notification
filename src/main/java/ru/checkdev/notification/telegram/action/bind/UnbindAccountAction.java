package ru.checkdev.notification.telegram.action.bind;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

/**
 * Команда телеграм бота /unbind -
 * отвязать аккаунт CheckDev от текущего аккаунта Telegram
 */

@AllArgsConstructor
public class UnbindAccountAction implements Action {
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var text = "";
        var user = userTelegramService.findByChatId(chatId);
        if (user.isPresent()) {
            userTelegramService.delete(user.get());
            text = "Ваш аккаунт CheckDev отвязан от текущего аккаунта Telegram";
        } else {
            text = "К данному аккаунту телеграм не привязан аккаунт CheckDev";
        }
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}
