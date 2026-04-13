package ru.checkdev.notification.telegram.action.bind;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

@AllArgsConstructor
public class UnbindAskEmailAction implements Action {
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var text = "";
        if (userTelegramService.findByChatId(chatId).isEmpty()) {
            text = "К данному аккаунту Telegram не привязан аккаунт CheckDev";
            bindingActions().remove(chatId.toString());
        } else {
            text = "Введите email (логин) пользователя:";
        }
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}
