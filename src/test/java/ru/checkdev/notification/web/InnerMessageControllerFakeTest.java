package ru.checkdev.notification.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.repository.InnerMessageRepositoryFake;
import ru.checkdev.notification.service.EurekaUriProvider;
import ru.checkdev.notification.service.InnerMessageService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class InnerMessageControllerFakeTest {

    @Mock
    private EurekaUriProvider uriProvider;

    @Test
    public void whenFindBotMessageByUserId() {
        InnerMessage botMessage = new InnerMessage(1, 10, "text", null, false);
        InnerMessageService innerMessageService = new InnerMessageService(
                new InnerMessageRepositoryFake(), null, uriProvider);
        InnerMessageController controller = new InnerMessageController(
                innerMessageService, null, null, null);

        InnerMessage savedMsg = innerMessageService.saveMessage(botMessage);
        List<InnerMessage> resp = controller.findMessage(savedMsg.getUserId()).getBody();

        assertThat(resp).contains(savedMsg);
    }
}