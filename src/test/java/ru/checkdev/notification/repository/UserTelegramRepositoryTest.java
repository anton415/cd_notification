package ru.checkdev.notification.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.checkdev.notification.domain.SubscribeTopic;
import ru.checkdev.notification.domain.UserTelegram;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CheckDev пробное собеседование
 * UserTelegramRepository TEST
 *
 * @author Dmitry Stepanov
 * @version 18.11.2023 00:46
 */
class UserTelegramRepositoryTest {

    private static final UserTelegram USER_TG = new UserTelegram(11, 10, 333L, false);
    private static final UserTelegram USER_TG_NOTIFIABLE = new UserTelegram(11, 10, 333L, true);

    private SubscribeTopicRepositoryFake topicFake;
    private UserTelegramRepositoryFake userTelegramFake;

    @BeforeEach
    public void init() {
        topicFake = new SubscribeTopicRepositoryFake();
        userTelegramFake = new UserTelegramRepositoryFake(topicFake);
    }

    @Test
    void whenFindByChatIdThenReturnOptionalEmpty() {
        Optional<UserTelegram> actual = userTelegramFake.findByChatId(-1);
        assertThat(actual).isEmpty();
    }

    @Test
    void whenFindByChatIdThenReturnOptionalUserTelegram() {
        userTelegramFake.save(USER_TG);
        Optional<UserTelegram> actual = userTelegramFake.findByChatId(USER_TG.getChatId());
        assertThat(actual).contains(USER_TG);
    }

    @Test
    void whenFindChatIdInUserIdsWhenEmptyList() {
        List<Long> actual = userTelegramFake.findChatIdInUserIdsIfNotifiable(List.of(1, 2, 3));
        assertThat(actual).isEmpty();
    }

    @Test
    void whenFindChatIdByUserIdThenGetNotifiableUsersChatId() {
        userTelegramFake.save(USER_TG_NOTIFIABLE);
        Optional<Long> actualUserId = userTelegramFake.findChatIdByUserIdIfNotifiable(10);
        assertThat(Optional.of(333L)).isEqualTo(actualUserId);
    }

    @Test
    void whenFindChatIdByUserIdAndUserNotNotifiableThenGetOptionalEmpty() {
        userTelegramFake.save(USER_TG);
        Optional<Long> actualChatId = userTelegramFake.findChatIdByUserIdIfNotifiable(USER_TG.getUserId());
        assertThat(actualChatId).isEmpty();
    }

    @Test
    void whenTryToFindChatIdByInvalidUserId() {
        userTelegramFake.save(USER_TG);
        Optional<Long> actualChatId = userTelegramFake.findChatIdByUserIdIfNotifiable(-100);
        assertThat(actualChatId).isEmpty();
    }

    @Test
    void whenFindByUserIdThenOptionalEmpty() {
        Optional<UserTelegram> actual = userTelegramFake.findByUserId(-1);
        assertThat(actual).isEmpty();
    }

    @Test
    void whenFindByUserIdThenOptionalUserTelegram() {
        userTelegramFake.save(USER_TG);
        Optional<UserTelegram> actual = userTelegramFake.findByUserId(USER_TG.getUserId());
        assertThat(actual).contains(USER_TG);
    }

    @Test
    void whenFindAllByTopicIdAndUserIdNotWhenEmptyList() {
        List<UserTelegram> actual = userTelegramFake.findAllByTopicIdAndUserIdNot(-1, -1);
        assertThat(actual).isEmpty();
    }

