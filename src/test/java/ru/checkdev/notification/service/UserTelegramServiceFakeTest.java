package ru.checkdev.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.domain.UserTelegram;
import ru.checkdev.notification.repository.SubscribeTopicRepositoryFake;
import ru.checkdev.notification.repository.UserTelegramRepositoryFake;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserTelegramServiceFakeTest {

    private static final long CHAT_ID = 333L;
    private static final UserTelegram USER_TG = new UserTelegram(11, 10, CHAT_ID, false);

    private UserTelegramService service;

    @BeforeEach
    void init() {
        service = new UserTelegramService(
                new UserTelegramRepositoryFake(
                        new SubscribeTopicRepositoryFake()));
    }

    @Test
    void whenSaveUserTelegramAndFindByUserId() {
        service.save(USER_TG);
        Optional<UserTelegram> userTg = service.findByChatId(CHAT_ID);
        assertThat(userTg).isPresent();
        assertThat(userTg.get().getUserId()).isEqualTo(10);
    }

    @Test
    void whenFindByChatIdThenReturnOptionalEmpty() {
        Optional<UserTelegram> actual = service.findByChatId(CHAT_ID);
        assertThat(actual).isEmpty();
    }

    @Test
    void whenFindByChatIdThenReturnOptionalUserID() {
        service.save(USER_TG);
        Optional<UserTelegram> actual = service.findByChatId(CHAT_ID);
        assertThat(actual).isPresent();
        assertThat(actual).contains(USER_TG);
    }

    @Test
    void whenFindByUserIdThenOptionalEmpty() {
        Optional<UserTelegram> actual = service.findByUserId(-1);
        assertThat(actual).isEmpty();
    }

    @Test
    void whenFindByUserIdThenOptionalUserTg() {
        service.save(USER_TG);
        Optional<UserTelegram> actual = service.findByUserId(USER_TG.getUserId());
        assertThat(actual).isPresent();
        assertThat(actual).contains(USER_TG);
    }

    @Test
    void whenFindAllByTopicIdAndUserIdNotWhenEmptyList() {
        List<UserTelegram> actual = service.findAllByTopicIdAndUserIdNot(-1, -1);
        assertThat(actual.isEmpty()).isTrue();
    }

    @Test
    void whenSetNotifiableThenNotifiableValueChangedToTrue() {
        UserTelegram user = new UserTelegram(3, 1, 1111L, false);
        service.save(user);
        service.setNotifiableByChatId(user.getChatId());
        boolean notifiable = service.findByUserId(user.getUserId()).get().isNotifiable();
        assertThat(notifiable).isTrue();
    }

    @Test
    void whenSetUnNotifiableThenNotifiableValueChangedToFalse() {
        UserTelegram user = new UserTelegram(4, 1, 1111L, true);
        service.save(user);
        service.setUnNotifiableByChatId(user.getChatId());
        boolean notifiable = service.findByUserId(user.getUserId()).get().isNotifiable();
        assertThat(notifiable).isFalse();
    }

    @Test
    void whenFindAllByTopicIdAndUserIdNotWhenList() {
        SubscribeTopicRepositoryFake topicSubFake = new SubscribeTopicRepositoryFake();
        UserTelegramRepositoryFake userTgFake = new UserTelegramRepositoryFake(topicSubFake);
        UserTelegram user1 = new UserTelegram(1, 1, 1111L, false);
        UserTelegram user2 = new UserTelegram(2, 2, 2222L, false);
        UserTelegram user3 = new UserTelegram(3, 3, 3333L, false);
        userTgFake.save(user1);
        userTgFake.save(user2);
        userTgFake.save(user3);
        SubscribeTopic topicSub1 = new SubscribeTopic(1, user1.getUserId(), 22);
        SubscribeTopic topicSub2 = new SubscribeTopic(2, user2.getUserId(), 22);
        SubscribeTopic topicSub3 = new SubscribeTopic(3, user3.getUserId(), 22);
        topicSubFake.save(topicSub1);
        topicSubFake.save(topicSub2);
        topicSubFake.save(topicSub3);
        UserTelegramService service = new UserTelegramService(userTgFake);
        List<UserTelegram> expect = List.of(user1, user2, user3);
        List<UserTelegram> actual = service.findAllByTopicIdAndUserIdNot(topicSub1.getTopicId(), -1);

        assertThat(actual).isEqualTo(expect);
    }

}