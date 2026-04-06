package ru.checkdev.notification.telegram.action.reg;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.service.UserTelegramService;
import ru.checkdev.notification.telegram.action.Action;

import java.util.Optional;

/**
 * Класс реализует пункт меню регистрации нового пользователя в телеграм бот.
 * # 1
 * RegAskNameAction - спрашивает имя.
 * Первый вызов регистрации спрашиваем имя.
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 * Arcady555
 * 06.11.2023
 */
@AllArgsConstructor
@Slf4j
public class RegAskNameAction implements Action {
    private final UserTelegramService userTelegramService;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var msg = update.getMessage();
        var text = "";
        var chatId = msg.getChatId();
        if (userTelegramService.findByChatId(chatId).isPresent()) {
            text = "Данный аккаунт Telegram уже зарегистрирован на сайте";
            bindingActions().remove(chatId.toString());
        } else {
            text = "Введите имя для регистрации нового пользователя:";
        }
        return Optional.of(new SendMessage(chatId.toString(), text));
    }
}
