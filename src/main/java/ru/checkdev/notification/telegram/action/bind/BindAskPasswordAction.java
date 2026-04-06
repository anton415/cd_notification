package ru.checkdev.notification.telegram.action.bind;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

/**
 * Класс реализует пункт меню телеграм бота /bind -
 * привязать аккаунт CheckDev к текущему аккаунту Telegram.
 * # 1 BindAskEmailAction - спрашиваем email.
 * # 2 BindPutEmailAction - сохраняется введенный email пользователя.
 * # 3
 * BindAskPasswordAction - третий шаг, спрашиваем пароль.
 */

@AllArgsConstructor
public class BindAskPasswordAction implements Action {
    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var text = "Введите пароль:";
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}