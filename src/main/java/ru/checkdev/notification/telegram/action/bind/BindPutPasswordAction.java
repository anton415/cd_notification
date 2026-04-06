package ru.checkdev.notification.telegram.action.bind;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

/**
 * Класс реализует пункт меню телеграм бота /bind -
 * привязать аккаунт CheckDev к текущему аккаунту Telegram.
 * # 1 BindAskEmailAction - спрашиваем email.
 * # 2 BindPutEmailAction - сохраняется введенный email пользователя.
 * # 3 BindAskPasswordAction - спрашиваем пароль.
 * # 4
 * BindPutPasswordAction - четвертый шаг, сохраняется введенный пароль.
 */
@AllArgsConstructor
public class BindPutPasswordAction implements Action {
    private final SessionTg sessionTg;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var msg = update.getMessage();
        var chatId = msg.getChatId().toString();
        var password = msg.getText();
        sessionTg.put(chatId, "password", password);
        return Optional.empty();
    }
}