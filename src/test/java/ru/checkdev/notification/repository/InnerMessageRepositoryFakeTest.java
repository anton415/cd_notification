package ru.checkdev.notification.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.InnerMessage;
import ru.checkdev.notification.dto.InnerMessageDTO;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InnerMessageRepositoryFakeTest {

    private static InnerMessageRepositoryFake innerMessageRepository;
    private static InnerMessage message1;
    private static InnerMessage message2;
    private static InnerMessage message3;

    @BeforeAll
    static void init() {
        innerMessageRepository = new InnerMessageRepositoryFake();
        message1 = new InnerMessage(1, 12, "message_1",
                new Timestamp(System.currentTimeMillis()), false, 1);
        message2 = new InnerMessage(2, 12, "message_2",
                new Timestamp(System.currentTimeMillis()), true, 2);
        message3 = new InnerMessage(3, 12, "message_3",
                new Timestamp(System.currentTimeMillis()), false, 3);

        innerMessageRepository.save(message1);
        innerMessageRepository.save(message2);
        innerMessageRepository.save(message3);
    }

    @Test
    void whenFindByUserIdAndReadFalseWhenOneOfInterviewsHasReadTrue() {
        assertThat(innerMessageRepository.findByUserIdAndReadFalse(12))
                .isEqualTo(List.of(message1, message3));
    }

    @Test
    void whenFindByUserIdAndReadFalseWhenOneOfUserIdsDifferent() {
        assertThat(innerMessageRepository.findByUserIdAndReadFalse(12))
                .isEqualTo(List.of(message1, message2));
    }

    @Test
    void whenNothingFoundByUserIdAndReadFalse() {
        assertThat(innerMessageRepository.findByUserIdAndReadFalse(9))
                .isEqualTo(List.of());
    }

    @Test
    void whenNoOneDTOFoundByUserIdAndReadFalse() {
        assertThat(innerMessageRepository.findMessageDTOByUserIdAndReadFalse(27))
                .isEqualTo(List.of());
    }

    @Test
    void whenFindDTOByUserIdAndReadFalseWhenOneOfInterviewsHasReadTrue() {
        var messageDTO1 = new InnerMessageDTO(1, 12, "message_1",
                message1.getCreated(), 1);
        var messageDTO3 = new InnerMessageDTO(3, 12, "message_3",
                message3.getCreated(), 3);

        assertThat(innerMessageRepository.findMessageDTOByUserIdAndReadFalse(12))
                .isEqualTo(List.of(messageDTO1, messageDTO3));
    }

    @Test
    void whenFindDTOByUserIdAndReadFalseWhenOneOfUserIdsDifferent() {
        var messageDTO1 = new InnerMessageDTO(1, 12, "message_1",
                message1.getCreated(), 1);
        var messageDTO3 = new InnerMessageDTO(3, 12, "message_3",
                message3.getCreated(), 3);

        assertThat(innerMessageRepository.findMessageDTOByUserIdAndReadFalse(12))
                .isEqualTo(List.of(messageDTO1, messageDTO3));
    }

}
