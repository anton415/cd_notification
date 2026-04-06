package ru.checkdev.notification.telegram.action.bind;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

/**
 * Класс реализует пункт меню телеграм бота /bind -
 * привязать аккаунт CheckDev к текущему аккаунту Telegram.
 * # 1
 * BindAskEmailAction - первый шаг, спрашиваем email.
 */

@AllArgsConstructor
public class BindAskEmailAction implements Action {
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var text = "";
        if (userTelegramService.findByChatId(chatId).isPresent()) {
            text = "К данному аккаунту Telegram уже привязан аккаунт CheckDev";
            bindingActions().remove(chatId.toString());
        } else {
            text = "Введите email пользователя:";
        }
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}
