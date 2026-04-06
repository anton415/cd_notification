package ru.checkdev.notification.repository;

import org.springframework.test.fake.CrudRepositoryFake;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.InnerMessageDTO;

import java.util.List;
import java.util.Objects;

public class InnerMessageRepositoryFake
        extends CrudRepositoryFake<InnerMessage, Integer>
        implements InnerMessageRepository {

    @Override
    public List<InnerMessage> findByUserIdAndReadFalse(int id) {
        return memory.values().stream()
                .filter(msg -> Objects.equals(msg.getUserId(), id))
                .filter(msg -> !msg.isRead())
                .toList();
    }

    @Override
    public List<InnerMessageDTO> findMessageDTOByUserIdAndReadFalse(int userId) {
        return memory.values().stream()
                .filter(msg -> !msg.isRead())
                .map(im -> new InnerMessageDTO(im.getId(),
                        im.getUserId(), im.getText(), im.getCreated(), im.getInterviewId()))
                .filter(msg -> Objects.equals(msg.getUserId(), userId))
                .toList();
    }

    @Override
    public void setReadById(int messageId) {
        memory.values().stream().
                filter(msg -> Objects.equals(msg.getId(), messageId))
                .forEach(msg -> msg.setRead(true));
    }

    @Override
    public void setReadAll(int userId) {
        memory.values().stream().
                filter(msg -> Objects.equals(msg.getUserId(), userId))
                .forEach(msg -> msg.setRead(true));
    }
}
