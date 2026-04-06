package ru.checkdev.notification.telegram.action;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.TgBot;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
public interface Action {
    Optional<BotApiMethod> handle(Update update);

    default Map<String, Iterator<Action>> bindingActions() {
        return TgBot.getBindingBy();
    }
}
