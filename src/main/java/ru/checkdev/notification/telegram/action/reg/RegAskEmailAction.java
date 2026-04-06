package ru.checkdev.notification.telegram.action.reg;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

/**
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот.
 * # 1 RegAskNameAction - спрашивает имя.
 * # 2 RegPutNameAction - запоминает введенное имя пользователя.
 * # 3
 * RegAskEmailAction
 * Третий вызов регистрации спрашиваем Email.
 */

@AllArgsConstructor
public class RegAskEmailAction implements Action {
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId();
        var text = "";
        if (userTelegramService.findByChatId(chatId).isPresent()) {
            text = "Данный аккаунт Telegram уже зарегистрирован на сайте";
        } else {
            text = "Введите email для регистрации:";
        }
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}