    @Test
    void whenFindChatIdInUserIdsThenGetListWithOnlyNotifiableUsersChatID() {
        UserTelegram user1 = new UserTelegram(1, 1, 1111L, true);
        UserTelegram user2 = new UserTelegram(2, 2, 2222L, false);
        UserTelegram user3 = new UserTelegram(3, 3, 3333L, true);
        List<Integer> userIds = List.of(user1.getUserId(), user2.getUserId(), user3.getUserId());
        List<Long> expect = List.of(user1.getChatId(), user3.getChatId());

        userTelegramFake.save(user1);
        userTelegramFake.save(user2);
        userTelegramFake.save(user3);
        List<Long> actual = userTelegramFake.findChatIdInUserIdsIfNotifiable(userIds);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenFindChatIdInUserIdsWhenListChatIDFirst() {
        UserTelegram user1 = new UserTelegram(1, 1, 1111L, true);
        UserTelegram user2 = new UserTelegram(2, 2, 2222L, true);
        UserTelegram user3 = new UserTelegram(3, 3, 3333L, true);
        userTelegramFake.save(user1);
        userTelegramFake.save(user2);
        userTelegramFake.save(user3);
        List<Integer> userIds = List.of(user1.getUserId());
        List<Long> expect = List.of(user1.getChatId());
        List<Long> actual = userTelegramFake.findChatIdInUserIdsIfNotifiable(userIds);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenFindAllByTopicIdAndUserIdNotWhenListAllUser() {
        UserTelegram user1 = new UserTelegram(1, 1, 1111L, true);
        UserTelegram user2 = new UserTelegram(2, 2, 2222L, false);
        UserTelegram user3 = new UserTelegram(3, 3, 3333L, true);
        userTelegramFake.save(user1);
        userTelegramFake.save(user2);
        userTelegramFake.save(user3);
        SubscribeTopic topicSub1 = new SubscribeTopic(1, user1.getUserId(), 22);
        SubscribeTopic topicSub2 = new SubscribeTopic(2, user2.getUserId(), 22);
        SubscribeTopic topicSub3 = new SubscribeTopic(3, user3.getUserId(), 22);
        topicFake.save(topicSub1);
        topicFake.save(topicSub2);
        topicFake.save(topicSub3);
        List<UserTelegram> expect = List.of(user1, user2, user3);
        List<UserTelegram> actual = userTelegramFake.findAllByTopicIdAndUserIdNot(topicSub1.getTopicId(), -1);

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenFindAllByTopicIdAndUserIdNotWhenListNoFirstUser() {
        UserTelegram user1 = new UserTelegram(1, 1, 1111L, true);
        UserTelegram user2 = new UserTelegram(2, 2, 2222L, false);
        UserTelegram user3 = new UserTelegram(3, 3, 3333L, true);
        userTelegramFake.save(user1);
        userTelegramFake.save(user2);
        userTelegramFake.save(user3);
        SubscribeTopic topicSub1 = new SubscribeTopic(1, user1.getUserId(), 22);
        SubscribeTopic topicSub2 = new SubscribeTopic(2, user2.getUserId(), 22);
        SubscribeTopic topicSub3 = new SubscribeTopic(3, user3.getUserId(), 22);
        topicFake.save(topicSub1);
        topicFake.save(topicSub2);
        topicFake.save(topicSub3);
        List<UserTelegram> expect = List.of(user2, user3);
        List<UserTelegram> actual = userTelegramFake.findAllByTopicIdAndUserIdNot(topicSub1.getTopicId(), user1.getUserId());

        assertThat(actual).isEqualTo(expect);
    }

    @Test
    void whenSetNotifiableThenNotifiableValueChanged() {
        UserTelegram userNotifiable = new UserTelegram(1, 1, 1111L, true);
        UserTelegram userUnNotifiable = new UserTelegram(2, 2, 2222L, false);
        userTelegramFake.save(userNotifiable);
        userTelegramFake.save(userUnNotifiable);
        UserTelegram expectedUserNotifiable = new UserTelegram(1, 1, 1111L, false);
        UserTelegram expectedUserUnNotifiable = new UserTelegram(2, 2, 2222L, true);

        userTelegramFake.setNotifiable(userNotifiable.getChatId(), false);
        userTelegramFake.setNotifiable(userUnNotifiable.getChatId(), true);
        Optional<UserTelegram> actualUser1 = userTelegramFake.findByUserId(1);
        Optional<UserTelegram> actualUser2 = userTelegramFake.findByUserId(2);

        assertThat(actualUser1).isEqualTo(Optional.of(expectedUserNotifiable));
        assertThat(actualUser2).isEqualTo(Optional.of(expectedUserUnNotifiable));
    }

}