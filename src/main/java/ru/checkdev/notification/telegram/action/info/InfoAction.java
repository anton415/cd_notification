package ru.checkdev.notification.telegram.action.info;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.action.Action;

import java.util.List;
import java.util.Optional;

/**
 * Класс реализует вывод доступных команд телеграмм бота
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 12.09.2023
 */
@AllArgsConstructor
public class InfoAction implements Action {
    private final List<String> actions;

    @Override
    public Optional<BotApiMethod> handle(Update update) {
        var chatId = update.getMessage().getChatId().toString();
        String sl = System.lineSeparator();
        var out = new StringBuilder();
        out.append("Доступные команды:").append(sl);
        for (String action : actions) {
            out.append(action).append(sl);
        }
        return Optional.of(new SendMessage(chatId, out.toString()));
    }
}
