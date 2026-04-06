package ru.checkdev.notification.telegram;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.checkdev.notification.telegram.action.Action;
import ru.checkdev.notification.telegram.action.info.UnKnownRequestAction;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс TgBootFake для имитации связи с телеграмм ботом,
 * используется для профиля develop, без использования Telegram API
 *
 * @author Dmitry Stepanov, user Dmitry
 * @since 04.12.2023
 */
public class TgBootFake implements Bot {
    private final Map<String, Iterator<Action>> bindingBy = new ConcurrentHashMap<>();
    private final Map<String, List<Action>> actions;
    private final String username;
    private final String token;

    public TgBootFake(Map<String, List<Action>> actions, String username, String token) {
        this.actions = actions;
        this.username = username;
        this.token = token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }
        var key = update.getMessage().getText();
        var chatId = update.getMessage().getChatId().toString();
        if (actions.containsKey(key)) {
            bindingBy.put(chatId, actions.get(key).iterator());
        } else if (!actions.containsKey(key) && !bindingBy.get(chatId).hasNext()) {
            var msg = new UnKnownRequestAction().handle(update);
            send(msg.get());
            return;
        }
        if (!bindingBy.containsKey(chatId)) {
            return;
        }
        var bindingActions = bindingBy.get(chatId);
        if (bindingActions == null || !bindingActions.hasNext()) {
            bindingBy.remove(chatId);
            return;
        }
        Optional<BotApiMethod> result;
        do {
            result = bindingActions.next().handle(update);
        } while (result.isEmpty());
        send(result.get());
    }

    @Override
    public void send(BotApiMethod msg) {
        System.out.println(msg);
    }
}
