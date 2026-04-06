package ru.checkdev.notification.telegram.action.reg;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.SessionTg;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

/**
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот.
 * # 1 RegAskNameAction - спрашивает имя.
 * # 2 RegPutNameAction - запоминает введенное имя пользователя.
 * # 3 RegAskEmailAction - спрашиваем email
 * # 4
 * RegPutEmailAction
 * Четвертый вызов регистрации запоминает введенное Email пользователя.
 */
@AllArgsConstructor
public class RegPutEmailAction implements Action {
    private final SessionTg sessionTg;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var msg = update.getMessage();
        var chatId = msg.getChatId().toString();
        var email = msg.getText();
        sessionTg.put(chatId, "email", email);
        return Optional.empty();
    }
}
