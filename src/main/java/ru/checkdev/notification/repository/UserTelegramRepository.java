package ru.checkdev.notification.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.checkdev.notification.domain.UserTelegram;

import java.util.List;
import java.util.Optional;

public interface UserTelegramRepository extends CrudRepository<UserTelegram, Integer> {
    Optional<UserTelegram> findByChatId(long chatId);

    /**
     * Метод возвращает список пользователей телеграмм по списку id пользователей сайта.
     *
     * @param userIds список id пользователей.
     * @return список пользователей телеграмм.
     */
    @Query("SELECT ut.chatId FROM cd_user_telegram ut WHERE ut.userId IN :userIds AND ut.notifiable = true")
    List<Long> findChatIdInUserIdsIfNotifiable(@Param("userIds") List<Integer> userIds);

    /**
     * Метод возвращает пользователя телеграмм по id пользователя, если он подписан на оповещения телеграмм.
     *
     * @param userId id пользователя.
     * @return Optional пользователя телеграмм.
     */
    @Query("SELECT ut.chatId FROM cd_user_telegram ut WHERE ut.userId = :userId AND ut.notifiable = true")
    Optional<Long> findChatIdByUserIdIfNotifiable(@Param("userId") int userId);

    Optional<UserTelegram> findByUserId(int userId);

    /**
     * Метод возвращает всех подписчиков кроме автора.
     *
     * @param topicId SubscribeTopic TopicId
     * @param userId  NOT SubscribeTopic UserId
     * @return List<SubscribeTopic>
     */
    @Query("""
            SELECT new ru.checkdev.notification.domain.UserTelegram(ut.id, ut.userId, ut.chatId, ut.notifiable)
            FROM cd_user_telegram ut 
            JOIN cd_subscribe_topic st 
            ON ut.userId !=:userId AND ut.userId = st.userId AND st.topicId =:topicId 
            """)
    List<UserTelegram> findAllByTopicIdAndUserIdNot(@Param("topicId") int topicId, @Param("userId") int userId);

    @Modifying
    @Transactional
    @Query("UPDATE cd_user_telegram ut SET ut.notifiable = ?2 WHERE ut.chatId = ?1")
    void setNotifiable(@Param("chatId") long chatId, @Param("notify") boolean notify);

}
